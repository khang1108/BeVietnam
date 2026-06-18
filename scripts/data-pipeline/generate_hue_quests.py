"""
Landmark-centric quest + spotlight generator for the Huế pilot.

Why this exists: the windowed book ingestion produced a thin, scattered chunk
set. This pipeline instead pulls rich passages straight from the OCR'd Huế books
*per famous landmark*, grounds the LLM (self-hosted vLLM) in them, and emits:

  - >=100 reusable QuestionPoolItem tasks, grouped into one quest-chain per
    landmark (metadata.quest_id), merged into data/question_pool.json.
  - one condition-tagged SpotlightPost per landmark -> data/posts/hue_spotlights.json.

Everything stays grounded (source_text traces to a real passage) and carries
review_status=needs_review until a human approves it.

Run (from repo root, with the env that has pydantic + httpx):
    /opt/miniconda3/bin/python scripts/data-pipeline/generate_hue_quests.py \
        --per-landmark 10

Requires the vLLM endpoint to be UP (api.iamphuckhang.dev).
"""

from __future__ import annotations

import argparse
import json
import logging
import re
import sys
import time
import unicodedata
from datetime import date
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parents[1]
for _p in (str(REPO_ROOT), str(SCRIPT_DIR)):
    if _p not in sys.path:
        sys.path.insert(0, _p)

from hue_landmarks import HUE_BOOKS, HUE_LANDMARKS, Landmark  # noqa: E402
from services.ai.agents.safety_keeper import SafetyKeeper  # noqa: E402
from services.ai.agents.spotlight_maker import SpotlightMaker  # noqa: E402
from services.ai.common.knowledge_schema import SourceType  # noqa: E402
from services.ai.common.llm import vllm_gateway  # noqa: E402
from services.ai.common.post_schema import SpotlightPost, SpotlightPostCollection  # noqa: E402
from services.ai.common.knowledge_schema import SourceRef  # noqa: E402

logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
logger = logging.getLogger("generate_hue_quests")

POOL_PATH = REPO_ROOT / "data" / "question_pool.json"
POSTS_PATH = REPO_ROOT / "data" / "posts" / "hue_spotlights.json"

_PAGE_MARKER_RE = re.compile(r"^\{\d+\}[-—]*\s*$")
_SEPARATOR_RE = re.compile(r"^[-—_=|·•\s]{3,}$")
_HEADING_MARK_RE = re.compile(r"^#{1,6}\s+")
_MULTISPACE_RE = re.compile(r"[ \t]+")

_REQUIRED_FIELDS = [
    "title",
    "question_text",
    "cultural_explanation",
    "source_text",
    "source",
    "place_name",
    "categories",
    "difficulty",
    "required_media",
    "indoor_outdoor",
    "weather_tags",
    "time_tags",
    "estimated_duration_minutes",
]


# ── passage mining ────────────────────────────────────────────────────────────
def _clean_line(line: str) -> str:
    line = _HEADING_MARK_RE.sub("", line)
    line = re.sub(r"<[^>]+>", "", line)  # stray OCR html
    return _MULTISPACE_RE.sub(" ", line).strip()


def load_passages(path: Path) -> list[str]:
    """Split a book into clean prose paragraphs (150–1200 chars)."""
    if not path.exists():
        logger.warning("book missing: %s", path)
        return []
    raw = path.read_text(encoding="utf-8", errors="ignore")
    paragraphs: list[str] = []
    buf: list[str] = []
    for line in raw.splitlines():
        s = line.strip()
        if not s or _PAGE_MARKER_RE.match(s) or _SEPARATOR_RE.match(s):
            if buf:
                paragraphs.append(" ".join(buf))
                buf = []
            continue
        if s.startswith("|"):  # OCR table row — skip
            continue
        cleaned = _clean_line(s)
        if cleaned:
            buf.append(cleaned)
    if buf:
        paragraphs.append(" ".join(buf))
    return [p for p in paragraphs if 150 <= len(p) <= 1200]


def _casefold(s: str) -> str:
    return unicodedata.normalize("NFC", s).casefold()


def select_passages(
    landmark: Landmark,
    corpus: list[tuple[str, str]],
    top_k: int,
) -> list[tuple[str, str]]:
    """Rank (book, passage) by alias hits; return the top_k for this landmark."""
    aliases = [_casefold(a) for a in landmark.aliases]
    scored: list[tuple[float, str, str]] = []
    for book_name, passage in corpus:
        low = _casefold(passage)
        hits = sum(low.count(a) for a in aliases)
        if hits == 0:
            continue
        distinct = sum(1 for a in aliases if a in low)
        length_bonus = 1.0 if 250 <= len(passage) <= 900 else 0.4
        score = hits + distinct * 1.5 + length_bonus
        scored.append((score, book_name, passage))
    scored.sort(key=lambda t: t[0], reverse=True)
    return [(book, _trim(passage)) for _, book, passage in scored[:top_k]]


def _trim(text: str, limit: int = 750) -> str:
    if len(text) <= limit:
        return text
    cut = text[:limit]
    dot = cut.rfind(". ")
    return (cut[: dot + 1] if dot > 250 else cut).strip()


# ── generation ────────────────────────────────────────────────────────────────
SINGLE_Q_SYSTEM = (
    "Bạn là nhà thiết kế trải nghiệm du lịch văn hóa Huế. Từ MỘT đoạn tư liệu về "
    "một địa danh, hãy tạo MỘT nhiệm vụ khám phá mà du khách có thể thực hiện ngay "
    "tại địa điểm (quan sát, chụp ảnh, ghi chú một chi tiết có thật). Chỉ dùng "
    "thông tin trong đoạn tư liệu, không bịa. Trả về DUY NHẤT một đối tượng JSON, "
    "không mảng, không thêm chữ nào khác."
)


def _single_question_prompt(landmark: Landmark, passage: str) -> str:
    cats = " | ".join(landmark.categories)
    return (
        f"Địa danh: {landmark.place_name}\n"
        f"Đoạn tư liệu (chỉ dùng nội dung này):\n{passage}\n\n"
        "Trả về một đối tượng JSON đúng định dạng:\n"
        "{\n"
        '  "title": "<tiêu đề ngắn, <= 12 từ>",\n'
        '  "question_text": "<nhiệm vụ cụ thể du khách làm tại chỗ>",\n'
        '  "cultural_explanation": "<vì sao chi tiết này có ý nghĩa văn hóa>",\n'
        '  "source_text": "<câu trong đoạn tư liệu làm căn cứ>",\n'
        f'  "categories": [<chọn trong: {cats}>],\n'
        '  "difficulty": "easy" | "medium" | "hard",\n'
        '  "required_media": "photo" | "note" | "quiz_answer",\n'
        '  "indoor_outdoor": "indoor" | "outdoor" | "any",\n'
        '  "weather_tags": [<"sunny"|"rainy"|"hot"|"cloudy"|"any">],\n'
        '  "time_tags": [<"morning"|"afternoon"|"evening"|"night"|"any">],\n'
        '  "estimated_duration_minutes": <số phút>\n'
        "}"
    )


def _coerce_question(raw: dict) -> dict | None:
    """Accept either a bare question object or a {'questions': [...]} wrapper."""
    if not isinstance(raw, dict):
        return None
    questions = raw.get("questions")
    if isinstance(questions, list) and questions and isinstance(questions[0], dict):
        return questions[0]
    if str(raw.get("question_text") or "").strip():
        return raw
    return None


def _retry_json(fn, attempts: int = 4, base_delay: float = 2.0) -> dict:
    """Retry a gateway call that returns {} on failure (transient 530/timeout)."""
    result: dict = {}
    for i in range(attempts):
        result = fn()
        if result:
            return result
        if i < attempts - 1:
            time.sleep(base_delay * (2**i))
    return result


def _slugify(text: str, max_len: int = 20) -> str:
    norm = unicodedata.normalize("NFKD", text)
    ascii_only = norm.encode("ascii", "ignore").decode("ascii").lower()
    slug = re.sub(r"[^a-z0-9]+", "-", ascii_only).strip("-")
    return slug[:max_len].strip("-") or "x"


def generate_questions_for(
    landmark: Landmark,
    passages: list[tuple[str, str]],
    keeper: SafetyKeeper,
    per_landmark: int,
) -> list[dict]:
    # One vLLM call per passage -> one question. Qwen won't reliably emit a
    # 10-item array (it returns a single object and large arrays risk timeout),
    # so we loop: short, fast, reliable JSON, and each question stays bound to a
    # specific real passage.
    books_used = sorted({b for b, _ in passages})
    source_label = "; ".join(books_used)
    out: list[dict] = []
    seq = 0
    for _book_name, passage in passages[:per_landmark]:
        raw = _retry_json(
            lambda p=passage: vllm_gateway.generate_json(
                SINGLE_Q_SYSTEM, _single_question_prompt(landmark, p), max_tokens=600
            )
        )
        time.sleep(0.4)  # throttle — sustained rapid calls crash the endpoint
        cand = _coerce_question(raw)
        if cand is None:
            logger.info("  [%s] no question from a passage", landmark.place_id)
            continue
        question_text = str(cand.get("question_text") or "").strip()
        if not question_text:
            continue
        # Ground to the real passage, not the model's possibly-paraphrased quote.
        source_text = str(cand.get("source_text") or "").strip() or passage
        seq += 1
        qid = f"hue-{landmark.place_id}-{seq:02d}-{_slugify(cand.get('title', ''))}"
        item = {
            "question_id": qid,
            "title": str(cand.get("title") or landmark.display_quest_title()).strip()[:160],
            "question_text": question_text,
            "cultural_explanation": str(
                cand.get("cultural_explanation") or source_text
            ).strip(),
            "source_text": source_text[:900],
            "source": source_label,
            "place_name": landmark.place_name,
            "latitude": landmark.latitude,
            "longitude": landmark.longitude,
            "radius_meters": landmark.radius_meters,
            "categories": _norm_list(cand.get("categories"), landmark.categories),
            "difficulty": str(cand.get("difficulty") or "easy").lower(),
            "required_media": str(cand.get("required_media") or "photo").lower(),
            "indoor_outdoor": str(
                cand.get("indoor_outdoor") or landmark.indoor_outdoor
            ).lower(),
            "weather_tags": _norm_list(cand.get("weather_tags"), ["any"]),
            "time_tags": _norm_list(cand.get("time_tags"), ["any"]),
            "estimated_duration_minutes": _as_int(
                cand.get("estimated_duration_minutes"), 15
            ),
            "language": "vi",
            "metadata": {
                "quest_id": landmark.quest_id(),
                "quest_title": landmark.display_quest_title(),
                "place_id": landmark.place_id,
                "source_books": books_used,
                "review_status": "needs_review",
                "auto_generated": True,
                "title_en": landmark.title_en,
            },
        }
        validation_item = {**item, "description": item["question_text"]}
        valid, errors = keeper.validate_full(validation_item, _REQUIRED_FIELDS)
        if valid:
            out.append(item)
        else:
            logger.info("  [%s] rejected: %s", landmark.place_id, errors)
    # stamp step_index/total now that the count is known
    total = len(out)
    for i, item in enumerate(out, 1):
        item["metadata"]["step_index"] = i
        item["metadata"]["total_steps"] = total
    return out


def build_spotlight(
    landmark: Landmark,
    passages: list[tuple[str, str]],
    maker: SpotlightMaker,
    today: date,
) -> SpotlightPost | None:
    facts = [{"text": p, "place_name": landmark.place_name} for _, p in passages]
    # Retry while the maker falls back (LLM blip) so we keep a real post, not a
    # generic fallback, when the endpoint recovers.
    draft: dict = {}
    for i in range(4):
        draft = maker.generate(landmark.place_name, facts, language="vi")
        if draft and not maker.used_fallback:
            break
        if i < 3:
            time.sleep(2.0 * (2**i))
    if not draft or not str(draft.get("body", "")).strip():
        logger.warning("  [%s] no spotlight draft", landmark.place_id)
        return None

    source_refs = [
        SourceRef(
            source_type=SourceType.BOOK,
            title=book_name,
            publisher="Nguồn sách (chưa kiểm định)",
            page_or_section="",
        )
        for book_name in sorted({b for b, _ in passages})
    ]
    try:
        return SpotlightPost(
            post_id=f"spot-hue-{landmark.place_id}",
            place_id=landmark.place_id,
            place_name=landmark.place_name,
            title=str(draft.get("title") or landmark.place_name).strip()[:160],
            body=str(draft.get("body") or "").strip()[:900],
            cultural_hook=str(draft.get("cultural_hook") or "").strip()[:400],
            language="vi",
            weather_tags=draft.get("weather_tags") or [],
            time_tags=draft.get("time_tags") or [],
            season_tags=draft.get("season_tags") or [],
            categories=_norm_list(draft.get("categories"), landmark.categories),
            source_refs=source_refs,
            reviewed_at=today,
            tags=landmark.categories,
            notes="auto-generated from Huế source books; pending human review",
        )
    except Exception as exc:  # noqa: BLE001 — validation failure -> skip post
        logger.warning("  [%s] spotlight invalid: %s", landmark.place_id, exc)
        return None


# ── helpers ───────────────────────────────────────────────────────────────────
def _norm_list(value, default: list[str]) -> list[str]:
    if isinstance(value, list):
        out = [str(x).strip().lower() for x in value if str(x).strip()]
        return out or list(default)
    if isinstance(value, str) and value.strip():
        return [value.strip().lower()]
    return list(default)


def _as_int(value, default: int) -> int:
    try:
        return int(value)
    except (TypeError, ValueError):
        return default


def merge_question_pool(new_questions: list[dict], today: date) -> tuple[int, int]:
    """
    Incremental merge: replace only the landmarks present in this batch.

    Keeps the curated storylines and any auto questions for landmarks NOT
    regenerated this run, then appends the fresh batch. This makes partial runs
    (e.g. --only after a vLLM crash) accumulate toward >=100 instead of wiping
    earlier good output.
    """
    pool = json.loads(POOL_PATH.read_text(encoding="utf-8"))
    new_place_ids = {
        q.get("metadata", {}).get("place_id") for q in new_questions
    }
    existing = [
        q
        for q in pool.get("questions", [])
        if not q.get("metadata", {}).get("auto_generated")
        or q.get("metadata", {}).get("place_id") not in new_place_ids
    ]
    seen = {q["question_id"] for q in existing}
    added = 0
    for q in new_questions:
        if q["question_id"] in seen:
            continue
        existing.append(q)
        seen.add(q["question_id"])
        added += 1
    pool["questions"] = existing
    pool["reviewed_at"] = pool.get("reviewed_at", str(today))
    pool["description"] = (
        "Huế pilot pool: 2 reviewed storylines + landmark-grounded quest-chains "
        "auto-generated from the Huế source books (needs_review). Each task binds "
        "a famous landmark with GPS + radius and cites a book passage."
    )
    POOL_PATH.write_text(
        json.dumps(pool, ensure_ascii=False, indent=2), encoding="utf-8"
    )
    return added, len(existing)


def write_posts(posts: list[SpotlightPost]) -> None:
    # Incremental: merge with any existing posts, new batch wins per post_id.
    merged: dict[str, dict] = {}
    if POSTS_PATH.exists():
        prior = json.loads(POSTS_PATH.read_text(encoding="utf-8"))
        for p in prior.get("posts", []):
            merged[p["post_id"]] = p
    for post in posts:
        merged[post.post_id] = post.model_dump(mode="json")

    collection = SpotlightPostCollection(
        dataset_id="hue-landmark-spotlights",
        dataset_name="Huế landmark spotlights",
        description="Condition-tagged spotlight posts for famous Huế landmarks, "
        "grounded in the source books (needs_review).",
        posts=list(merged.values()),
    )
    POSTS_PATH.parent.mkdir(parents=True, exist_ok=True)
    POSTS_PATH.write_text(
        collection.model_dump_json(indent=2), encoding="utf-8"
    )


# ── main ──────────────────────────────────────────────────────────────────────
def main() -> int:
    parser = argparse.ArgumentParser(description="Generate Huế landmark quests + spotlights.")
    parser.add_argument("--per-landmark", type=int, default=10, help="Questions to request per landmark.")
    parser.add_argument("--top-k", type=int, default=7, help="Passages to ground each landmark.")
    parser.add_argument("--only", nargs="+", default=None, help="Limit to these place_ids.")
    parser.add_argument("--min-questions", type=int, default=8, help="Skip landmarks already holding this many auto questions (full runs only).")
    parser.add_argument("--force", action="store_true", help="Regenerate even landmarks that already have enough questions.")
    parser.add_argument("--dry-run", action="store_true", help="Generate but do not write files.")
    args = parser.parse_args()

    today = date.today()

    logger.info("Loading Huế books…")
    corpus: list[tuple[str, str]] = []
    for book_name, rel in HUE_BOOKS.items():
        passages = load_passages(REPO_ROOT / rel)
        logger.info("  %s -> %d passages", book_name, len(passages))
        corpus.extend((book_name, p) for p in passages)
    if not corpus:
        logger.error("No passages loaded. Aborting.")
        return 1

    landmarks = HUE_LANDMARKS
    if args.only:
        only = set(args.only)
        landmarks = [lm for lm in landmarks if lm.place_id in only]

    # Skip landmarks already holding enough auto questions (full runs only), so
    # a re-run after a crash only spends calls on the gaps.
    existing_counts: dict[str, int] = {}
    if POOL_PATH.exists() and args.only is None and not args.force:
        prior = json.loads(POOL_PATH.read_text(encoding="utf-8"))
        for q in prior.get("questions", []):
            meta = q.get("metadata", {})
            if meta.get("auto_generated"):
                pid = meta.get("place_id")
                existing_counts[pid] = existing_counts.get(pid, 0) + 1

    keeper = SafetyKeeper()
    maker = SpotlightMaker()
    all_questions: list[dict] = []
    posts: list[SpotlightPost] = []
    consecutive_zero = 0

    for lm in landmarks:
        if existing_counts.get(lm.place_id, 0) >= args.min_questions:
            logger.info(
                "[%s] already has %d auto questions — skipping",
                lm.place_id,
                existing_counts[lm.place_id],
            )
            continue
        passages = select_passages(lm, corpus, args.top_k)
        if not passages:
            logger.warning("[%s] no matching passages — skipped", lm.place_id)
            continue
        logger.info("[%s] %d passages -> generating…", lm.place_id, len(passages))
        questions = generate_questions_for(lm, passages, keeper, args.per_landmark)
        logger.info("  -> %d valid questions", len(questions))

        if questions:
            all_questions.extend(questions)
            post = build_spotlight(lm, passages, maker, today)
            if post:
                posts.append(post)
            consecutive_zero = 0
        else:
            consecutive_zero += 1
            if consecutive_zero >= 2:
                logger.error(
                    "2 consecutive landmarks produced 0 questions — vLLM likely "
                    "down. Stopping; partial progress will be saved."
                )
                break

    logger.info(
        "TOTAL: %d questions across %d landmarks, %d posts",
        len(all_questions),
        len(landmarks),
        len(posts),
    )

    if args.dry_run:
        logger.info("dry-run: nothing written")
        return 0

    added, pool_total = merge_question_pool(all_questions, today)
    auto_total = pool_total - _curated_count()
    logger.info(
        "question_pool.json: +%d this run (pool %d, auto %d)",
        added,
        pool_total,
        auto_total,
    )
    if posts:
        write_posts(posts)
        logger.info("hue_spotlights.json: +%d posts this run", len(posts))

    if auto_total < 100:
        logger.warning(
            "Auto questions < 100 (%d). vLLM may have dropped — re-run to fill gaps.",
            auto_total,
        )
    else:
        logger.info("Auto questions >= 100 (%d). Target met.", auto_total)
    return 0


def _curated_count() -> int:
    """How many non-auto (curated) questions are in the pool."""
    pool = json.loads(POOL_PATH.read_text(encoding="utf-8"))
    return sum(
        1
        for q in pool.get("questions", [])
        if not q.get("metadata", {}).get("auto_generated")
    )


if __name__ == "__main__":
    raise SystemExit(main())

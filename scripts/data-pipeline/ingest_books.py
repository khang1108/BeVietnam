"""
Book ingestion → KnowledgeChunk drafts for the BeVietnam cultural KB.

Two-stage pipeline (per docs/book-ingestion-pipeline.md, adapted to the
current KnowledgeChunk schema in services/ai/common/knowledge_schema.py):

  Stage 1 — parse + chunk
    Section-aware: split a Markdown/TXT book by headings (so every chunk
    keeps its `page_or_section`), then slide ~600-char overlapping windows
    on sentence boundaries within each section.

  Stage 2 — LLM extraction (default provider: self-hosted vLLM)
    One JSON call per chunk. The model distils a single grounded cultural fact
    and maps it to a controlled place_id vocabulary (the Huế POIs + the
    national `viet-nam` bucket) and a fixed category set. Anything without
    clear cultural content is skipped. Provider is selectable with --provider
    (vllm | gemini); per-book source identity comes from the BOOKS registry.

Auto-extracted facts are NOT trusted blindly: every emitted chunk is written
with `review_status = needs_review` and `source_type = book`, matching the
project policy that book claims must be marked and human-reviewed before they
are promoted to `approved` / seeded as authoritative.

Usage (from repo root):
    # 2 Huế books → one collection (Task 1.3):
    PYTHONPATH=. services/backend/venv/bin/python scripts/data-pipeline/ingest_books.py \
        --book data/books/Co-do-hue-xua-va-nay.md \
               data/books/30-nam-nghien-cuu-van-hoa-dan-gian.md \
        --out  data/knowledge/hue_book_chunks.json

    # parse/chunk only, no LLM (offline sanity check):
    ... ingest_books.py --book data/books/Co-do-hue-xua-va-nay.md --dry-run

    # cap chunks per book sent to the LLM:
    ... ingest_books.py --book <book.md> --limit 20

    # also embed + upsert the drafts into Qdrant:
    ... ingest_books.py --book <book.md> --seed
"""

from __future__ import annotations

import argparse
import json
import logging
import re
import sys
import unicodedata
from dataclasses import dataclass
from datetime import date
from pathlib import Path

from services.ai.common.knowledge_schema import (
    KnowledgeChunk,
    KnowledgeChunkCollection,
)
from services.ai.common.llm import llm_gateway, vllm_gateway

logger = logging.getLogger("ingest_books")

# ── Controlled vocabulary ─────────────────────────────────────────────────────
# Gemini must map every fact onto one of these places. Default is the national
# `viet-nam` bucket; a Huế POI slug is used only when the passage is clearly
# about that specific place/topic. Keep in sync with data/knowledge/hue_chunks.json.
PLACE_VOCAB: dict[str, str] = {
    "viet-nam": "Việt Nam",
    "thua-thien-hue": "Thừa Thiên Huế",
    "hue-imperial-city": "Kinh thành Huế",
    "thien-mu-pagoda": "Chùa Thiên Mụ",
    "perfume-river": "Sông Hương",
    "lang-minh-mang": "Lăng Minh Mạng",
    "lang-tu-duc": "Lăng Tự Đức",
    "lang-khai-dinh": "Lăng Khải Định",
    "nha-nhac": "Nhã nhạc cung đình Huế",
    "chau-ban-nguyen": "Châu bản triều Nguyễn",
    "hue-cuisine": "Ẩm thực Huế",
    "van-mieu-hue": "Văn Miếu Huế",
    "dan-nam-giao": "Đàn Nam Giao",
}

CATEGORY_VOCAB = [
    "architecture", "art", "festival", "food",
    "history", "nature", "religion", "tradition",
]

# ── Per-book source identity ──────────────────────────────────────────────────
# Keyed by file stem. `id_prefix` keeps chunk_ids unique + traceable per book;
# `scope` is "hue" (POI-bound primary) or "national" (viet-nam supplementary).
@dataclass(frozen=True)
class BookSource:
    source_title: str
    publisher: str
    id_prefix: str
    scope: str          # "hue" | "national"
    default_place: str  # bucket for province/non-POI facts in this book


BOOKS: dict[str, BookSource] = {
    "Co-do-hue-xua-va-nay": BookSource(
        "Cố Đô Huế Xưa Và Nay",
        "Hội KHLS Thừa Thiên Huế — NXB Thuận Hóa (2005)",
        "cdhxvn",
        "hue",
        "thua-thien-hue",
    ),
    "30-nam-nghien-cuu-van-hoa-dan-gian": BookSource(
        "30 năm nghiên cứu văn hóa dân gian Thừa Thiên Huế (1991–2021)",
        "Hội Văn nghệ Dân gian Thừa Thiên Huế — NXB Thuận Hóa (2021)",
        "vhdg",
        "hue",
        "thua-thien-hue",
    ),
    "Co-so-van-hoa-viet-nam": BookSource(
        "Cơ sở văn hóa Việt Nam",
        "Trần Ngọc Thêm — NXB Giáo dục",
        "csvhvn",
        "national",
        "viet-nam",
    ),
    "Van-hoa-am-thuc-viet-nam-tu-ly-luan-va-thuc-tien": BookSource(
        "Văn hóa ẩm thực Việt Nam nhìn từ lý luận và thực tiễn",
        "Trần Quốc Vượng, Nguyễn Thị Bảy — NXB Từ điển Bách khoa (2010)",
        "vhat",
        "national",
        "viet-nam",
    ),
}


def resolve_book(path: Path) -> BookSource:
    """Look up a book's source identity by file stem, with a safe default."""
    book = BOOKS.get(path.stem)
    if book is None:
        logger.warning("No source registry entry for %s — using generic attribution.", path.name)
        return BookSource(path.stem, path.stem, _slugify(path.stem, 16), "national", "viet-nam")
    return book

CHUNK_CHARS = 600
CHUNK_STEP = 400
MIN_SECTION_CHARS = 120

_HEADING_RE = re.compile(r"^#{1,6}\s+(.*\S)\s*$")
_SENTENCE_SPLIT_RE = re.compile(r"(?<=[.!?…])\s+")


# ── Stage 1: parse + chunk ────────────────────────────────────────────────────
_PAGE_MARKER_RE = re.compile(r"^\{\d+\}[-—]*$")  # OCR page break: {12}-------------


_INLINE_HTML_RE = re.compile(r"<[^>]+>")  # <sup>3</sup>, <br>, … OCR/markdown cruft


def _is_noise(line: str) -> bool:
    """Drop OCR image refs / scan artifacts / page markers / TOC rows."""
    stripped = line.strip()
    return (
        not stripped
        or stripped.startswith("![")
        or stripped.startswith("|")  # markdown table row → TOC/index in these books
        or stripped.lower().startswith("scan to open")
        or bool(re.fullmatch(r"_page_\d+.*", stripped))
        or bool(_PAGE_MARKER_RE.match(stripped))
        or bool(re.fullmatch(r"[-—_=|]{3,}", stripped))  # bare separator rules
    )


def _clean_inline(text: str) -> str:
    """Strip inline HTML tags and collapse whitespace within a passage."""
    return _INLINE_HTML_RE.sub("", text).strip()


def split_sections(text: str) -> list[tuple[str, str]]:
    """Split Markdown into (heading, body) sections. Pre-heading text → 'Mở đầu'."""
    sections: list[tuple[str, list[str]]] = [("Mở đầu", [])]
    for line in text.splitlines():
        heading = _HEADING_RE.match(line)
        if heading:
            sections.append((heading.group(1).strip(), []))
        elif not _is_noise(line):
            sections[-1][1].append(line)
    return [(h, "\n".join(body).strip()) for h, body in sections]


def window_section(body: str) -> list[str]:
    """Slide overlapping ~CHUNK_CHARS windows over a section on sentence breaks."""
    sentences = [s.strip() for s in _SENTENCE_SPLIT_RE.split(body) if s.strip()]
    windows: list[str] = []
    cursor = 0
    while cursor < len(sentences):
        buf: list[str] = []
        size = 0
        idx = cursor
        while idx < len(sentences) and size < CHUNK_CHARS:
            buf.append(sentences[idx])
            size += len(sentences[idx]) + 1
            idx += 1
        window = _clean_inline(" ".join(buf))
        if len(window) >= 60:
            windows.append(window)
        # advance by ~CHUNK_STEP worth of characters for overlap
        step_chars, advanced = 0, cursor
        while advanced < idx and step_chars < CHUNK_STEP:
            step_chars += len(sentences[advanced]) + 1
            advanced += 1
        cursor = max(advanced, cursor + 1)
    return windows


def build_chunks(text: str) -> list[tuple[str, str]]:
    """Return (section_heading, chunk_text) pairs for the whole book."""
    pairs: list[tuple[str, str]] = []
    for heading, body in split_sections(text):
        if len(body) < MIN_SECTION_CHARS:
            continue
        for window in window_section(body):
            pairs.append((heading, window))
    return pairs


# ── Stage 2: Gemini extraction ────────────────────────────────────────────────
_SYSTEM_PROMPT = (
    "Bạn là chuyên gia văn hóa Việt Nam, trích xuất tri thức nền có thể kiểm chứng "
    "từ sách giáo trình. Chỉ trả về JSON, không thêm chữ nào khác."
)


def _user_prompt(section: str, passage: str, default_place: str) -> str:
    places = "\n".join(f"  {slug} = {name}" for slug, name in PLACE_VOCAB.items())
    return (
        f"Mục sách: {section}\n"
        f"Đoạn văn:\n\"\"\"\n{passage}\n\"\"\"\n\n"
        "Rút ra MỘT sự thật văn hóa rõ ràng, trung lập, đứng độc lập từ đoạn trên.\n"
        "Trả về JSON đúng định dạng:\n"
        '{\n'
        '  "text": "<một đoạn 1-3 câu nêu sự thật văn hóa, tiếng Việt sạch>",\n'
        '  "place_id": "<một slug trong danh sách bên dưới>",\n'
        '  "category": "<một trong: ' + ", ".join(CATEGORY_VOCAB) + '>",\n'
        '  "tags": ["<2-4 từ khóa slug>"]\n'
        "}\n"
        f"Danh sách place_id hợp lệ (mặc định {default_place}; chọn một POI Huế cụ thể "
        "khi đoạn nói rõ về nơi/chủ đề đó; dùng thua-thien-hue cho sự thật cấp tỉnh "
        "Huế không gắn POI cụ thể):\n" + places + "\n"
        "Nếu đoạn không có nội dung văn hóa rõ ràng, trả về: {\"skip\": true}"
    )


def _slugify(value: str, max_len: int = 80) -> str:
    norm = unicodedata.normalize("NFKD", value)
    norm = "".join(c for c in norm if not unicodedata.combining(c)).lower()
    norm = re.sub(r"[^a-z0-9]+", "-", norm).strip("-")
    return norm[:max_len].strip("-") or "item"


def extract_chunk(
    section: str,
    passage: str,
    index: int,
    used_ids: set[str],
    book: BookSource,
    gateway,
) -> KnowledgeChunk | None:
    """Call the LLM for one passage and build a validated KnowledgeChunk draft."""
    result = gateway.generate_json(
        _SYSTEM_PROMPT, _user_prompt(section, passage, book.default_place)
    )
    if not result or result.get("skip"):
        return None

    place_id = str(result.get("place_id", book.default_place)).strip().lower()
    if place_id not in PLACE_VOCAB:
        place_id = book.default_place
    category = str(result.get("category", "")).strip().lower()
    if category not in CATEGORY_VOCAB:
        category = "tradition"
    fact = str(result.get("text", "")).strip()
    if len(fact) < 40:
        return None

    base_id = f"book-{book.id_prefix}-{place_id}-{_slugify(section, 24)}-{index:03d}"
    chunk_id = base_id
    suffix = 1
    while chunk_id in used_ids:
        suffix += 1
        chunk_id = f"{base_id}-{suffix}"
    used_ids.add(chunk_id)

    try:
        return KnowledgeChunk(
            chunk_id=chunk_id,
            place_id=place_id,
            place_name=PLACE_VOCAB[place_id],
            category=category,
            language="vi",
            text=fact[:900],
            source_type="book",
            source_title=book.source_title,
            publisher=book.publisher,
            page_or_section=section[:160],
            reviewed_at=date.today(),
            review_status="needs_review",
            related_poi_ids=[] if place_id in {"viet-nam", "thua-thien-hue"} else [place_id],
            tags=[str(t).strip().lower() for t in result.get("tags", []) if str(t).strip()],
            notes=f"auto-extracted from {book.source_title}; pending human review",
        )
    except Exception as exc:  # pydantic validation failure → skip this passage
        logger.warning("Chunk %s failed validation: %s", chunk_id, exc)
        return None


# ── Orchestration ─────────────────────────────────────────────────────────────
GATEWAYS = {"vllm": vllm_gateway, "gemini": llm_gateway}


def main() -> int:
    parser = argparse.ArgumentParser(description="Ingest book(s) into KnowledgeChunk drafts.")
    parser.add_argument("--book", required=True, nargs="+", type=Path, help="Book .md/.txt path(s)")
    parser.add_argument(
        "--out", type=Path,
        default=Path("data/knowledge/vietnam_culture_chunks.json"),
        help="Output KnowledgeChunkCollection JSON",
    )
    parser.add_argument("--dataset-id", default="", help="Collection slug (default: derived from --out)")
    parser.add_argument("--dataset-name", default="", help="Human collection name (default: derived)")
    parser.add_argument(
        "--provider", choices=sorted(GATEWAYS), default="gemini",
        help="LLM provider for extraction (default: gemini)",
    )
    parser.add_argument("--limit", type=int, default=0, help="Cap chunks per book sent to LLM (0 = all)")
    parser.add_argument("--dry-run", action="store_true", help="Parse + chunk only; no LLM calls")
    parser.add_argument("--seed", action="store_true", help="Embed + upsert drafts into Qdrant")
    args = parser.parse_args()

    logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")

    missing = [b for b in args.book if not b.exists()]
    if missing:
        logger.error("Book(s) not found: %s", ", ".join(str(m) for m in missing))
        return 2

    # Stage 1 — parse + chunk every book (offline).
    book_pairs: list[tuple[BookSource, list[tuple[str, str]]]] = []
    for book_path in args.book:
        source = resolve_book(book_path)
        pairs = build_chunks(book_path.read_text(encoding="utf-8"))
        if args.limit:
            pairs = pairs[: args.limit]
        logger.info("Parsed %d passages from %s [%s]", len(pairs), book_path.name, source.scope)
        book_pairs.append((source, pairs))

    if args.dry_run:
        for source, pairs in book_pairs:
            logger.info("── %s ──", source.source_title)
            for section, passage in pairs[:3]:
                logger.info("  [%s] %s…", section[:40], passage[:90])
        total = sum(len(p) for _, p in book_pairs)
        logger.info("Dry run complete — %d total passages, no LLM calls made.", total)
        return 0

    # Stage 2 — LLM extraction across all books into one collection.
    gateway = GATEWAYS[args.provider]
    used_ids: set[str] = set()
    chunks: list[KnowledgeChunk] = []
    attempts = empty = 0
    for source, pairs in book_pairs:
        for i, (section, passage) in enumerate(pairs):
            attempts += 1
            chunk = extract_chunk(section, passage, i, used_ids, source, gateway)
            if chunk is None:
                empty += 1
            else:
                chunks.append(chunk)

    # Quota / outage guard: if the LLM produced nothing across many attempts the
    # gateway is almost certainly returning {} (429 quota, or the vLLM endpoint
    # down — e.g. HTTP 530 from cloudflared). Do NOT write an empty dataset.
    if not chunks:
        logger.error(
            "Extracted 0 chunks from %d passages via '%s'. The endpoint is likely "
            "unavailable (the gateway swallows errors and returns {}). Nothing "
            "written. Bring the provider up and re-run.",
            attempts, args.provider,
        )
        return 1
    if empty:
        logger.warning("%d/%d passages produced no fact (skipped or failed).", empty, attempts)

    dataset_id = args.dataset_id or _slugify(args.out.stem, 60)
    dataset_name = args.dataset_name or f"{args.out.stem} — auto-extracted drafts"
    collection = KnowledgeChunkCollection(
        version=1,
        dataset_id=dataset_id,
        dataset_name=dataset_name,
        description=(
            f"Auto-extracted culture chunks from {len(args.book)} book(s) via "
            f"'{args.provider}'. review_status=needs_review until a human approves each chunk."
        ),
        chunks=chunks,
    )
    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(
        json.dumps(collection.model_dump(mode="json"), ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    logger.info("Wrote %d draft chunks → %s", len(chunks), args.out)

    if args.seed:
        from services.ai.common.qdrant_store import qdrant_store

        qdrant_store.ensure_collection()
        n = qdrant_store.upsert_documents([c.qdrant_payload() for c in chunks])
        logger.info("Seeded %d chunks into Qdrant collection.", n)

    return 0


if __name__ == "__main__":
    sys.exit(main())

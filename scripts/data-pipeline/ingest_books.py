"""
Book ingestion → KnowledgeChunk drafts for the BeVietnam cultural KB.

Two-stage pipeline (per docs/book-ingestion-pipeline.md, adapted to the
current KnowledgeChunk schema in services/ai/common/knowledge_schema.py):

  Stage 1 — parse + chunk
    Section-aware: split a Markdown/TXT book by headings (so every chunk
    keeps its `page_or_section`), then slide ~600-char overlapping windows
    on sentence boundaries within each section.

  Stage 2 — Gemini extraction
    One JSON call per chunk. Gemini distils a single grounded cultural fact
    and maps it to a controlled place_id vocabulary (the Huế POIs + the
    national `viet-nam` bucket) and a fixed category set. Anything without
    clear cultural content is skipped.

Auto-extracted facts are NOT trusted blindly: every emitted chunk is written
with `review_status = needs_review` and `source_type = book`, matching the
project policy that book claims must be marked and human-reviewed before they
are promoted to `approved` / seeded as authoritative.

Usage (from repo root):
    PYTHONPATH=. services/backend/venv/bin/python scripts/data-pipeline/ingest_books.py \
        --book data/books/book1.md \
        --out  data/knowledge/vietnam_culture_chunks.json

    # parse/chunk only, no Gemini (offline sanity check):
    ... ingest_books.py --book data/books/book1.md --dry-run

    # cap the number of chunks sent to Gemini (quota-friendly):
    ... ingest_books.py --book data/books/book1.md --limit 20

    # also embed + upsert the drafts into Qdrant:
    ... ingest_books.py --book data/books/book1.md --seed
"""

from __future__ import annotations

import argparse
import json
import logging
import re
import sys
import unicodedata
from datetime import date
from pathlib import Path

from services.ai.common.knowledge_schema import (
    KnowledgeChunk,
    KnowledgeChunkCollection,
)
from services.ai.common.llm import llm_gateway

logger = logging.getLogger("ingest_books")

# ── Controlled vocabulary ─────────────────────────────────────────────────────
# Gemini must map every fact onto one of these places. Default is the national
# `viet-nam` bucket; a Huế POI slug is used only when the passage is clearly
# about that specific place/topic. Keep in sync with data/knowledge/hue_chunks.json.
PLACE_VOCAB: dict[str, str] = {
    "viet-nam": "Việt Nam",
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

# Source identity for this book (matches data/sources/hue_sources.json → book-co-so-vhvn).
SOURCE_TITLE = "Cơ sở văn hóa Việt Nam"
SOURCE_PUBLISHER = 'Trần Ngọc Thêm — "Cơ sở văn hóa Việt Nam"'

CHUNK_CHARS = 600
CHUNK_STEP = 400
MIN_SECTION_CHARS = 120

_HEADING_RE = re.compile(r"^#{1,6}\s+(.*\S)\s*$")
_SENTENCE_SPLIT_RE = re.compile(r"(?<=[.!?…])\s+")


# ── Stage 1: parse + chunk ────────────────────────────────────────────────────
def _is_noise(line: str) -> bool:
    """Drop OCR image refs / scan artifacts that carry no cultural text."""
    stripped = line.strip()
    return (
        stripped.startswith("![")
        or stripped.startswith("![](")
        or stripped.lower().startswith("scan to open")
        or bool(re.fullmatch(r"_page_\d+.*", stripped))
    )


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
        window = " ".join(buf).strip()
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


def _user_prompt(section: str, passage: str) -> str:
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
        "Danh sách place_id hợp lệ (mặc định viet-nam; chỉ chọn POI Huế khi đoạn "
        "nói cụ thể về nơi/chủ đề đó):\n" + places + "\n"
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
) -> KnowledgeChunk | None:
    """Call Gemini for one passage and build a validated KnowledgeChunk draft."""
    result = llm_gateway.generate_json(_SYSTEM_PROMPT, _user_prompt(section, passage))
    if not result or result.get("skip"):
        return None

    place_id = str(result.get("place_id", "viet-nam")).strip().lower()
    if place_id not in PLACE_VOCAB:
        place_id = "viet-nam"
    category = str(result.get("category", "")).strip().lower()
    if category not in CATEGORY_VOCAB:
        category = "tradition"
    fact = str(result.get("text", "")).strip()
    if len(fact) < 40:
        return None

    base_id = f"book-csvhvn-{place_id}-{_slugify(section, 24)}-{index:03d}"
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
            source_title=SOURCE_TITLE,
            publisher=SOURCE_PUBLISHER,
            page_or_section=section[:160],
            reviewed_at=date.today(),
            review_status="needs_review",
            related_poi_ids=[] if place_id == "viet-nam" else [place_id],
            tags=[str(t).strip().lower() for t in result.get("tags", []) if str(t).strip()],
            notes="auto-extracted from book1.md via Gemini; pending human review",
        )
    except Exception as exc:  # pydantic validation failure → skip this passage
        logger.warning("Chunk %s failed validation: %s", chunk_id, exc)
        return None


# ── Orchestration ─────────────────────────────────────────────────────────────
def main() -> int:
    parser = argparse.ArgumentParser(description="Ingest a book into KnowledgeChunk drafts.")
    parser.add_argument("--book", required=True, type=Path, help="Path to book .md/.txt")
    parser.add_argument(
        "--out", type=Path,
        default=Path("data/knowledge/vietnam_culture_chunks.json"),
        help="Output KnowledgeChunkCollection JSON",
    )
    parser.add_argument("--limit", type=int, default=0, help="Cap chunks sent to Gemini (0 = all)")
    parser.add_argument("--dry-run", action="store_true", help="Parse + chunk only; no Gemini")
    parser.add_argument("--seed", action="store_true", help="Embed + upsert drafts into Qdrant")
    args = parser.parse_args()

    logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")

    if not args.book.exists():
        logger.error("Book not found: %s", args.book)
        return 2

    pairs = build_chunks(args.book.read_text(encoding="utf-8"))
    logger.info("Parsed %d candidate passages from %s", len(pairs), args.book.name)
    if args.limit:
        pairs = pairs[: args.limit]
        logger.info("Limited to %d passages", len(pairs))

    if args.dry_run:
        for section, passage in pairs[:3]:
            logger.info("  [%s] %s…", section[:40], passage[:90])
        logger.info("Dry run complete — no Gemini calls made.")
        return 0

    used_ids: set[str] = set()
    chunks: list[KnowledgeChunk] = []
    attempts = empty = 0
    for i, (section, passage) in enumerate(pairs):
        attempts += 1
        chunk = extract_chunk(section, passage, i, used_ids)
        if chunk is None:
            empty += 1
        else:
            chunks.append(chunk)

    # Quota / outage guard: if Gemini produced nothing across many attempts the
    # gateway is almost certainly returning {} (e.g. 429). Do NOT write an empty
    # dataset — report and fail so the run is not mistaken for success.
    if not chunks:
        logger.error(
            "Extracted 0 chunks from %d passages. Gemini likely unavailable "
            "(quota/429) — the LLM gateway swallows errors and returns {}. "
            "Nothing written. Restore Gemini quota and re-run.",
            attempts,
        )
        return 1
    if empty:
        logger.warning("%d/%d passages produced no fact (skipped or failed).", empty, attempts)

    collection = KnowledgeChunkCollection(
        version=1,
        dataset_id="vietnam-culture-csvhvn",
        dataset_name="Cơ sở văn hóa Việt Nam — auto-extracted drafts",
        description=(
            "Auto-extracted national-culture chunks from book1.md (Trần Ngọc Thêm). "
            "review_status=needs_review until a human approves each chunk."
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

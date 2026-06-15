#!/usr/bin/env python3
"""
Book Ingestion Pipeline — BeVietnam AI Core

Converts book files (PDF / TXT / DOCX) into structured cultural knowledge
stored in Qdrant, ready for Culture Scout retrieval.

Two-stage processing per chunk:
  Stage 1 — Parse & chunk : extract raw text, split into ~600-char segments
  Stage 2 — LLM extraction: Gemini converts each chunk into one cultural fact
             {text, place_name, category, source}

New facts are assigned UUID IDs so they never overwrite existing seed data.

Usage:
    python scripts/ingest_books.py                          # scans data/books/
    python scripts/ingest_books.py --books-dir path/to/dir  # custom dir
    python scripts/ingest_books.py --file path/to/book.pdf  # single file
    python scripts/ingest_books.py --clear                  # wipe collection first
    python scripts/ingest_books.py --dry-run                # extract only, no upload
    python scripts/ingest_books.py --generate-questions     # write data/question_pool.json
    python scripts/ingest_books.py --delay 0.5              # seconds between Gemini calls
    python scripts/ingest_books.py --preview 10             # show first N extracted facts
"""

from __future__ import annotations

import argparse
import json
import logging
import re
import time
import uuid
from pathlib import Path

from dotenv import load_dotenv

load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s │ %(levelname)-5s │ %(message)s",
    datefmt="%H:%M:%S",
)
logger = logging.getLogger(__name__)

# ── Chunking config ───────────────────────────────────────────────────────────
CHUNK_SIZE = 600       # target max characters per chunk
CHUNK_OVERLAP = 80     # overlap tail kept when splitting long paragraphs
MIN_CHUNK_LEN = 60     # discard chunks shorter than this
BATCH_UPLOAD = 25      # upsert to Qdrant every N facts

# ── Gemini extraction prompt ──────────────────────────────────────────────────
_SYSTEM_PROMPT = """\
You are a Vietnamese cultural knowledge curator.
Extract exactly ONE self-contained cultural fact from the passage provided.
The fact must be accurate, grounded only in the passage, and about Vietnamese culture,
history, cuisine, architecture, tradition, nature, or art.
Preserve the original language of the text."""

def _user_prompt(chunk: str, source: str) -> str:
    return f"""\
Extract the single most important cultural fact from this passage.

PASSAGE:
{chunk}

Return a JSON object:
{{
  "text": "One clear factual sentence, max 250 chars, original language of the passage",
  "place_name": "Most specific Vietnamese location: Huế | Đà Nẵng | Hội An | Mỹ Sơn | Việt Nam | or other",
  "category": "Exactly one of: history | architecture | food | tradition | festival | nature | art | religion",
  "source": "{source}"
}}

If the passage contains no clear cultural fact about Vietnam, return: {{"skip": true}}"""


# ── File parsers ──────────────────────────────────────────────────────────────

def _parse_txt(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="replace")


def _parse_pdf(path: Path) -> str:
    try:
        import pypdf
    except ImportError:
        logger.error("pypdf not installed — run: pip install pypdf")
        return ""
    pages: list[str] = []
    with open(path, "rb") as fh:
        reader = pypdf.PdfReader(fh)
        for page in reader.pages:
            text = page.extract_text() or ""
            if text.strip():
                pages.append(text.strip())
    return "\n\n".join(pages)


def _parse_docx(path: Path) -> str:
    try:
        import docx
    except ImportError:
        logger.error("python-docx not installed — run: pip install python-docx")
        return ""
    doc = docx.Document(str(path))
    return "\n\n".join(p.text.strip() for p in doc.paragraphs if p.text.strip())


def parse_file(path: Path) -> str:
    """Return full plain text extracted from the file."""
    ext = path.suffix.lower()
    if ext == ".pdf":
        return _parse_pdf(path)
    if ext in (".txt", ".md"):
        return _parse_txt(path)
    if ext == ".docx":
        return _parse_docx(path)
    logger.warning("Unsupported file type %s — skipping %s", ext, path.name)
    return ""


# ── Text chunker ──────────────────────────────────────────────────────────────

def chunk_text(text: str) -> list[str]:
    """
    Split text into overlapping chunks that stay within CHUNK_SIZE.

    Strategy:
      1. Split on blank lines (paragraph boundaries).
      2. Further split long paragraphs on sentence-ending punctuation.
      3. Merge short sentences into chunks up to CHUNK_SIZE.
         Keep an CHUNK_OVERLAP-char tail as overlap context.
    """
    paragraphs = [p.strip() for p in re.split(r"\n{2,}", text) if p.strip()]

    sentences: list[str] = []
    for para in paragraphs:
        if len(para) <= CHUNK_SIZE:
            sentences.append(para)
        else:
            # Split on Vietnamese / English sentence boundaries
            parts = re.split(r"(?<=[.!?।])\s+", para)
            sentences.extend(p.strip() for p in parts if p.strip())

    chunks: list[str] = []
    current = ""
    for sent in sentences:
        if not current:
            current = sent
        elif len(current) + 1 + len(sent) <= CHUNK_SIZE:
            current = current + " " + sent
        else:
            if len(current) >= MIN_CHUNK_LEN:
                chunks.append(current)
            # Carry overlap into next chunk for context continuity
            overlap_start = max(0, len(current) - CHUNK_OVERLAP)
            current = current[overlap_start:] + " " + sent

    if current and len(current) >= MIN_CHUNK_LEN:
        chunks.append(current)

    return chunks


# ── LLM extractor ─────────────────────────────────────────────────────────────

class FactExtractor:
    """Uses Gemini to convert raw text chunks into structured cultural facts."""

    def __init__(self, api_key: str) -> None:
        import os
        from google import genai
        # Remove GOOGLE_API_KEY so the SDK doesn't override our explicit key
        os.environ.pop("GOOGLE_API_KEY", None)
        self._client = genai.Client(api_key=api_key)

    def extract(self, chunk: str, source: str) -> dict | None:
        """
        Extract one cultural fact from a chunk.
        Returns {text, place_name, category, source} or None if skipped/failed.
        """
        from google.genai import types

        try:
            response = self._client.models.generate_content(
                model="gemini-2.5-flash",
                contents=_user_prompt(chunk, source),
                config=types.GenerateContentConfig(
                    system_instruction=_SYSTEM_PROMPT,
                    response_mime_type="application/json",
                    temperature=0.2,  # factual extraction — keep it deterministic
                ),
            )
            result: dict = json.loads(response.text.strip())
        except Exception as exc:
            logger.debug("Gemini call failed: %s", exc)
            return None

        if result.get("skip"):
            return None

        # Validate required fields
        if not all(result.get(k) for k in ("text", "place_name", "category", "source")):
            return None

        return {
            "text": result["text"][:300],
            "place_name": result["place_name"],
            "category": result["category"],
            "source": result["source"],
        }


# ── Qdrant uploader ───────────────────────────────────────────────────────────

def upload_facts(facts: list[dict]) -> int:
    """
    Embed and upsert facts to Qdrant with UUID point IDs.
    UUID IDs prevent collisions with the integer IDs used by seed_qdrant.py.
    """
    from qdrant_client.models import PointStruct
    from src.bevietnam.ai.common.qdrant_store import qdrant_store
    from src.bevietnam.ai.common.config import settings

    texts = [f["text"] for f in facts]
    embeddings = qdrant_store.embedding_model.encode(texts, show_progress_bar=False)

    points = [
        PointStruct(
            id=str(uuid.uuid4()),
            vector=emb.tolist(),
            payload={
                "text": fact["text"],
                "place_name": fact["place_name"],
                "category": fact["category"],
                "source": fact["source"],
            },
        )
        for fact, emb in zip(facts, embeddings)
    ]

    qdrant_store.client.upsert(
        collection_name=settings.qdrant_collection,
        points=points,
    )
    return len(points)


# ── Per-file pipeline ─────────────────────────────────────────────────────────

def ingest_file(
    path: Path,
    extractor: FactExtractor,
    delay: float,
    dry_run: bool,
    preview_remaining: list[int],  # mutable counter [N] — shows first N facts
) -> tuple[int, int, int, list[dict]]:
    """
    Process one book file.
    Returns (chunks_total, facts_extracted, facts_uploaded).
    """
    logger.info("📖  %s", path.name)

    raw = parse_file(path)
    if not raw.strip():
        logger.warning("    No text extracted — skipping")
        return 0, 0, 0, []

    chunks = chunk_text(raw)
    logger.info("    %d chars → %d chunks", len(raw), len(chunks))

    source = path.stem
    batch: list[dict] = []
    facts: list[dict] = []
    extracted = 0
    uploaded = 0

    for i, chunk in enumerate(chunks, 1):
        fact = extractor.extract(chunk, source)

        if fact is not None:
            extracted += 1
            batch.append(fact)
            facts.append(fact)

            # Show preview facts
            if preview_remaining[0] > 0:
                preview_remaining[0] -= 1
                print(
                    f"\n  ── PREVIEW FACT ──────────────────────────────────\n"
                    f"  text      : {fact['text']}\n"
                    f"  place     : {fact['place_name']}\n"
                    f"  category  : {fact['category']}\n"
                    f"  source    : {fact['source']}\n"
                )

        # Batch upload
        if not dry_run and len(batch) >= BATCH_UPLOAD:
            n = upload_facts(batch)
            uploaded += n
            logger.info("    [%d/%d] uploaded batch of %d", i, len(chunks), n)
            batch.clear()

        if delay > 0 and i < len(chunks):
            time.sleep(delay)

    # Upload remainder
    if not dry_run and batch:
        n = upload_facts(batch)
        uploaded += n

    logger.info(
        "    ✅  %d extracted, %d skipped, %d uploaded",
        extracted, len(chunks) - extracted, uploaded,
    )
    return len(chunks), extracted, uploaded, facts


def write_question_pool(
    facts: list[dict],
    output_path: Path,
    place_name: str,
    language: str,
    max_questions: int,
) -> int:
    """Generate reusable questions from facts and write them as backend JSON."""
    from src.bevietnam.ai.agents.question_pool_maker import QuestionPoolMaker

    maker = QuestionPoolMaker()
    questions = maker.generate(
        facts=facts,
        place_name=place_name,
        language=language,
        max_questions=max_questions,
    )
    output_path.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        "version": 1,
        "source": "book_ingestion",
        "place_name": place_name,
        "language": language,
        "facts_count": len(facts),
        "questions": questions,
    }
    output_path.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    return len(questions)


# ── Entry point ───────────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(
        description="Ingest book files into Qdrant for BeVietnam AI Core"
    )
    parser.add_argument(
        "--books-dir", default="data/books",
        help="Directory containing PDF/TXT/DOCX files (default: data/books)",
    )
    parser.add_argument("--file", help="Process a single file")
    parser.add_argument(
        "--clear", action="store_true",
        help="Delete and recreate the Qdrant collection before ingesting",
    )
    parser.add_argument(
        "--dry-run", action="store_true",
        help="Extract facts with Gemini but do NOT upload to Qdrant",
    )
    parser.add_argument(
        "--delay", type=float, default=1.0,
        help="Seconds to wait between Gemini calls (default: 1.0)",
    )
    parser.add_argument(
        "--preview", type=int, default=0, metavar="N",
        help="Print the first N extracted facts to stdout",
    )
    parser.add_argument(
        "--generate-questions",
        action="store_true",
        help="Generate a reusable question pool JSON from extracted facts",
    )
    parser.add_argument(
        "--question-pool-out",
        default="data/question_pool.json",
        help="Question pool output path (default: data/question_pool.json)",
    )
    parser.add_argument(
        "--place-name",
        default="",
        help="Optional default place name for generated questions",
    )
    parser.add_argument(
        "--language",
        default="vi",
        choices=["vi", "en"],
        help="Question language (default: vi)",
    )
    parser.add_argument(
        "--max-questions",
        type=int,
        default=40,
        help="Maximum questions to generate when --generate-questions is used",
    )
    args = parser.parse_args()

    # ── Validate env ──────────────────────────────────────────────────────────
    from src.bevietnam.ai.common.config import settings

    if not settings.gemini_api_key:
        logger.error("GEMINI_API_KEY not set. Add it to .env or environment.")
        return

    # ── Collect files ─────────────────────────────────────────────────────────
    if args.file:
        files = [Path(args.file)]
    else:
        books_dir = Path(args.books_dir)
        if not books_dir.exists():
            logger.error("Books directory not found: %s", books_dir)
            logger.info("Create it and drop your files there:  mkdir -p %s", books_dir)
            return
        files = sorted(
            f for f in books_dir.iterdir()
            if f.suffix.lower() in (".pdf", ".txt", ".md", ".docx")
            and not f.name.startswith(".")
        )

    if not files:
        logger.warning("No book files found in %s (PDF / TXT / DOCX).", args.books_dir)
        return

    logger.info("Files to process: %s", [f.name for f in files])

    # ── Prepare Qdrant ────────────────────────────────────────────────────────
    if not args.dry_run:
        from src.bevietnam.ai.common.qdrant_store import qdrant_store

        if args.clear:
            logger.warning("--clear: deleting collection '%s'", settings.qdrant_collection)
            try:
                qdrant_store.client.delete_collection(settings.qdrant_collection)
            except Exception:
                pass
        qdrant_store.ensure_collection()

    # ── Run pipeline ──────────────────────────────────────────────────────────
    extractor = FactExtractor(api_key=settings.gemini_api_key)
    preview_counter = [args.preview]  # mutable list so ingest_file can decrement it

    total_chunks = total_extracted = total_uploaded = 0
    all_facts: list[dict] = []

    for file_path in files:
        c, e, u, facts = ingest_file(
            path=file_path,
            extractor=extractor,
            delay=args.delay,
            dry_run=args.dry_run,
            preview_remaining=preview_counter,
        )
        total_chunks += c
        total_extracted += e
        total_uploaded += u
        all_facts.extend(facts)

    questions_written = 0
    if args.generate_questions and all_facts:
        questions_written = write_question_pool(
            facts=all_facts,
            output_path=Path(args.question_pool_out),
            place_name=args.place_name,
            language=args.language,
            max_questions=args.max_questions,
        )
        logger.info(
            "Generated %d question-pool items at %s",
            questions_written,
            args.question_pool_out,
        )

    # ── Summary ───────────────────────────────────────────────────────────────
    print(
        f"\n{'─'*50}\n"
        f"  Files processed : {len(files)}\n"
        f"  Total chunks    : {total_chunks}\n"
        f"  Facts extracted : {total_extracted}  "
        f"({100 * total_extracted // total_chunks if total_chunks else 0}% yield)\n"
        f"  Facts skipped   : {total_chunks - total_extracted}\n"
        f"  Facts uploaded  : {'(dry-run)' if args.dry_run else total_uploaded}\n"
        f"  Questions       : {questions_written}\n"
        f"{'─'*50}"
    )

    if not args.dry_run and total_uploaded:
        logger.info(
            "Collection '%s' now has %d new facts ready for Culture Scout.",
            settings.qdrant_collection, total_uploaded,
        )


if __name__ == "__main__":
    main()

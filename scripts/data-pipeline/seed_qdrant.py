"""
Seed Qdrant with the curated Huế knowledge chunks (one-time build).

Reads knowledge-chunk JSON files, validates each chunk against KnowledgeChunk,
embeds them via the configured embedder (HF bge-m3) and upserts into the Qdrant
collection. After this, Culture Scout retrieves real grounded facts instead of
the hardcoded fallback set.

Qdrant target comes from services/ai/common/config.py (.env):
  - Qdrant Cloud      QDRANT_CLUSTER_ENDPOINT + QDRANT_API_KEY
  - Embedded on-disk  QDRANT_PATH        (no server, fully local)
  - Local server      QDRANT_HOST/PORT
Embedding needs HF_TOKEN.

Usage (from repo root):
  PYTHONPATH=. services/backend/venv/bin/python scripts/data-pipeline/seed_qdrant.py
  PYTHONPATH=. ... seed_qdrant.py --recreate          # drop + rebuild collection
  PYTHONPATH=. ... seed_qdrant.py --include-books      # also load book chunks
  PYTHONPATH=. ... seed_qdrant.py data/knowledge/hue_chunks.json [more.json ...]
"""

import argparse
import json
import sys
from pathlib import Path

from services.ai.common.config import settings
from services.ai.common.knowledge_schema import KnowledgeChunk
from services.ai.common.qdrant_store import qdrant_store

DEFAULT_CHUNKS = "data/knowledge/hue_chunks.json"
BOOK_CHUNKS = "data/knowledge/hue_book_chunks.json"


def load_valid_chunks(path: str) -> tuple[list[dict], int]:
    """Return (valid raw-chunk dicts, skipped count). Invalid chunks are skipped,
    not fatal — book-derived chunks can exceed the 900-char text bound."""
    payload = json.loads(Path(path).read_text(encoding="utf-8"))
    raw_chunks = payload["chunks"] if isinstance(payload, dict) else payload

    valid: list[dict] = []
    skipped = 0
    for raw in raw_chunks:
        try:
            KnowledgeChunk.model_validate(raw)  # validate only; keep the raw dict
            valid.append(raw)
        except Exception as exc:
            skipped += 1
            cid = raw.get("chunk_id", "<no-id>")
            print(f"  skip {cid}: {str(exc).splitlines()[0][:120]}")
    return valid, skipped


def main() -> int:
    parser = argparse.ArgumentParser(description="Seed Qdrant with knowledge chunks.")
    parser.add_argument("paths", nargs="*", help="Chunk JSON files (default: curated Huế chunks).")
    parser.add_argument("--include-books", action="store_true", help=f"Also load {BOOK_CHUNKS}.")
    parser.add_argument("--recreate", action="store_true", help="Drop and recreate the collection first.")
    args = parser.parse_args()

    paths = list(args.paths) or [DEFAULT_CHUNKS]
    if args.include_books and BOOK_CHUNKS not in paths:
        paths.append(BOOK_CHUNKS)

    print(f"Qdrant collection : {settings.qdrant_collection}")
    print(f"Embedding model   : {settings.embedding_model_name} ({settings.embedding_dimension}d)")
    print(f"Source files      : {', '.join(paths)}")

    documents: list[dict] = []
    total_skipped = 0
    for path in paths:
        if not Path(path).exists():
            print(f"ERROR: {path} not found", file=sys.stderr)
            return 1
        valid, skipped = load_valid_chunks(path)
        total_skipped += skipped
        print(f"  {path}: {len(valid)} valid, {skipped} skipped")
        documents.extend(valid)

    if not documents:
        print("ERROR: no valid chunks to seed", file=sys.stderr)
        return 1

    if args.recreate:
        try:
            qdrant_store.client.delete_collection(settings.qdrant_collection)
            print(f"Dropped existing collection: {settings.qdrant_collection}")
        except Exception:
            pass  # collection may not exist yet

    qdrant_store.ensure_collection()
    count = qdrant_store.upsert_documents(documents)

    print(f"\nDone. Seeded {count} chunks ({total_skipped} skipped) into "
          f"'{settings.qdrant_collection}'.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

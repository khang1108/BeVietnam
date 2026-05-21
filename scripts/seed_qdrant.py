"""
Seed Qdrant — Load cultural documents about Huế and Hội An.

This script reads cultural facts from hue_cultural_data.json,
embeds them using BAAI/bge-m3, and uploads to Qdrant.

Run via Docker:
    docker compose run --rm seed-qdrant

The bge-m3 model (~2.2 GB) is downloaded on first run.
"""

import json
import sys
from pathlib import Path

# When running inside Docker, PYTHONPATH=/app
# so `ai.common` is importable directly
from ai.common.qdrant_store import qdrant_store


def main() -> None:
    """Load cultural documents and seed Qdrant."""

    # ── Load cultural data ────────────────────────────────────────────────────
    # In Docker: /app/scripts/hue_cultural_data.json
    data_path = Path(__file__).parent / "hue_cultural_data.json"
    if not data_path.exists():
        print(f"❌ Data file not found: {data_path}")
        sys.exit(1)

    with open(data_path, encoding="utf-8") as f:
        documents = json.load(f)

    print(f"📚 Loaded {len(documents)} cultural documents")

    # ── Ensure Qdrant collection exists ───────────────────────────────────────
    print("🔧 Ensuring Qdrant collection exists...")
    qdrant_store.ensure_collection()

    # ── Embed and upload ──────────────────────────────────────────────────────
    print("🧠 Embedding documents with bge-m3 (this may take a moment)...")
    count = qdrant_store.upsert_documents(documents)

    print(f"✅ Successfully seeded {count} documents into Qdrant!")
    print(f"   Collection: cultural_knowledge")
    print(f"   Ready for Culture Scout retrieval.")


if __name__ == "__main__":
    main()

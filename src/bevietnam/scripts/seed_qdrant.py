"""
Seed Qdrant — Load cultural documents about Huế and Hội An.

This script reads cultural facts from hue_cultural_data.json,
embeds them using BAAI/bge-m3, and uploads to Qdrant.

Run without Docker, using embedded Qdrant on disk:
    bevietnam/bin/python src/bevietnam/scripts/seed_qdrant.py

Run with an explicit embedded Qdrant path:
    bevietnam/bin/python src/bevietnam/scripts/seed_qdrant.py --qdrant-path data/qdrant

Run via Docker:
    docker compose run --rm seed-qdrant

The bge-m3 model (~2.2 GB) is downloaded on first run.
"""

import argparse
import json
import os
import sys
from pathlib import Path


def _bootstrap_paths() -> Path:
    """Make both `ai...` and `src.bevietnam...` imports work when run as a file."""
    current_file = Path(__file__).resolve()
    package_root = current_file.parents[1]  # .../src/bevietnam
    repo_root = current_file.parents[3]     # project root

    for path in (repo_root, package_root):
        path_str = str(path)
        if path_str not in sys.path:
            sys.path.insert(0, path_str)

    return repo_root


REPO_ROOT = _bootstrap_paths()


def _parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Seed cultural documents into Qdrant.")
    parser.add_argument(
        "--qdrant-path",
        default=None,
        help="Use embedded local Qdrant at this path. No Docker/server needed.",
    )
    parser.add_argument(
        "--use-server",
        action="store_true",
        help="Use QDRANT_HOST/QDRANT_PORT instead of the embedded local default.",
    )
    return parser.parse_args()


def _configure_qdrant(args: argparse.Namespace) -> None:
    if args.qdrant_path:
        qdrant_path = Path(args.qdrant_path).expanduser()
        if not qdrant_path.is_absolute():
            qdrant_path = REPO_ROOT / qdrant_path
        os.environ["QDRANT_PATH"] = str(qdrant_path)
        return

    if args.use_server:
        return

    has_cloud = os.environ.get("QDRANT_CLUSTER_ENDPOINT") and os.environ.get("QDRANT_API_KEY")
    has_server = os.environ.get("QDRANT_HOST")
    has_embedded = os.environ.get("QDRANT_PATH")

    if not has_cloud and not has_server and not has_embedded:
        os.environ["QDRANT_PATH"] = str(REPO_ROOT / "data" / "qdrant")


def main() -> None:
    """Load cultural documents and seed Qdrant."""
    args = _parse_args()
    _configure_qdrant(args)

    # Import after configuring env vars because settings are loaded at import time.
    from ai.common.qdrant_store import qdrant_store

    # ── Load cultural data ────────────────────────────────────────────────────
    # In Docker: /app/scripts/hue_cultural_data.json
    data_path = Path(__file__).parent / "hue_cultural_data.json"
    if not data_path.exists():
        print(f"Data file not found: {data_path}")
        sys.exit(1)

    with open(data_path, encoding="utf-8") as f:
        documents = json.load(f)

    print(f"Loaded {len(documents)} cultural documents")

    # ── Ensure Qdrant collection exists ───────────────────────────────────────
    print("Ensuring Qdrant collection exists...")
    qdrant_store.ensure_collection()

    # ── Embed and upload ──────────────────────────────────────────────────────
    print("Embedding documents with bge-m3 (this may take a moment)...")
    count = qdrant_store.upsert_documents(documents)

    print(f"   Successfully seeded {count} documents into Qdrant!")
    print(f"   Collection: cultural_knowledge")
    print(f"   Ready for Culture Scout retrieval.")


if __name__ == "__main__":
    main()

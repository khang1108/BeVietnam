"""
Validate a curated knowledge dataset against the KnowledgeChunk schema, and
cross-check that each chunk's source traces to an approved source in the Huế
source registry.

Usage:
    PYTHONPATH=. services/backend/venv/bin/python \
        scripts/data-pipeline/validate_knowledge_chunks.py \
        data/knowledge/hue_chunks.json data/sources/hue_sources.json
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

from services.ai.common.knowledge_schema import parse_knowledge_payload

_SOURCE_ID_RE = re.compile(r"source_id:\s*([a-z0-9-]+)")


def _load(path: str):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def validate(chunks_path: str, registry_path: str | None = None) -> int:
    chunks = parse_knowledge_payload(_load(chunks_path))
    print(f"OK: {len(chunks)} chunks parsed and schema-valid")

    place_ids = sorted({c.place_id for c in chunks})
    multi = [pid for pid in place_ids if sum(c.place_id == pid for c in chunks) >= 2]
    print(f"   {len(place_ids)} POIs ({len(multi)} with 2+ chunks)")

    errors: list[str] = []
    if registry_path:
        registry = _load(registry_path)
        approved = {
            s["source_id"]
            for s in registry.get("sources", [])
            if s.get("review_status") == "approved"
        }
        for chunk in chunks:
            match = _SOURCE_ID_RE.search(chunk.notes or "")
            if not match:
                errors.append(f"{chunk.chunk_id}: no source_id in notes")
            elif match.group(1) not in approved:
                errors.append(
                    f"{chunk.chunk_id}: source_id '{match.group(1)}' not approved in registry"
                )
        if not errors:
            print(f"   all chunks trace to an approved source ({len(approved)} approved)")

    for err in errors:
        print(f"FAIL {err}")
    return 1 if errors else 0


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(2)
    sys.exit(validate(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else None))

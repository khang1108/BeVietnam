# Huế Cultural Sources — Registry

`hue_sources.json` is the **vetted-before-extraction** list of authoritative
sources for Huế culture. It is the provenance ledger for the knowledge base:
every `KnowledgeChunk` later seeded into Qdrant must trace back to a `source_id`
here, and the chunk's `source_*` fields must match this entry.

## Workflow

```
hue_sources.json (needs_review)
        │  ← human review: verify URL, publisher, coverage, license
        ▼
hue_sources.json (approved)
        │  ← extract text → chunk → fill KnowledgeChunk (knowledge_schema.py)
        ▼
data/knowledge/hue_chunks.json  → validate → seed Qdrant
```

Extraction does **not** start until an entry's `review_status` is `approved`.

## Entry fields

| Field | Meaning |
|-------|---------|
| `source_id` | Stable slug; referenced from chunk metadata. |
| `publisher` | Authoritative org / author name. |
| `url` | Canonical page (or local path for `book` files). |
| `source_type` | `official` \| `unesco` \| `book`. Book claims are marked separately in chunk metadata per source policy. |
| `language` | `vi` \| `en` \| `both`. |
| `covers` | Topics / places this source can ground. |
| `place_ids` | Related POI slugs (match the chunk `place_id`). |
| `authority_note` | Why this source is trustworthy / scope limits. |
| `license_note` | Reuse constraints — read before quoting. |
| `review_status` | `needs_review` → `approved` (gate for extraction). |
| `acquired` | (books) whether the file is in `data/books/`. |

## Current sources (summary)

- **official:** Hue Monuments Conservation Centre (`hueworldheritage.org.vn`),
  Vietnam National Authority of Tourism, Hue Dept. of Tourism (`visithue.vn`),
  Hue city portal (`hue.gov.vn`), National Archives of Vietnam (Châu bản).
- **unesco:** Complex of Hué Monuments (WH 678, 1993), Nhã nhạc (ICH 00074,
  2008/2003), Memory of the World (Châu bản 2017; Hue royal-architecture poetry
  2016 — per-element URLs to pin during review).
- **book:** *Cơ sở văn hóa Việt Nam* (Trần Ngọc Thêm, in repo); recommended but
  not-yet-acquired *Kiến trúc Cố đô Huế* (Phan Thuận An) and *Đại Nam nhất thống
  chí* (Quốc sử quán triều Nguyễn).

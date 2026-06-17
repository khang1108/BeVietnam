# Knowledge Base Strategy — Quests, Quest-Chains & Spotlight Posts

**Status:** plan (awaiting approval) · **Date:** 2026-06-17 · **Pilot:** Huế

This is the strategy + task breakdown for turning the source material (4 OCR'd
culture books + approved government/UNESCO web sources) into **one curated
knowledge base** that grounds three products: the **recommendation feed**
(spotlight posts), **storyline quests/quest-chains**, and the **bubble map**.

---

## 1. Decisions locked (2026-06-17)

| Fork | Decision | Consequence |
|------|----------|-------------|
| What is a "post"? | **Authored spotlights** — a new stored content type | Add `SpotlightPost` model + dataset + feed-surfacing logic |
| Book extraction engine | **Fully automated vLLM** (Qwen2.5-14B @ `api.iamphuckhang.dev`) | No human approval gate; all book chunks stay `review_status=needs_review` |
| Book scope | **2 Huế primary + 2 national supplementary** | Huế books → `hue-*` POIs; national books → `viet-nam` + `related_poi_ids` cross-refs |

---

## 2. Architecture — one KB, three products

```
RAW SOURCES                      CURATED KB (grounding)            PRODUCTS
─────────────                    ───────────────────────           ────────────────────
data/books/*.md (4 books) ─┐
                           ├──►  KnowledgeChunk[]  ──► Qdrant ──┬─► Feed: SpotlightPost[]
gov / UNESCO web ──────────┘     (POI-bound, source-traced,     │      (authored, condition-tagged)
                                  review_status, → bge-m3)       │
                                                                 ├─► Quests: question_pool.json
                                                                 │      (storyline tasks, grounded)
                                                                 │
                                                                 └─► Bubble map (same scores as feed)
```

**The single grounding rule:** every product artifact (a spotlight post, a quest
task, a feed explanation) must trace to ≥1 `KnowledgeChunk`, which in turn traces
to a `SourceRef`. Nothing user-facing is ungrounded.

### Existing foundation (reused, not rebuilt)
- `KnowledgeChunk` / `KnowledgeChunkCollection` schema — POI-bound, source-traced.
- `hue_chunks.json` (17 chunks / 11 POIs) + `hue_sources.json` (11 approved sources).
- Trip Advisor scoring (`scoring.py`) — deterministic suitability/culture scores in code.
- `data/question_pool.json` — 2 reviewed Huế storylines (seed for quest expansion).
- vLLM endpoint (`api.iamphuckhang.dev/v1`, OpenAI-compatible) — the extraction/generation engine.

### What's new
- **`SpotlightPost`** content type (the "post"): `{post_id, place_id, title, body,
  cultural_hook, weather_tags, time_tags, season_tags, source_refs, review_status, lang}`.
  Stored in `data/posts/hue_spotlights.json`; surfaced by the feed when current
  conditions match its tags, then ranked by the existing suitability score.
- **vLLM-backed ingestion + generation** replacing the dead Gemini path.

---

## 3. Dependency graph

```
SpotlightPost schema ─┐
POI registry/vocab  ──┤
OCR clean + chunker ──┴─► vLLM extract ─► KnowledgeChunk JSON ─► seed Qdrant ─┬─► Spotlight gen ─► feed surfacing
                                                                              └─► Quest gen ─► question_pool
gov-web crawl ───────────────────────────► KnowledgeChunk JSON ─┘ (joins Qdrant seed)
```

Build order is bottom-up: schema + chunker → KB → Qdrant → products.

---

## 4. Task breakdown

### Phase 0 — Foundation (schema + vocab)

#### Task 0.1: `SpotlightPost` schema + dataset wrapper
**Description:** New Pydantic content type for authored spotlight posts, mirroring
the `KnowledgeChunk`/`Collection` pattern (slug ids, lang vi|en, review_status,
source_refs, condition tags).
**Acceptance criteria:**
- [ ] `SpotlightPost` + `SpotlightPostCollection` validate a hand-written sample post.
- [ ] `source_refs` non-empty; `review_status` defaults `needs_review`.
**Verification:** `python -c "..."` validates a sample; unique `post_id` enforced.
**Dependencies:** None · **Files:** `services/ai/common/post_schema.py`, sample JSON · **Scope:** S

#### Task 0.2: POI registry + book→place mapping vocab
**Description:** Make the place vocabulary explicit and single-sourced: the 11 Huế
`hue-*` POIs + `viet-nam` pseudo-POI, with the rule that Huế books map to `hue-*`
and national books map to `viet-nam` + `related_poi_ids`.
**Acceptance criteria:**
- [ ] One module/JSON lists every valid `place_id`, name (vi/en), lat/lng, category.
- [ ] Ingestion + spotlight + quest pipelines import this, not hardcoded lists.
**Verification:** existing `hue_chunks.json` place_ids all resolve against the registry.
**Dependencies:** None · **Files:** `data/registry/hue_pois.json`, loader · **Scope:** S

### Checkpoint: Foundation — schemas validate, registry resolves existing data.

### Phase 1 — Book ingestion (books → KB chunks, automated vLLM)

#### Task 1.1: OCR-clean + section-aware chunker
**Description:** Strip `{N}---` page markers, `![](_page_…)` image lines, and fix
common OCR diacritic noise; chunk by heading/section into retrieval-sized windows.
**Acceptance criteria:**
- [ ] Running on `Co-do-hue-xua-va-nay.md` yields clean text chunks, no markers/image lines.
- [ ] Chunk size within embedding limits; sentence boundaries preserved.
**Verification:** `--dry-run` prints sample cleaned chunks for manual eyeball.
**Dependencies:** None · **Files:** extend `scripts/data-pipeline/ingest_books.py` · **Scope:** M

#### Task 1.2: Point extraction at vLLM (replace Gemini)
**Description:** Swap the dead Gemini call for the OpenAI client against
`api.iamphuckhang.dev/v1` (Qwen2.5-14B), JSON-mode extraction → `KnowledgeChunk(needs_review)`.
**Acceptance criteria:**
- [ ] Extracts valid `KnowledgeChunk`s from a 5-passage `--limit` run.
- [ ] Schema-invalid model output is dropped + counted, never written.
**Verification:** `--dry-run --limit 5` against live vLLM returns validated chunks.
**Dependencies:** 1.1 · **Files:** `ingest_books.py`, vLLM client helper · **Scope:** M

#### Task 1.3: Ingest 2 Huế books → `hue_book_chunks.json`
**Acceptance criteria:**
- [ ] Validator passes; chunks distributed across `hue-*` POIs; deduped.
- [ ] All `source_type=book`, `review_status=needs_review`, page_or_section set.
**Verification:** `validate_knowledge_chunks.py` clean; spot-check 10 chunks read sensibly.
**Dependencies:** 1.2 · **Files:** `data/knowledge/hue_book_chunks.json` · **Scope:** M

#### Task 1.4: Ingest 2 national books → `vietnam_culture_chunks.json`
**Acceptance criteria:**
- [ ] `place_id=viet-nam` with `related_poi_ids` cross-refs to relevant `hue-*` POIs.
- [ ] Validator passes; food book chunks tagged for cuisine POIs.
**Verification:** validator clean; cross-refs resolve against the registry.
**Dependencies:** 1.2, 0.2 · **Files:** `data/knowledge/vietnam_culture_chunks.json` · **Scope:** M

### Checkpoint: KB — all chunk datasets validate, place_ids resolve, dedup clean.

### Phase 2 — Seed Qdrant (make grounding live)

#### Task 2.1: Embed + upsert all chunks to Qdrant
**Description:** Embed every approved/needs_review chunk (HF bge-m3) and upsert to
the `cultural_knowledge` collection so Culture Scout retrieves real data.
**Acceptance criteria:**
- [ ] Qdrant point count = total chunks; payloads preserve all fields.
- [ ] `CultureScout.retrieve` returns book/web chunks, not the 7 hardcoded fallbacks.
**Verification:** retrieval smoke test on a Huế query returns sourced chunks.
**Dependencies:** 1.3, 1.4 · **Files:** seed script (reuse `qdrant_store`) · **Scope:** M

### Checkpoint: Retrieval grounded end-to-end.

### Phase 3 — Government / UNESCO web track

#### Task 3.1: Crawl approved sources → `KnowledgeChunk(official|unesco)`
**Description:** Fetch the 8 official/UNESCO sources in `hue_sources.json`, extract
chunks via the same pipeline; these require `source_url` and may be `approved`.
**Acceptance criteria:**
- [ ] Chunks validate with `source_url`; `source_type` official/unesco.
- [ ] Merged into Qdrant alongside book chunks.
**Verification:** validator clean; retrieval mixes web + book sources.
**Dependencies:** 1.2, 2.1 · **Files:** crawl script (reuse `crawl.ipynb` logic), chunk JSON · **Scope:** M

### Phase 4 — Spotlight posts (the new feed product)

#### Task 4.1: Spotlight generation pipeline
**Description:** Per POI, retrieve top chunks → vLLM composes a `SpotlightPost`
(title, body, cultural_hook) and assigns `weather/time/season` tags → `needs_review` JSON.
**Acceptance criteria:**
- [ ] Generates valid posts for ≥3 POIs, each citing ≥1 chunk in `source_refs`.
- [ ] Condition tags drawn from a controlled vocab (sunny/rainy/hot; morning/evening; etc.).
**Verification:** `SpotlightPostCollection` validates output; spot-check reads well.
**Dependencies:** 0.1, 2.1 · **Files:** `scripts/data-pipeline/generate_spotlights.py`, `data/posts/hue_spotlights.json` · **Scope:** M

#### Task 4.2: Feed surfacing of spotlights
**Description:** Backend feed selects spotlight posts whose condition tags match the
current weather/time/season, then ranks by the existing suitability score.
**Acceptance criteria:**
- [ ] `GET /api/v1/feed` returns condition-matched posts (rainy → indoor-tagged surfaces).
- [ ] Falls back to score-only cards when no post matches; never empty on provider failure.
**Verification:** feed test with stubbed weather contexts returns expected posts.
**Dependencies:** 4.1 · **Files:** `feed` service + schema glue · **Scope:** M

### Phase 5 — Quests from the KB

#### Task 5.1: Grounded quest generation → expand `question_pool.json`
**Description:** Per POI retrieve chunks → vLLM composes storyline tasks →
SafetyKeeper validates (≥1 source_ref, length/safety) → append to the reviewed pool.
**Acceptance criteria:**
- [ ] New storylines added; every task cites ≥1 chunk; SafetyKeeper passes all.
- [ ] `question_pool_service.load_pool()` loads the expanded pool (fallback=False).
**Verification:** pool loads; task→chunk citations resolve.
**Dependencies:** 2.1 · **Files:** `data/question_pool.json`, generation script · **Scope:** M

### Checkpoint: Complete — feed shows condition-matched grounded posts; quests grounded; map uses same scores.

---

## 5. Risks & mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Automated extraction quality** (no human gate, OCR noise) | Med-High | Keep `review_status=needs_review`; schema-validate + dedup; spot-check a sample per book; never auto-promote book claims to `approved` |
| **vLLM availability** (single tunnel, shared with EXACT) | High | Run ingestion in batches; only one tunnel consumer at a time; cache extracted JSON so re-runs don't re-hit the LLM |
| **Qdrant / HF reachability in sandbox** | Med | Pipelines write JSON first; Qdrant seed is a separate idempotent step; fallback facts remain until seeded |
| **Copyright** (book content) | Med | `source_type=book`, quote sparingly with `page_or_section`; do not redistribute PDFs; chunks are short facts, not passages |
| **Schema drift** (thin `quest.py` DB model vs rich pool/contract) | Med | Treat `question_pool.json` as the authoring source of truth; backend persistence maps a subset — out of scope here, flag for backend team |
| **Large binaries already in git history** (~42M PDFs) | Low | Note only; removal needs history rewrite — decide separately |

---

## 6. Open questions for the human

1. **Spotlight condition vocab** — confirm the controlled tag sets (weather: sunny/
   cloudy/rainy/hot/cool; time: morning/midday/evening/night; season: spring/summer/
   autumn/winter or dry/wet). Affects feed matching.
2. **Approved vs needs_review for the pilot** — fully-automated means book chunks
   ship as `needs_review`. Is that acceptable for the graded pilot's displayed cultural
   claims, or do you want at least a light spot-check pass before the pilot demo?
3. **Quest persistence** — do we wire the rich `question_pool.json` into the thin
   `quest.py` DB tables now, or is that the backend team's task 8?
4. **gov-web crawl scope** — all 8 official/UNESCO sources, or a subset for the pilot?

---

## 7. Parallelization

- **Sequential (must):** 0.x → 1.x → 2.1 (schema → chunks → Qdrant).
- **Parallel after 2.1:** Phase 3 (gov web), Phase 4 (spotlights), Phase 5 (quests)
  are independent product tracks that all consume the same seeded KB.

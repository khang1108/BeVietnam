# AI Service Implementation Plan: BeVietnam Hue Pilot

## Summary

Build the AI service as the cultural intelligence layer for the Hue pilot. It will own grounded cultural retrieval, recommendation scoring plus explanations, and storyline task generation, while keeping all outputs deterministic enough to test and reliable enough for a real tourist pilot.

Key decisions:

- AI service owns score plus explanation, but the LLM does not freely invent numeric scores.
- Runtime storyline uses prebuilt reviewed task pools plus runtime selection, with live generation only as fallback.
- Knowledge base uses official sources plus approved books, with source metadata required on every cultural claim.

## Architecture Decisions

- Use the current stack: FastAPI, LangGraph, Gemini, Qdrant, and Hugging Face embeddings.
- Repair current broken imports first so `services.ai.main:app` runs directly from the monorepo.
- Use managed PostgreSQL as the production/pilot product database. Local PostgreSQL or SQLite-style local storage is development-only, not the pilot source of truth.
- Use Firebase for authentication and media storage:
  - Firebase Authentication verifies tourist identity.
  - Firebase Storage stores check-in/capture photos.
  - Backend verifies Firebase ID tokens before writing protected user data.
- Keep database responsibilities separated:
  - PostgreSQL stores users, POIs, quest chains, task progress, check-ins, feed/map records, survey responses, provider cache, and AI job logs.
  - Qdrant stores vectorized cultural knowledge chunks for retrieval.
  - Firebase stores identity and uploaded media, not relational product data.
- AI service computes the final recommendation score from normalized backend context:
  - `weather_score`
  - `traffic_score`
  - `distance_score`
  - `crowd_score`
  - `culture_score`
- `culture_score` is computed by AI service from retrieved facts, source quality, place priority, and user interests.
- Gemini generates explanations and tasks only after retrieval. If Qdrant or Gemini fails, return curated fallback output with `fallback=true`.

## Public AI Contracts

Extend existing AI schemas instead of creating a new service boundary.

### `POST /explain-recommendation`

Input:

```json
{
  "user_id": "demo-user",
  "language": "vi",
  "place": {
    "place_id": "hue-imperial-city",
    "name": "Kinh thanh Hue",
    "category": "heritage",
    "lat": 16.4691,
    "lng": 107.5774
  },
  "interests": ["history", "architecture"],
  "context": {
    "weather_score": 80,
    "traffic_score": 70,
    "distance_score": 90,
    "crowd_score": 65,
    "missing_factors": []
  }
}
```

Output:

```json
{
  "status": "ok",
  "data": {
    "place_id": "hue-imperial-city",
    "suitability_score": 78,
    "culture_score": 85,
    "bubble_size": "large",
    "explanation": "Good time to visit because traffic and distance are favorable, and the site strongly matches your interest in Nguyen dynasty architecture.",
    "cultural_highlight": "Kinh thanh Hue was the political center of the Nguyen dynasty and is part of the Complex of Hue Monuments.",
    "reason_codes": ["culture_match", "good_weather", "nearby"],
    "source_refs": [
      {
        "source_type": "official",
        "title": "Trung tam Bao ton Di tich Co do Hue",
        "url": "...",
        "publisher": "Hue Monuments Conservation Centre",
        "page_or_section": "Kinh thanh Hue"
      }
    ],
    "fallback": false,
    "confidence": 0.86
  }
}
```

### `POST /generate-question-pool`

Used offline or admin-side before the pilot. Generates draft tasks from curated facts, then the team reviews and freezes them into `data/question_pool.json`.

### `POST /generate-task`

Runtime fallback only. If the reviewed task pool cannot serve a suitable task, generate one task from retrieved knowledge and validate it with SafetyKeeper.

### `GET /quest-chain`

Return the frozen pilot quest chains:

- `quest-hue-imperial`: 3 tasks.
- `quest-hue-river-food`: 3 tasks.

### `POST /verify-capture`

Keep lightweight for pilot. AI service can judge media presence and produce feedback, but backend remains authoritative for GPS radius, task status, and persistence.

### Optional Debug Endpoint: `POST /knowledge/search`

Internal-only endpoint for testing retrieval quality. Returns top facts with source metadata. Not needed by web or mobile.

## Implementation Tasks

### Task 1: Make AI Service Runnable

Description: Fix stale `src.bevietnam...` imports, Docker command, and script imports so the service starts cleanly.

Acceptance criteria:

- `from services.ai.main import app` succeeds.
- `uvicorn services.ai.main:app --port 8001` starts.
- `/health` returns `200`.

Verification:

- `PYTHONPATH=. python -c "from services.ai.main import app"`
- `PYTHONPATH=. uvicorn services.ai.main:app --host 127.0.0.1 --port 8001`
- `curl http://127.0.0.1:8001/health`

Likely touched:

- `services/ai/**`
- `scripts/data-pipeline/**`
- `services/ai/Dockerfile`

### Task 2: Define Knowledge Chunk Schema

Description: Replace loose cultural facts with traceable chunks.

Required fields:

```json
{
  "chunk_id": "hue-imperial-001",
  "place_id": "hue-imperial-city",
  "place_name": "Kinh thanh Hue",
  "category": "history",
  "language": "vi",
  "text": "...",
  "source_type": "official|book|unesco",
  "source_title": "...",
  "source_url": "...",
  "publisher": "...",
  "page_or_section": "...",
  "reviewed_at": "2026-06-16"
}
```

Acceptance criteria:

- Existing Hue facts are migrated to the new shape.
- Unsupported facts are marked `needs_review` or excluded.
- Every generated answer can include `source_refs`.

Verification:

- JSON validation script passes.
- At least 10 Hue POIs have 2 or more chunks each.

### Task 3: Rebuild CultureScout Retrieval

Description: Upgrade `CultureScout` to search by place, interest, language, and category; return source-rich facts.

Acceptance criteria:

- Retrieves facts from Qdrant when available.
- Falls back to local curated JSON when Qdrant or Hugging Face fails.
- Filters by `place_id` or `place_name`.
- Never returns facts without source metadata.

Verification:

- Unit tests for Qdrant success, empty result, provider failure, and fallback.
- Manual search for `Kinh thanh Hue` returns official or book-sourced facts.

### Task 4: Implement AI Scoring And Explanation

Description: Replace the TripAdvisor canned sentence with real score assembly and grounded explanation.

Scoring formula:

```text
suitability_score =
  0.20 * weather_score +
  0.20 * traffic_score +
  0.15 * distance_score +
  0.15 * crowd_score +
  0.30 * culture_score
```

Culture score:

- Base place priority: 40%.
- Retrieved fact strength and source quality: 30%.
- User interest match: 20%.
- Language/content completeness: 10%.

Acceptance criteria:

- AI service returns `suitability_score`, `culture_score`, `bubble_size`, `explanation`, `cultural_highlight`, `reason_codes`, and `source_refs`.
- Gemini may write explanation text, but numeric scores are calculated in Python.
- If Gemini fails, return template explanation with same score.

Verification:

- Tests for high, medium, and low bubble thresholds.
- Tests for missing weather or traffic fallback flags.
- Golden response test for `Kinh thanh Hue`.

### Task 5: Generate Reviewed Question Pool

Description: Use `QuestionPoolMaker` to draft tasks from curated facts, then freeze reviewed pilot tasks.

Acceptance criteria:

- `data/question_pool.json` contains two 3-task storylines.
- Each task has `place_id`, GPS radius, required media, source refs, weather tags, time tags, and bilingual text.
- No task asks users to enter unsafe or restricted areas.

Verification:

- JSON schema validation.
- Manual review checklist passes for all 6 tasks.
- Backend selection can read the file.

### Task 6: Improve Runtime QuestMaker

Description: Keep LangGraph flow, but make runtime generation reliable and source-grounded.

Acceptance criteria:

- `/generate-task` retrieves facts first.
- Generated task includes source refs.
- SafetyKeeper rejects unsafe, too long, unsupported, or malformed tasks.
- Fallback task is returned when retrieval, generation, or validation fails.

Verification:

- Tests for valid generation, empty retrieval, invalid LLM output, and fallback.
- Manual call with Hue coordinates returns a useful task.

### Task 7: Strengthen SafetyKeeper

Description: Add validation rules for source grounding, language, field length, unsafe content, and schema compliance.

Acceptance criteria:

- Recommendation explanations require at least one source ref unless `fallback=true`.
- Cultural claims without source refs are rejected.
- Tasks cannot mention dangerous or restricted behavior.
- Output lengths fit mobile/feed UI.

Verification:

- Unit tests for missing source, dangerous text, long text, invalid difficulty, and malformed response.

### Task 8: Backend Integration Checkpoint

Description: Wire backend to the final AI contracts.

Acceptance criteria:

- Backend uses managed PostgreSQL as the pilot source of truth for product data.
- Backend verifies Firebase ID tokens for protected user actions and stores capture photos in Firebase Storage.
- Backend sends normalized weather, traffic, distance, and crowd scores.
- Feed and bubble map use the same `suitability_score`.
- Storyline next-task prefers reviewed pool and uses AI runtime fallback only when needed.
- AI service failure does not break feed, map, or storyline.

Verification:

- Backend smoke tests for feed, map, storyline, and verify-capture.
- Simulated AI failure still returns usable fallback data.

## Timeline

Day 1:

- Task 1: make AI service runnable.
- Task 2: lock knowledge schema.
- Start official/book source cleanup.

Day 2:

- Task 3: retrieval with Qdrant plus local fallback.
- Seed or reseed Qdrant with reviewed Hue chunks.

Day 3:

- Task 4: AI scoring plus grounded explanation.
- Add tests and golden examples.

Day 4:

- Task 5: generate and review question pool.
- Task 6: improve runtime QuestMaker fallback.

Day 5:

- Task 7: safety validation.
- Task 8: backend integration checkpoint.

Day 6:

- End-to-end testing with web and mobile.
- Fix contract mismatches only.

Day 7:

- Pilot run with tourists.
- Collect survey, logs, screenshots, and failure notes.

Day 8:

- Final bug fixes.
- Freeze demo data.
- Complete report evidence for AI architecture, source grounding, scoring, limitations, and pilot results.

## Test Plan

Required automated tests:

- AI import/startup smoke test.
- `CultureScout` retrieval success/fallback tests.
- `TripAdvisor` score formula tests.
- `QuestMaker` fallback tests.
- `QuestionPoolMaker` schema validation tests.
- `SafetyKeeper` rejection tests.
- Backend-to-AI contract smoke tests.

Required manual scenarios:

- Good weather plus low traffic makes `Kinh thanh Hue` large on map.
- Rain or high traffic reduces score but still explains why the place matters.
- Gemini disabled still returns feed explanation and quest fallback.
- Qdrant disabled still returns curated source-backed facts.
- Tourist completes first storyline task with photo and GPS.

## Assumptions

- Local `.env` already contains live provider keys; secret values must not be committed or shown in reports.
- Qdrant Cloud remains the vector store.
- Gemini remains the LLM provider.
- Firebase remains the auth and media-storage provider.
- Managed PostgreSQL is used for the pilot database; local database usage is limited to development and fallback testing.
- Backend can provide normalized context scores for weather, traffic, distance, and crowd.
- AI service focuses on Hue pilot quality first; Da Nang and Hoi An expansion are future scope unless pilot time remains.

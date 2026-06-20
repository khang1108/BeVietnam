# BeVietnam Team API Contract

**Date:** 2026-06-20
**Scope:** stable web/mobile integration contract for the Huế-first V1.

Backend base path: `/api/v1`

AI responses are allowed to degrade, but response shapes must stay stable. Clients should never crash when `fallback=true`, `ai_generated=false`, or optional diagnostics are absent.

---

## 1. Feed Item Fields

Endpoint:

```http
GET /api/v1/feed
```

Response:

```json
{
  "items": [
    {
      "id": "f-001",
      "place_id": "hue-imperial-city",
      "name": "Kinh thành Huế",
      "category": "history",
      "thumbnail_url": "https://...",
      "score": 0.95,
      "explanation": "Why this place is good now.",
      "created_at": "2026-06-20T09:00:00Z"
    }
  ]
}
```

Required client fields:

| Field | Type | Notes |
|---|---|---|
| `id` | string | Feed card id. |
| `place_id` | string | Join key to place/map data. |
| `name` | string | Display name. |
| `category` | string | Example: `history`, `temple`, `museum`, `food`, `nature`. |
| `thumbnail_url` | string/null | UI must tolerate null. |
| `score` | number | 0.0-1.0 legacy recommendation score. |
| `explanation` | string | Human-readable reason. |
| `created_at` | ISO datetime | Display/sort metadata. |

AI overlay fields may be added by backend without breaking clients:

| Field | Type | Notes |
|---|---|---|
| `suitability_score` | integer | 0-100 Python-computed score. |
| `bubble_size` | string | `small`, `medium`, `large`. |
| `reason_codes` | string[] | Example: `good_weather`, `nearby`, `not_crowded`, `culture_match`. |
| `source_refs` | object[] | Cultural grounding references. |
| `fallback` | boolean | True if backend/AI used fallback content. |
| `ai_generated` | boolean | True only when LLM phrasing was used. |

---

## 2. Map Marker Fields

Endpoint:

```http
GET /api/v1/places?category=&limit=100&offset=0
```

Response:

```json
{
  "total": 1,
  "items": [
    {
      "id": "hue-imperial-city",
      "name": "Kinh thành Huế",
      "category": "history",
      "description": "Short description.",
      "latitude": 16.4692,
      "longitude": 107.5775,
      "image_url": "https://...",
      "reference_url": "https://..."
    }
  ]
}
```

Map marker contract:

| Field | Type | Notes |
|---|---|---|
| `id` | string | Same as `place_id` in feed/tasks when possible. |
| `name` | string | Marker label/detail title. |
| `category` | string | Marker icon/category styling. |
| `latitude` | number | Required for map. |
| `longitude` | number | Required for map. |
| `image_url` | string/null | Detail preview; tolerate null. |
| `reference_url` | string/null | External source; tolerate null. |

Bubble sizing:

| Input | UI behavior |
|---|---|
| `bubble_size="large"` or `suitability_score >= 70` | Largest marker. |
| `bubble_size="medium"` or `45 <= suitability_score < 70` | Medium marker. |
| `bubble_size="small"` or `suitability_score < 45` | Small marker. |
| Missing AI score | Use medium/default marker. |

---

## 3. Quest / Task Fields

Quest chain endpoint:

```http
GET /api/v1/storyline/quest?user_id=demo-user&quest_id=quest-hue-imperial
```

Response:

```json
{
  "quest_id": "quest-hue-imperial",
  "place_name": "Kinh thành Huế",
  "total_tasks": 3,
  "current_step": 1,
  "tasks": [
    {
      "quest_id": "quest-hue-imperial",
      "task_id": "task-001",
      "step_index": 1,
      "title": "Find Ngọ Môn",
      "description": "Move to the gate and observe its architecture.",
      "cultural_explanation": "Cultural context shown after/with task.",
      "completion_requirement": "Upload a photo showing the gate.",
      "unlock_condition": {},
      "next_task_hint": "",
      "difficulty": "easy",
      "reason_codes": ["nearby", "culture_match"],
      "place_id": "hue-imperial-city",
      "status": "active"
    }
  ]
}
```

Next task endpoint:

```http
GET /api/v1/storyline/next-task?user_id=demo-user&latitude=16.4692&longitude=107.5775&weather=cloudy&time_of_day=morning
```

Response:

```json
{
  "task": {
    "task_id": "fallback-hue-ngo-mon-detail",
    "title": "Quan sát biểu tượng ở Ngọ Môn",
    "description": "Task text.",
    "cultural_explanation": "Why this task matters culturally.",
    "difficulty": "easy",
    "completion_requirement": "Hoàn thành theo yêu cầu và gửi minh chứng: photo.",
    "place_id": "Kinh thành Huế",
    "score": 0.92
  },
  "ai_generated": false,
  "fallback": false
}
```

Task status enum:

| Value | Meaning |
|---|---|
| `locked` | Not available yet. |
| `active` | Current task. |
| `completed` | Finished task. |

Difficulty enum:

| Value |
|---|
| `easy` |
| `medium` |
| `hard` |

---

## 4. Capture Verification Response

Endpoint:

```http
POST /api/v1/storyline/verify-capture
```

Request:

```json
{
  "user_id": "demo-user",
  "task": {
    "task_id": "task-001",
    "title": "Find Ngọ Môn",
    "description": "Take a photo of the main gate.",
    "completion_requirement": "Photo must show the gate.",
    "place_id": "hue-imperial-city"
  },
  "capture": {
    "media_url": "https://minio.example/captures/demo.jpg",
    "note": "At the gate",
    "place_id": "hue-imperial-city"
  }
}
```

Response:

```json
{
  "approved": true,
  "match": true,
  "status": "approved",
  "reason": "The image clearly shows the requested gate.",
  "confidence": 0.88,
  "fallback": false,
  "ai_generated": true
}
```

Field semantics:

| Field | Type | Notes |
|---|---|---|
| `approved` | boolean | UI unlocks next task only when true. |
| `match` | boolean | Vision model judgment that image matches the task. |
| `status` | string | `approved`, `rejected`, `needs_review`, or `error`. |
| `reason` | string | Safe to show to user. |
| `confidence` | number | 0.0-1.0. |
| `fallback` | boolean | True if VLM/AI was unavailable or no image was supplied. |
| `ai_generated` | boolean | True if VLM produced the verdict. |

Client behavior:

| Status | UI behavior |
|---|---|
| `approved` | Mark task completed and unlock next node. |
| `rejected` | Keep task active; show `reason`; allow retry. |
| `needs_review` | Keep task active or show pending/manual-review state; do not crash. |
| `error` | Show retry UI; do not unlock. |

---

## 5. Error / Fallback Behavior

Global rules:

- AI failure must not break feed, map, or storyline screens.
- If `fallback=true`, show the content but avoid claiming it was AI-personalized.
- If `ai_generated=false`, the text came from deterministic template, curated pool, or fallback.
- Missing optional fields must not crash clients.
- Clients should prefer backend booleans (`approved`, `fallback`, `ai_generated`) over parsing text.

Recommended UI copy:

| Condition | Copy |
|---|---|
| Capture `needs_review` | "We received your photo, but automatic checking is temporarily unavailable." |
| Feed fallback | "Recommended from curated cultural data." |
| Missing map score | Use default marker size silently. |

---

## 6. LangSmith / AI Observability

AI service traces these runtime workflows when LangSmith env vars are configured:

```env
LANGSMITH_TRACING=true
LANGSMITH_API_KEY=<secret>
LANGSMITH_PROJECT=BeVietnam AI
```

Traced workflows:

- `Trip Advisor`
- `Trip Advisor Explain`
- `Capture Judge`
- `Capture Judge Verify`

Do not expose LangSmith keys to web/mobile clients.

# BeVietnam Huế Pilot: 8-Day Delivery Plan

## Summary

Deliver both web and Android as pilot-ready products. Use a Goong bubble map, Google Routes traffic data, OpenWeather, Firebase Authentication/Storage, PostgreSQL, Qdrant, and grounded LangGraph agents.

Deploy the complete stack to the existing VPS/domain with Docker Compose and HTTPS. Pilot content is frozen at 10 Huế POIs and two 3-task storylines.

## Integration Strategy

- Create `integration/hue-pilot` from local `main`; commit only `.gitignore` and `docs/PROJECT_SPEC.md`. Leave unrelated `CLAUDE.md` untouched.
- Do not directly merge teammate branches because they target removed `src/...` paths.
- Port `origin/feat/mobile-api-integration` into current `mobile/`.
- Port aggregate web work from `origin/nghia`, then selectively overlay the newer scoped changes from:
  - `origin/feature/storyline-checkin`
  - `origin/feature/events-auto-status`
  - `origin/feature/dashboard-3D-animation`
  - `origin/feature/dashboard-dragon-video`
- Exclude generated files, old directory structures, `.next`, `tsconfig.tsbuildinfo`, and local configuration files.

## Public Interfaces

- `GET /api/v1/feed?lat=&lng=&language=` returns ranked POIs with `suitability_score`, factor scores, explanation, cultural summary, sources, timestamp, and fallback flags.
- `GET /api/v1/map/pois?lat=&lng=&language=` returns the same POI scores used by the feed.
- Protected endpoints require `Authorization: Bearer <Firebase ID token>`; user identity is derived from the token, never request `user_id`.
- `GET /api/v1/storyline/quest?quest_id=` and `/storyline/next-task` return persisted user progress and grounded tasks.
- `POST /api/v1/captures` records Firebase Storage path, task, place, GPS, and capture time.
- `POST /api/v1/storyline/verify-capture` approves only when a photo exists and the user is within the task’s configurable radius; default radius is 200 meters.
- Firebase Storage uses user-owned paths: `captures/{firebase_uid}/{capture_id}`.

## Execution Tasks

| Day | Task | Owner | Verification |
|---|---|---|---|
| 1 | Port teammate branches into current paths and freeze API contracts | Web, Mobile, Backend | Web builds; Android builds; no old `src/` paths are introduced |
| 1 | Repair all stale Python imports, Docker commands, and Alembic imports | Backend/AI | Backend and AI apps import successfully; health endpoints return `200` |
| 2 | Implement PostgreSQL migrations and repositories for users, places, quests, progress, captures, attempts, and recommendation snapshots | Backend | Fresh migration succeeds; data survives restart |
| 2 | Configure Firebase Google/email login, backend token verification, Firebase Storage rules, and both client SDKs | Backend, Web, Mobile | Guest browsing works; protected routes reject invalid tokens; authenticated upload succeeds |
| 3 | Build the shared suitability-scoring service using the specification weights | Backend | Feed and map return identical deterministic scores and explanations |
| 3 | Integrate OpenWeather and Google Routes `computeRouteMatrix`; cache results and expose fallback flags | Backend | Live and simulated-failure tests pass; feed still renders when providers fail |
| 4 | Curate 10 Huế POIs and two 3-task storylines from verified government sources; seed PostgreSQL and Qdrant | AI/Data | Every cultural item has source metadata; retrieval and curated fallback tests pass |
| 4–6 | Connect web and Android to final feed, map, Firebase, storyline, GPS, camera, and upload contracts | Web, Mobile | Both clients complete the full tourist flow on real devices |
| 7 | Deploy VPS stack with Caddy HTTPS, backend, AI, web, PostgreSQL volume, and configured secrets | Deploy/QA | Public domain passes end-to-end smoke test; camera, GPS, and Firebase work over HTTPS |
| 7–8 | Run tourist pilot, fix critical findings, and complete `docs/main_report.md` | Whole team | At least 10 surveys collected; report includes results, limitations, tests, and architecture |

## Scoring Defaults

- Cultural value: 30%
- Distance: 20%
- Weather suitability: 15%
- Traffic-aware travel time: 15%
- Estimated crowdedness: 15%
- Preference match: 5%, using neutral `50` for guests
- Estimated crowdedness must always be labeled as an estimate.
- External-provider failure uses neutral factor values and visible fallback flags.
- Google traffic uses Routes API `computeRouteMatrix` with `TRAFFIC_AWARE`; Goong remains the map renderer.

## Final Verification

- Backend/AI import, API, persistence, Firebase authorization, scoring, grounding, and fallback tests pass.
- `npm run lint && npm run build` passes.
- `./gradlew testDebugUnitTest assembleDebug` passes.
- Docker Compose starts cleanly on the VPS and survives restart.
- Both clients complete: browse → feed/map → authenticate → storyline → GPS/photo capture → unlock next task.
- Pilot reaches: ≥10 participants, ≥70% recommendation usefulness, ≥60% cultural learning, and ≥50% storyline completion.

## Assumptions

- Existing VPS, domain, Firebase project, Firebase billing, and Google Maps Platform billing are ready.
- Firebase Storage is the selected media store.
- Qdrant Cloud remains the vector store.
- Product-critical failures take priority over visual polish; teammate UI features may be disabled only when they block pilot flows.
- Google Routes matrix supports the 10-POI pilot in one request: <https://developers.google.com/maps/documentation/routes/compute_route_matrix>.
- Firebase Storage access is restricted by authenticated user ownership: <https://firebase.google.com/docs/storage/security/rules-conditions>.

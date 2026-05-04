# Agile Kickoff Plan — Agent-Based Smart Tourism System for Vietnam

**Date:** 2026-05-04  
**Purpose:** Prepare the team for the project kickoff meeting and give every member a clear, practical plan for starting MVP development.  
**Team size:** 6 members  
**Recommended database:** PostgreSQL  
**Recommended delivery model:** Scrum-lite Agile with 1-week sprints

---

## 1. Project Summary

This project is a smart tourism system for Vietnam. The system helps travelers discover meaningful cultural places, complete storyline-based exploration tasks, receive personalized recommendations, and generate travel memories through AI-assisted vlog/diary generation.

The project has four main product surfaces for the MVP:

1. **Android App**
   - Primary native mobile experience for Android users.
   - Used for login, map discovery, storyline tasks, photo capture, check-ins, and personal travel progress.

2. **Responsive Web App / PWA**
   - Website platform for desktop users and iPhone users during the MVP.
   - Used for discovery, generated vlogs, travel history, and possibly team/admin debugging tools.
   - Can be designed responsively so iPhone users can still access core features through Safari without native iOS development.

3. **Backend API**
   - Owns authentication, user data, places, tasks, captures, feed APIs, and communication with AI Core.
   - Serves Android and web clients through the same API contracts.

4. **AI Core / Agent System**
   - Owns cultural knowledge retrieval, task generation, recommendation reasoning, photo verification, and vlog generation architecture.

Future option:

- A native iOS app can be added later when the team has access to Mac hardware or a CI/cloud build workflow for iOS.

---

## 2. Key Technical Decisions

### 2.1 Primary database: PostgreSQL

The project should standardize on **PostgreSQL**.

Reasons:

- The current `docker-compose.yaml` already uses a PostgreSQL service.
- PostgreSQL is suitable for relational product data such as users, places, captures, tasks, comments, ratings, and generated posts.
- PostgreSQL supports JSON fields when flexible metadata is needed.
- PostgreSQL can support geospatial features later through PostGIS, which is useful for map and tourism features.
- PostgreSQL is strong for future analytics and recommendation-related queries.

Current note:

- `db/bevietnam.sql` is currently written in MySQL-style SQL.
- The schema should be treated as a logical draft and converted into PostgreSQL-compatible migrations soon.

### 2.2 Backend framework

Recommended backend framework: **FastAPI**.

Important clarification:

- FastAPI is the **server-side backend framework**, not the frontend platform technology.
- Android and web clients will call FastAPI through HTTP/JSON APIs.
- One backend should serve all active MVP platforms so the team does not duplicate business logic.

Reasons:

- The repository already has FastAPI dependencies.
- FastAPI works well with Python AI services.
- It provides automatic API documentation through OpenAPI.
- It is fast enough for MVP development and easy for student teams to understand.

### 2.3 Frontend platform technology choices

Recommended frontend stack:

| Platform | Recommended technology | Why this is suitable |
|---|---|---|
| Android | **Kotlin + Jetpack Compose** | Modern official Android stack used heavily in industry; declarative UI; strong Google support; suitable for camera, location, maps, and offline mobile features. |
| Web / PWA | **Next.js + TypeScript + React** | Common enterprise web stack; used by many modern product teams; supports routing, responsive UI, API integration, and maintainable frontend architecture. Can also serve iPhone users through Safari as a responsive website/PWA. |

Why not build native iOS in the MVP:

- Native iOS development requires macOS and Xcode for practical local development and testing.
- The team does not currently have Mac hardware.
- Trying to force native iOS without the required environment would slow the project and create unnecessary risk.

Recommended MVP approach:

- Build one strong **native Android app**.
- Build one strong **responsive website/PWA** that works well on desktop and mobile browsers, including iPhone Safari.
- Keep API contracts platform-neutral so a native iOS app can be added later without changing backend logic.

Tradeoff:

- iPhone users will not get a fully native app in the MVP.
- The responsive web/PWA should cover the most important viewing and discovery flows, while Android handles the strongest native camera/location experience first.

### 2.4 AI Core as a separate service

The AI Core should remain a separate service from the backend.

Reasons:

- AI logic will evolve faster than normal backend CRUD logic.
- Agent workflows may need separate dependencies, prompts, vector database access, and model provider configuration.
- Separating AI Core makes responsibilities clearer.

Recommended service boundary:

- Backend owns product state and user-facing APIs.
- AI Core owns reasoning, generation, retrieval, ranking support, and validation logic.
- Backend calls AI Core through internal HTTP APIs.

---

## 3. Team Roles and Responsibilities

## 3.1 Team Lead + Agent Architecture Owner

**Owner:** Team leader

Main responsibilities:

- Define the full AI agent architecture.
- Design AI Core service boundaries.
- Define agent inputs, outputs, and failure behavior.
- Prioritize backlog with the team.
- Review system-level design decisions.
- Coordinate backend, Android, web, and AI dependencies.
- Make sure the MVP stays focused.

Primary deliverables:

- Agent architecture diagram.
- AI Core API contract draft.
- Task generation flow.
- Recommendation flow.
- Vlog generation flow.
- Cultural knowledge retrieval design.
- Sprint planning support.

---

## 3.2 Backend Engineer 1 — Platform, Auth, and Database

Main responsibilities:

- Set up backend project structure.
- Convert database schema to PostgreSQL.
- Implement migrations.
- Implement users, profiles, and authentication-related APIs.
- Maintain database consistency.
- Define backend coding conventions.

Primary deliverables:

- PostgreSQL schema v1.
- Migration setup.
- User and profile models.
- Auth provider model.
- Basic auth/profile endpoints.
- Backend local setup instructions.

---

## 3.3 Backend Engineer 2 — Product APIs and Integrations

Main responsibilities:

- Implement places, feed, storyline, capture, and AI integration APIs.
- Connect backend to AI Core.
- Integrate external services such as weather and map providers when needed.
- Help design background jobs for vlog generation.

Primary deliverables:

- Places API.
- Feed API.
- Storyline task API.
- Capture metadata API.
- AI Core client module.
- Context service for weather/map data.

---

## 3.4 Android Frontend Engineer 1 — App Shell, Auth, and Capture

Recommended technology:

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM or MVI-style presentation layer
- **Networking:** Retrofit or Ktor Client
- **Async:** Kotlin Coroutines + Flow
- **Local storage:** Room or DataStore when needed
- **Camera/media:** CameraX and Android photo picker where suitable

Main responsibilities:

- Set up the Android app structure.
- Implement Android navigation.
- Implement Android login/profile flow.
- Implement Android capture/upload flow.
- Connect Android app to backend APIs.
- Follow Android platform conventions for permissions, camera, and local storage.

Primary deliverables:

- Android project shell.
- Android navigation structure.
- Android API client.
- Android auth/profile screens.
- Android capture screen.
- Android permission handling for camera and location.

---

## 3.5 Android Frontend Engineer 2 — Discovery, Feed, and Storyline UI

Recommended technology:

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM or MVI-style presentation layer
- **Networking:** Retrofit or Ktor Client, shared with Android Frontend Engineer 1
- **Async:** Kotlin Coroutines + Flow
- **Maps/location:** Google Maps SDK or Goong-compatible Android SDK/API if selected

Main responsibilities:

- Implement Android map/discovery UI.
- Implement Android personalized feed UI.
- Implement Android storyline task UI.
- Display places, tasks, and recommendation explanations.
- Work with Backend Engineer 2 on API payloads for places, feed, and tasks.
- Coordinate with Android Frontend Engineer 1 so the Android app has one consistent architecture.

Primary deliverables:

- Android discovery/map screen.
- Android feed screen.
- Android storyline task screen.
- Android place card component.
- Android task card component.

---

## 3.6 Web Frontend Engineer — Website Platform / PWA

Recommended technology:

- **Language:** TypeScript
- **Framework:** Next.js + React
- **Styling:** Tailwind CSS or CSS Modules
- **Data fetching/state:** TanStack Query for server state, lightweight local state where needed
- **Validation:** Zod for shared request/response validation if useful
- **Maps:** Goong Maps, Mapbox GL-compatible APIs, or another selected web map provider
- **Responsive/PWA support:** mobile-first responsive layout, installable PWA option if time allows

Main responsibilities:

- Set up the web app structure.
- Implement responsive web discovery pages.
- Implement vlog/memory viewing pages.
- Implement mobile-browser-friendly layouts for iPhone users.
- Build simple internal/debug pages if useful for the team.
- Connect web app to backend APIs.
- Keep web API contracts aligned with Android.

Primary deliverables:

- Next.js web app shell.
- Web API client.
- Responsive discovery page.
- Places list or map page.
- Vlog/post viewing page.
- Mobile-browser-friendly layout.

---

## 4. Agile Process

## 4.1 Recommended framework

Use **Scrum-lite**.

This means the team follows Scrum principles but keeps ceremonies short and practical.

Why Scrum-lite is suitable:

- The project is still early.
- The team is small.
- The MVP requires coordination across backend, AI, Android, and web.
- Weekly progress checks help prevent people from working in different directions.

## 4.2 Sprint length

Recommended sprint length: **1 week**.

Reasons:

- Fast feedback.
- Easier for student teams to stay aligned.
- Good for early-stage architecture and MVP implementation.

## 4.3 Ceremonies

### Sprint Planning

Frequency: once per sprint  
Recommended duration: 45–60 minutes

Purpose:

- Select sprint goal.
- Select stories/tasks.
- Confirm owners.
- Confirm dependencies.
- Confirm expected demo.

### Daily Standup

Frequency: every working day  
Recommended duration: 10–15 minutes

Each member answers:

1. What did I finish yesterday?
2. What will I do today?
3. What is blocking me?

### Backlog Refinement

Frequency: once per sprint  
Recommended duration: 30–45 minutes

Purpose:

- Clarify upcoming tasks.
- Split large tasks.
- Add acceptance criteria.
- Identify missing API/schema decisions.

### Sprint Review / Demo

Frequency: end of sprint  
Recommended duration: 30–45 minutes

Purpose:

- Show working software or concrete artifacts.
- Validate whether sprint goal was achieved.
- Record feedback.

### Retrospective

Frequency: after sprint review  
Recommended duration: 20–30 minutes

Questions:

1. What went well?
2. What was confusing or blocked?
3. What should we improve next sprint?

---

## 5. Working Agreement

The team should agree to these rules during kickoff.

### 5.1 General rules

- Every task must have one clear owner.
- Every sprint must have one clear sprint goal.
- Each member should push small changes frequently.
- Do not wait for perfect backend or AI logic; use mocks when needed.
- Prioritize working vertical slices over isolated unfinished modules.
- Avoid adding features that are not part of the MVP.

### 5.2 Communication rules

- Report blockers early.
- If a task affects another member, communicate before changing the contract.
- API and schema changes must be announced clearly.
- The team lead resolves priority conflicts.

### 5.3 Code review rules

- At least one teammate should review non-trivial changes.
- Database changes should be reviewed by Backend Engineer 1 and the team lead.
- AI contract changes should be reviewed by the team lead and Backend Engineer 2.
- Frontend API usage should match documented backend contracts.

---

## 6. Backlog Structure

Use this hierarchy:

```text
Epic → Feature → User Story → Task/Subtask
```

Example:

```text
Epic: Storyline Task Engine
Feature: Generate next cultural task
User Story: As a traveler, I want to receive a task near my current location so that I can explore the culture around me.
Tasks:
- Define request/response contract
- Implement backend endpoint
- Implement AI Core mock endpoint
- Display task in the Android app and responsive web app
- Test happy path
```

---

## 7. MVP Epics

## Epic 1 — Project Foundation

Goal:

Create a runnable local development environment and clear project structure.

Includes:

- Docker Compose setup.
- Backend service skeleton.
- AI Core service skeleton.
- Web app shell.
- Android app shell.
- Shared API contract folder.
- Environment variable documentation.

---

## Epic 2 — Authentication and User Profile

Goal:

Allow users to have identity and personal data in the system.

Includes:

- Users table.
- User profiles table.
- Auth provider table.
- Login/register flow.
- Profile read/update flow.
- User-scoped API requests.

---

## Epic 3 — Places and Map Discovery

Goal:

Allow users to discover tourism places on Android and web.

Includes:

- Places schema.
- Place categories.
- Places API.
- Android map/list UI.
- Web discovery UI.
- Basic filters.

---

## Epic 4 — Storyline Task Engine

Goal:

Give users cultural exploration tasks based on location/context.

Includes:

- Storyline/task schema.
- API for next task.
- AI Core task generation endpoint.
- Android storyline task UI.
- Task progress tracking.

---

## Epic 5 — Capture and Media

Goal:

Allow users to capture photos as evidence of exploration or travel memories.

Includes:

- Capture metadata schema.
- Media asset schema.
- Android capture screen.
- Upload flow.
- Capture list/history.
- Link capture to task/place when needed.

---

## Epic 6 — Context-Aware Recommendation Feed

Goal:

Recommend places or tasks based on user interests, location, context, and cultural relevance.

Includes:

- Interest taxonomy.
- User interest state.
- User interest events.
- Feed API.
- Ranking logic v1.
- AI-generated explanation snippets.

---

## Epic 7 — AI Cultural Brain

Goal:

Create the AI Core service that supports cultural retrieval and agent workflows.

Includes:

- Cultural knowledge ingestion.
- Vector database setup with Qdrant.
- Retrieval API.
- Task generation agent.
- Feed explanation agent.
- Photo verification agent design.
- Vlog generation agent design.

---

## Epic 8 — Nightly Vlog Generation

Goal:

Generate a travel diary/vlog post from a user's validated captures.

Includes:

- AI job schema.
- Vlog post schema.
- Source capture selection.
- Agent pipeline design.
- Generated title/body/summary.
- Android or web viewing UI.

---

## Epic 9 — QA, Demo, and Deployment Readiness

Goal:

Make the project stable enough to demo and continue development.

Includes:

- Local setup documentation.
- Basic tests.
- API smoke tests.
- Demo seed data.
- Error logging.
- Sprint demo checklist.

---

## 8. Definition of Ready

A task or user story is ready when it has:

- Clear purpose.
- Clear owner.
- Clear acceptance criteria.
- Known dependencies.
- API contract or UI mockup if needed.
- Database impact identified if needed.
- Test/demo expectation.

A task is not ready if:

- Nobody understands the expected output.
- It depends on an undefined API.
- It depends on an undecided schema.
- It is too large to finish in one sprint.

---

## 9. Definition of Done

A task or user story is done when:

- The implementation is complete.
- The owner tested it locally.
- The result can be demonstrated.
- Related API/schema documentation is updated if needed.
- It does not break existing flows.
- It is merged or ready to merge.
- Any known limitations are documented.

For frontend tasks, done means:

- The screen/component renders correctly.
- It handles loading and empty states.
- It calls the correct API or mock service.
- The owner has manually tested the main flow.

For backend tasks, done means:

- Endpoint works locally.
- Request/response format is clear.
- Database read/write behavior is correct.
- Error cases at system boundaries are handled.

For AI tasks, done means:

- Input/output contract is defined.
- Prompt or agent behavior is documented.
- At least one example request/response exists.
- Failure behavior is described.

---

## 10. Sprint Roadmap

## Sprint 0 — Kickoff and Foundation

Recommended duration: 1 week

### Sprint Goal

Align the team, finalize architecture decisions, and prepare the project for implementation.

### Main Outcomes

- Team understands the MVP.
- Roles are confirmed.
- PostgreSQL decision is confirmed.
- Backend, AI Core, Android and web responsibilities are clear.
- Sprint 1 tasks are ready.

### Tasks

#### Team Lead / Agent Architecture Owner

- Explain product vision to the team.
- Finalize MVP scope.
- Draw high-level system architecture.
- Define AI Core responsibilities.
- Define first AI Core API contracts.
- Create draft agent architecture.
- Prepare Sprint 1 backlog.

#### Backend Engineer 1

- Review current `db/bevietnam.sql`.
- Convert current auth/user schema direction to PostgreSQL design.
- Choose migration tool.
- Define initial database naming conventions.
- Prepare user/profile/auth schema migration plan.

#### Backend Engineer 2

- Review backend folder structure.
- Draft FastAPI project structure.
- Define internal module layout.
- Prepare AI Core client design.
- Draft product API list for places, feed, tasks, and captures.

#### Android Frontend Engineer

- Review Android project structure.
- Choose Android navigation structure.
- Draft Android screens for login, profile, map/feed, storyline, and capture.
- Prepare Android API client structure.
- Identify Android camera, location, and map permission requirements.

#### Android Frontend Engineer 2

- Draft Android map/feed/storyline screens.
- Define reusable Android components such as place cards and task cards.
- Identify Android map provider integration needs.
- Coordinate screen flow with Android Frontend Engineer 1.

#### Web Frontend Engineer

- Review web folder structure.
- Draft web page structure.
- Prepare web API client structure.
- Draft discovery and vlog page layout.

### Sprint 0 Demo

The team should present:

- Architecture diagram.
- Initial database direction.
- API list.
- Android screen plan.
- Web screen plan.
- AI agent architecture draft.

---

## Sprint 1 — Vertical Skeleton

Recommended duration: 1 week

### Sprint Goal

Create a thin end-to-end system where frontend clients can call backend, backend can access database, and backend can call AI Core.

### Main Outcomes

- Local development stack runs.
- Backend exposes basic health/profile/mock APIs.
- AI Core exposes a health endpoint and mock task endpoint.
- Android app can call backend.
- Web app can call backend.

### User Stories

#### Story 1: Backend health check

As a developer, I want a backend health endpoint so that I can verify the backend is running.

Acceptance criteria:

- `GET /health` returns service status.
- Endpoint works in local development.

#### Story 2: AI Core health check

As a developer, I want an AI Core health endpoint so that I can verify the AI service is running.

Acceptance criteria:

- `GET /health` returns service status.
- Backend can call AI Core health endpoint.

#### Story 3: User profile skeleton

As a user, I want the system to store my basic profile so that future features can personalize my experience.

Acceptance criteria:

- User/profile schema exists.
- Backend can return a mock or real user profile.
- Android and web can display profile data.

#### Story 4: Mock storyline task

As a traveler, I want to receive a simple cultural task so that I can understand the exploration flow.

Acceptance criteria:

- AI Core returns a mock task.
- Backend exposes an endpoint to get next task.
- Android displays the task.

### Sprint 1 Demo

Demo flow:

```text
Run services locally → open Android/web → call backend → backend returns profile/task → UI displays response
```

---

## Sprint 2 — Core MVP Flow

Recommended duration: 1 week

### Sprint Goal

Implement the first meaningful tourism user flow.

### Main Outcomes

- User can view places.
- User can receive a storyline task.
- User can capture/upload a photo or capture metadata.
- Backend stores core product data.

### User Stories

#### Story 1: Places API

As a traveler, I want to see tourism places so that I can choose where to explore.

Acceptance criteria:

- Places schema exists.
- Backend returns a list of places.
- Places have name, category, location, and short description.
- Android or web can display the list.

#### Story 2: Android discovery screen and responsive web discovery page

As a traveler, I want to see nearby places on my phone so that I can explore around me.

Acceptance criteria:

- Android app displays places from API or mock data.
- Responsive web page displays places from API or mock data.
- UI shows place name, category, and description.
- Loading and empty states exist on Android and web.

#### Story 3: Storyline task API

As a traveler, I want the system to give me a task near my context so that exploration feels guided.

Acceptance criteria:

- Backend endpoint returns a task.
- Task includes title, description, target place or context, and completion requirement.
- Android displays the task clearly.
- Responsive web displays the task clearly.

#### Story 4: Capture metadata

As a traveler, I want to save a capture so that my exploration can be remembered and verified.

Acceptance criteria:

- Capture schema exists.
- Backend can store capture metadata.
- Android can submit capture metadata.
- Web can display capture history or submit capture metadata if browser upload is included.
- Capture can optionally link to task or place.

### Sprint 2 Demo

Demo flow:

```text
User opens Android app → sees places → receives task → submits capture metadata
```

---

## Sprint 3 — AI and Context Integration

Recommended duration: 1 week

### Sprint Goal

Start replacing mocks with AI/context-aware behavior.

### Main Outcomes

- AI Core generates or improves cultural task content.
- Feed ranking v1 exists.
- Context service design begins.
- Recommendation explanation appears in UI.

### User Stories

#### Story 1: AI-generated cultural task

As a traveler, I want tasks that include cultural meaning so that I learn while exploring.

Acceptance criteria:

- AI Core accepts location/place/context input.
- AI Core returns task title, description, cultural explanation, and completion requirement.
- Backend can call AI Core and return the result to Android and web clients.

#### Story 2: Interest-based feed v1

As a traveler, I want recommendations related to my interests so that the feed feels personal.

Acceptance criteria:

- Interest schema exists.
- User interest state can be seeded or updated manually.
- Feed endpoint returns ranked places.
- Ranking reason is included.

#### Story 3: Recommendation explanation UI

As a traveler, I want to know why a place is recommended so that I can trust the system.

Acceptance criteria:

- Feed item includes explanation text.
- Android displays explanation text.
- Web displays explanation text if the feed/discovery page is ready.

### Sprint 3 Demo

Demo flow:

```text
User opens feed → backend ranks places → AI/core or heuristic provides explanation → UI displays personalized recommendation
```

---

## Sprint 4 — Vlog Pipeline Draft

Recommended duration: 1 week

### Sprint Goal

Create the first draft of the AI-assisted vlog generation pipeline.

### Main Outcomes

- Vlog schema exists.
- AI job schema exists.
- Draft agent pipeline exists.
- Android or web can display a generated draft post.

### User Stories

#### Story 1: Vlog post schema

As a user, I want my travel day to become a generated memory so that I can review it later.

Acceptance criteria:

- Vlog post schema exists.
- Vlog post links to user and date.
- Vlog post can store title, summary, body, status, and timestamps.

#### Story 2: Vlog agent pipeline draft

As the system, I want to process captures into a narrative so that users can receive generated memories.

Acceptance criteria:

- Pipeline stages are defined: Curator, Vision Describer, Narrator, Publisher.
- Each stage has inputs and outputs.
- Mock implementation can create one generated post from sample capture data.

#### Story 3: Vlog viewing UI

As a user, I want to read my generated vlog so that I can remember my trip.

Acceptance criteria:

- Android or web displays generated vlog title and body.
- Empty state exists when no vlog is available.

### Sprint 4 Demo

Demo flow:

```text
User has captures → AI pipeline generates draft vlog → Android/web displays generated vlog
```

---

## 11. Agent Architecture Workstream

This is the team lead's main technical workstream.

## 11.1 Agent system goals

The agent system should support:

- Cultural task generation.
- Cultural knowledge retrieval.
- Personalized feed explanations.
- Capture/photo verification.
- Nightly vlog generation.

## 11.2 Recommended agents

### 1. Cultural Retrieval Agent

Purpose:

Find relevant cultural knowledge for a place, location, or user query.

Inputs:

- Place ID or place name.
- User location.
- User language.
- Query or task context.

Outputs:

- Relevant cultural facts.
- Source references.
- Confidence score.

Used by:

- Task Generator Agent.
- Feed Explanation Agent.
- Vlog Narrator Agent.

---

### 2. Task Generator Agent

Purpose:

Generate cultural exploration tasks for users.

Inputs:

- User location.
- Nearby places.
- User interests.
- Cultural retrieval results.
- Weather/context if available.

Outputs:

- Task title.
- Task description.
- Cultural explanation.
- Completion requirement.
- Suggested place or geofence.
- Difficulty level.

Example output:

```json
{
  "title": "Find the old yellow wall",
  "description": "Walk near the old quarter and capture a colonial-style yellow wall.",
  "cultural_explanation": "Yellow colonial walls are a common visual marker in several historic Vietnamese towns.",
  "completion_requirement": "Upload one photo showing the yellow wall.",
  "difficulty": "easy"
}
```

---

### 3. Feed Explanation Agent

Purpose:

Explain why a place is recommended now.

Inputs:

- Place information.
- User interests.
- Context such as weather, time, or location.
- Ranking score factors.

Outputs:

- Short recommendation explanation.
- Reason codes.

Example:

```text
Recommended because you like local history, and this museum is a good indoor option during rainy weather.
```

---

### 4. Capture Verification Agent

Purpose:

Check whether a user capture satisfies a task requirement.

Inputs:

- Task requirement.
- Uploaded image or image metadata.
- Optional place/location metadata.

Outputs:

- Verification status: approved, rejected, needs_review.
- Reason.
- Confidence.

MVP note:

- This can start as a mock or manual rule-based result.
- Full vision-based verification can be added later.

---

### 5. Vlog Curator Agent

Purpose:

Choose useful captures for vlog generation.

Inputs:

- User captures for a date.
- Capture metadata.
- Place/task links.

Outputs:

- Selected captures.
- Ordering.
- Reason for selection.

---

### 6. Vlog Narrator Agent

Purpose:

Generate a coherent travel diary from selected captures and cultural context.

Inputs:

- Selected captures.
- Cultural retrieval results.
- User language preference.
- Tone preference if available.

Outputs:

- Vlog title.
- Summary.
- Markdown body.
- Safety status.

---

### 7. Publisher Agent

Purpose:

Save generated vlog output into the product database.

Inputs:

- Final generated content.
- User ID.
- Date.
- Source captures.

Outputs:

- Vlog post record.
- Job status.

---

## 11.3 Agent failure rules

The agent system should define failure behavior early.

Examples:

- If retrieval fails, return a simple fallback task.
- If task generation fails, backend returns a safe mock task.
- If photo verification confidence is low, mark as `needs_review`.
- If vlog generation fails, keep job status as `failed` and allow retry.
- If generated text is unsafe or low quality, block publish and regenerate later.

---

## 12. API Contract Planning

The team should define API contracts before implementation.

Recommended first contracts:

### Backend APIs

```text
GET /health
GET /me
PATCH /me/profile
GET /places
GET /feed
GET /storyline/next-task
POST /captures
GET /vlogs
```

### AI Core APIs

```text
GET /health
POST /retrieve-cultural-context
POST /generate-task
POST /explain-recommendation
POST /verify-capture
POST /generate-vlog
```

Contract format should include:

- Endpoint name.
- Owner.
- Request body.
- Response body.
- Error cases.
- Example payload.
- Whether the endpoint uses mock data or real data.

---

## 13. Database Planning

## 13.1 Current schema direction

The current SQL file already includes user/authentication/common information tables:

- `users`
- `user_profiles`
- `user_auth_providers`
- `user_devices`
- `interests`
- `user_interest_state`
- `user_interest_events`

These should be preserved conceptually, but converted to PostgreSQL syntax.

## 13.2 Recommended next schemas

Add these schema groups soon:

### Places

- `places`
- `place_categories`
- `place_category_links`
- `place_sources`
- `place_interests`

### Storyline tasks

- `storylines`
- `storyline_steps`
- `user_storyline_progress`
- `task_attempts`
- `task_verifications`

### Captures and media

- `daily_capture_rolls`
- `captures`
- `media_assets`
- `capture_place_links`

### Recommendations

- `recommendation_requests`
- `recommendation_results`
- `place_context_snapshots`

### Vlogs and AI jobs

- `ai_jobs`
- `vlog_posts`
- `vlog_source_captures`
- `ai_generation_logs`

---

## 14. Risk Register

| Risk | Impact | Probability | Mitigation |
|---|---:|---:|---|
| MVP scope becomes too large | High | High | Freeze Sprint 1–3 scope and defer extras |
| AI work blocks product flow | High | Medium | Use mock AI endpoints early |
| Backend APIs are delayed | High | Medium | Define contracts first; frontend can use mocks |
| Database schema changes too often | Medium | High | Review schema changes before implementation |
| Native Android map integration takes longer than expected | Medium | Medium | Start with list UI before full map if needed |
| Vlog generation is too complex | Medium | Medium | Start with text-only generated diary |
| Team members misunderstand ownership | High | Medium | Assign one owner per task and review daily |
| External API keys or quotas are unavailable | Medium | Medium | Use mock weather/map data for MVP |

---

## 15. Tomorrow Kickoff Meeting Agenda

Recommended duration: 60–90 minutes.

## 15.1 Opening — 5 minutes

Goal:

Explain why the meeting exists and what decisions must be made.

Expected outcome:

Everyone understands that this meeting starts MVP execution, not just discussion.

---

## 15.2 Product Vision — 10 minutes

Explain:

- Who the app is for.
- What problem it solves.
- Why Vietnam tourism needs this system.
- What makes the product different.

Key message:

The product is not just a map app. It is a cultural exploration system powered by AI agents.

---

## 15.3 MVP Scope — 10 minutes

Confirm MVP must-have features:

- User identity/profile.
- Places discovery.
- Storyline task.
- Capture flow.
- Basic recommendation feed.
- AI-generated cultural explanation.
- Draft vlog generation.

Confirm non-goals for early MVP:

- Full social network.
- Perfect traffic accuracy.
- Advanced moderation system.
- Fully automated high-quality video generation.
- Nationwide production-scale data ingestion.

---

## 15.4 Architecture Walkthrough — 15 minutes

Show the system as:

```text
Android App / Responsive Web App
        ↓
Backend API
        ↓
PostgreSQL + Object Storage + AI Core
        ↓
AI Core + Qdrant Vector Database
```

Explain:

- Backend owns product data.
- AI Core owns reasoning/generation.
- PostgreSQL owns relational data.
- Qdrant owns vector search.
- Android is the primary native MVP experience.
- Responsive web/PWA covers desktop users and iPhone users during MVP.

---

## 15.5 Role Assignment — 10 minutes

Confirm each member's ownership:

- Backend 1: auth/database/platform.
- Backend 2: product APIs/integrations.
- Android: native Android app with Kotlin and Jetpack Compose.
- Android 2: Android discovery/feed/storyline UI with Kotlin and Jetpack Compose.
- Web: discovery/vlog web pages.
- Team lead: agent architecture/system coordination.

---

## 15.6 Sprint 0 Planning — 15 minutes

Decide:

- Sprint 0 goal.
- Tasks for each member.
- Expected output by the next meeting.
- Communication channel.
- Branching and review rules.

---

## 15.7 Risks and Questions — 10 minutes

Discuss:

- What is unclear?
- What might block each role?
- Which API/data decisions are needed first?
- Which features should be mocked first?

---

## 15.8 Closing — 5 minutes

Confirm:

- PostgreSQL is official.
- FastAPI backend is official.
- AI Core is separate.
- 1-week sprint cadence is accepted.
- Every member knows their Sprint 0 tasks.

---

## 16. Immediate Action Items After Kickoff

### Team Lead

- Finalize agent architecture draft.
- Create first AI Core API contract.
- Finalize Sprint 1 backlog.

### Backend Engineer 1

- Convert user/auth schema concept to PostgreSQL.
- Set up migration structure.
- Prepare database local run instructions.

### Backend Engineer 2

- Create backend API skeleton.
- Add health endpoint.
- Draft product API contracts.

### Android Frontend Engineer

- Create native Android app shell with Kotlin and Jetpack Compose.
- Add Android navigation.
- Draft Android auth/profile, discovery/feed, storyline, and capture screens.
- Prepare Android API client.

### Android Frontend Engineer 2

- Draft Android discovery/feed/storyline screens.
- Prepare Android place/task card components.
- Coordinate API usage with Android Frontend Engineer 1.

### Web Frontend Engineer

- Create web app shell.
- Draft discovery/vlog pages.
- Prepare web API client.

---

## 17. Success Criteria for the First Month

By the end of the first month, the team should be able to demo:

```text
A user opens the Android app,
logs in or uses a test profile,
sees tourism places,
receives a cultural storyline task,
submits a capture,
gets a basic personalized recommendation,
and views a generated travel memory draft.
```

This does not need to be production-ready. It must be clear, integrated, and demonstrable.

---

## 18. Final Guidance

The most important principle for this project is:

```text
Build one integrated MVP flow before building advanced AI features deeply.
```

The AI architecture is important, but the team should avoid spending too much time on isolated agent design without connecting it to the user journey.

Recommended implementation order:

1. Local stack and service skeletons.
2. PostgreSQL schema and migrations.
3. Auth/profile flow.
4. Places and task APIs.
5. Android core screens and responsive web pages.
6. AI Core mock endpoints.
7. Real AI task generation and cultural retrieval.
8. Recommendation feed.
9. Capture flow.
10. Vlog generation draft.

This order gives the team visible progress every sprint and reduces the risk of late integration problems.

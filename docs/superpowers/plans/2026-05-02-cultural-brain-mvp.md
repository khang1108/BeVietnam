# Cultural Brain MVP Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a hybrid smart tourism system for Vietnam featuring storyline-based tasks and a contextual personalized feed driven by a "Cultural Brain" AI.

**Architecture:** A Unified Backend (Node.js/FastAPI) serving a Next.js Web App and Expo Mobile App, integrated with an AI Core microservice (Python) using RAG for cultural context.

**Tech Stack:** 
- **Backend:** FastAPI (Python) or Express (TypeScript). Let's go with **FastAPI** for better AI integration.
- **Frontend:** Next.js (Web), Expo/React Native (Mobile).
- **AI Core:** LangChain/LlamaIndex, Qdrant (Vector DB), OpenAI/Gemini APIs.
- **Maps:** Goong API.

---

## Chunk 1: Project Scaffolding & Shared Infrastructure

### Task 1: Initialize Project Structure
**Files:**
- Create: `backend/main.py`, `backend/requirements.txt`
- Create: `ai-core/main.py`, `ai-core/requirements.txt`
- Create: `web/package.json`
- Create: `mobile/package.json`

- [ ] **Step 1: Create directories and init files**
- [ ] **Step 2: Setup basic FastAPI server for Backend and AI Core**
- [ ] **Step 3: Setup docker-compose.yml for local development**
- [ ] **Step 4: Commit boilerplate**

### Task 2: Cultural Knowledge Base (AI Core)
**Files:**
- Create: `ai-core/vector_db.py`
- Create: `ai-core/data/culture_data.json`

- [ ] **Step 1: Define schema for cultural data (POI, historical tidbits)**
- [ ] **Step 2: Implement Vector DB initialization with Qdrant (local/cloud)**
- [ ] **Step 3: Create a script to ingest sample data (UNESCO sites in VN)**
- [ ] **Step 4: Test retrieval with a sample query**

---

## Chunk 2: Storyline Engine & Task Generation

### Task 3: Task Synthesizer (AI Core)
**Files:**
- Create: `ai-core/agents/storyline_agent.py`
- Modify: `ai-core/main.py`

- [ ] **Step 1: Write a prompt template for generating "Cultural Tasks" based on location**
- [ ] **Step 2: Implement endpoint `POST /generate-task` in AI Core**
- [ ] **Step 3: Test task generation with manual coordinate inputs**

### Task 4: Storyline API (Backend)
**Files:**
- Create: `backend/api/storyline.py`
- Modify: `backend/main.py`

- [ ] **Step 1: Implement endpoint `GET /storyline/next-task` that calls AI Core**
- [ ] **Step 2: Manage user state (current task, progress) in Postgres/Redis**
- [ ] **Step 3: Test full flow: User GPS -> AI Task -> BE Response**

---

## Chunk 3: Contextual Personalized Feed

### Task 5: Context Integration (Weather & Traffic)
**Files:**
- Create: `backend/services/context_service.py`

- [ ] **Step 1: Integrate OpenWeather API for real-time weather**
- [ ] **Step 2: Integrate Goong/VietMap for traffic status**
- [ ] **Step 3: Aggregate context into a single DTO**

### Task 6: Personalized Feed Engine
**Files:**
- Create: `ai-core/agents/feed_agent.py`
- Modify: `backend/api/feed.py`

- [ ] **Step 1: Implement AI logic to rank POIs based on context (Weather + Social Trend)**
- [ ] **Step 2: Create endpoint `GET /feed` on Backend**
- [ ] **Step 3: Verify feed content adapts to "Rainy" vs "Sunny" mocks**

---

## Chunk 4: Mobile & Web Integration

### Task 7: Mobile Capture & Verification
- [ ] **Step 1: Basic Camera & GPS integration in Expo**
- [ ] **Step 2: Connect to `GET /storyline/next-task`**
- [ ] **Step 3: Implement `POST /capture` with photo upload to S3 + AI Verification call**

### Task 8: Web Dashboard & Vlog View
- [ ] **Step 1: Implement Map view (Goong) on Web**
- [ ] **Step 2: Create a page to display generated Vlogs**
- [ ] **Step 3: Connect to Backend for user history**

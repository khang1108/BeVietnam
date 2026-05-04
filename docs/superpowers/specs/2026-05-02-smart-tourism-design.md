# Spec: Agent-Based Smart Tourism System for Vietnam (Cultural Brain)

**Date:** 2026-05-02  
**Status:** Draft - Pending Review  
**Lead & AI Core:** User  
**Team Structure:** 2 BE, 1 Web FE, 1 Mobile FE, 1 AI Core (Lead)

---

## 1. Vision & Purpose
A hybrid mobile/web application that transforms tourism in Vietnam into a gamified cultural exploration. The system uses a "Cultural Brain" (AI Core) to guide users through storyline-based tasks, verify their discoveries via photos, and autonomously generate nightly vlogs.

## 2. Core Features

### 2.1 Storyline-Centric Tasks (Mobile-First)
- **Gamified Discovery:** Users follow a narrative path where completing a task (e.g., "Capture a yellow colonial wall in Hoi An") unlocks the next chapter or a hidden cultural "Bubble".
- **Hybrid Verification:** Simple tasks verified on-device (Edge AI); complex tasks verified by the AI Core on the backend.

### 2.2 Personalized Contextual Feed (Web & Mobile)
- **Context-Aware Recommendations:** The feed suggests cultural destinations based on:
    - **Current Context:** Location + Real-time Weather (via OpenWeather) + Traffic (via Goong).
    - **Social Trends:** Popularity among similar traveler profiles.
    - **Personal Interests:** Historical capture behavior.
- **Dynamic Content:** AI-generated snippets that explain *why* a destination is worth visiting *now* (e.g., "It's raining in Hue, perfect for the Imperial Museum's indoor exhibits").

### 2.3 Autonomous Vlog Generation (Nightly Job)
- **Narrative Assembly:** AI Core synthesizes validated captures into a coherent story.
- **Multi-Platform Consumption:** Mobile for quick sharing; Web for high-quality playback and memory management.

## 3. Technical Architecture

### 3.1 Components
- **AI Core (The Brain):**
    - **RAG Engine:** Vector DB (e.g., Qdrant) storing Vietnamese cultural heritage data.
    - **LLM Orchestrator:** Synthesizes tasks, feed snippets, and vlog narratives.
    - **Vision Validator:** Checks photo content against task requirements.
- **Unified Backend:**
    - Handles "On-demand" data fetching for bubbles and feed.
    - Manages task state and media storage (S3).
- **Frontend:**
    - **Mobile:** React Native/Flutter for "On-the-go" capture and navigation.
    - **Web:** React/Next.js for "Discovery & Memory" hub.

### 3.2 Data Flow (The Feed Example)
1. **Request:** Client requests feed with GPS + UserID.
2. **Context Enrichment:** BE fetches Weather/Traffic data.
3. **AI Ranking:** AI Core queries Vector DB + Context + User Profile.
4. **Response:** BE returns a prioritized, AI-described list of destinations.

## 4. Success Criteria
- AI-generated tasks are culturally accurate and engaging.
- The Feed provides relevant suggestions that adapt to weather/location changes.
- Seamless coordination between 1 AI Lead and 5 Engineering members.

---
*End of Spec.*

# Problem Decomposition — Multi-Agent Smart Tourism System (BeVietnam)

**MVP Scope:** Ho Chi Minh City

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Problem Decomposition](#2-problem-decomposition)
3. [Pattern Recognition](#3-pattern-recognition)
4. [Abstraction](#4-abstraction)
5. [Algorithm Design](#5-algorithm-design)
6. [System Architecture](#6-system-architecture)
7. [Detailed Pipeline Specifications](#7-detailed-pipeline-specifications)
8. [Technical Solution Summary](#8-technical-solution-summary)

---

## 1. Project Overview

### 1.1 Summary

**BeVietnam** is a smart tourism system built on a Multi-Agent System (MAS) architecture, focused on helping tourists (both domestic and international) discover the cultural depth of Vietnam — from street food hidden in narrow alleys, weekly local events, live music shows, to the regional cultural stories behind each destination.

Unlike conventional tourism applications that merely list popular attractions, BeVietnam employs a multi-agent system to:
- **Automatically collect** data from multiple sources (Google Maps, Facebook Events, food blogs, local news outlets)
- **Verify and cleanse** raw data into reliable, trustworthy information
- **Culturally enrich** — leverage AI to augment historical context, regional significance, and local dining etiquette
- **Culturally translate** — go beyond literal translation to explain Vietnamese concepts for foreign visitors
- **Continuously update** events, newly opened venues, and changes in operating hours

The core user experience is a **personalized feed** — scrolling through cultural stories, upcoming events, and culinary destinations AI-curated by region and preference, delivered on a mobile application.

### 1.2 Problem Statement

With the rapid growth of the domestic tourism market, the number of tourists visiting Vietnam has increased significantly. According to VnEconomy, in 2025 alone, international tourist arrivals to Vietnam rose by 20.4% year-over-year and 17.8% above 2019 (pre-COVID-19) levels, marking an all-time high.

However, the current tourism experience suffers from **four core problems**:

**P1 — Information overload with insufficient cultural depth.** Tourism information is fragmented across Google Maps, TripAdvisor, personal blogs, and social media. No single source provides adequate cultural context — why a small phở stall tucked in an alley is special, what region a dish originates from, or how locals truly enjoy it. Tourists see only names, addresses, and a few photos — lacking the "soul" of each destination.

**P2 — Foreign visitors struggle to grasp local culture.** When a French tourist stands before a "Bún bò Huế" stall, Google Translate merely outputs "Hue beef noodle soup" — failing to convey that this is the culinary pride of Central Vietnam, that its signature heat comes from lemongrass and chili powder, or that Huế locals eat bún bò in the morning as a daily ritual. The language barrier is only the surface — the cultural barrier is the real problem.

**P3 — Alley-hidden cuisine and local events remain undiscoverable.** Saigon's best eateries are often buried deep in alleys, without websites or Google Business listings. Street events, acoustic live shows at coffee shops, weekend night markets — the experiences tourists crave the most — are only promoted through local Facebook Groups or word-of-mouth. They are virtually invisible on international tourism platforms.

**P4 — Stale tourism data with no continuous update mechanism.** A restaurant closure, a cancelled event, seasonal changes in operating hours — information on current tourism apps often fails to reflect reality. Relying on crowdsourced reviews is not fast enough, and there is no automated system to verify data freshness.

### 1.3 Objectives

Build a Multi-Agent System capable of:

| ID | Objective | Success Metric |
|----|-----------|----------------|
| G1 | Automatically collect POI and event data from multiple sources | Minimum 200 POIs + 50 events for HCMC in MVP |
| G2 | Enrich each venue/dish with AI-generated cultural context | ≥ 80% items have cultural context after pipeline execution |
| G3 | Cultural translation Vi ↔ En (beyond literal translation) | Each item has both `description_vi` and `description_en` |
| G4 | Deliver a personalized feed on mobile application | Feed ranking based on ≥ 4 factors |
| G5 | Continuously update events (minimum every 6 hours) | Event Monitor Agent runs automatically on schedule |
| G6 | Enable community contributions and data maintenance | Community submission flow with AI moderation |
| G7 | Operate at near-zero cost | Entire stack runs on free tier |

---

## 2. Problem Decomposition

### 2.1 Decomposition Level 1 — Four Subsystems

The overall problem is divided into four subsystems, each with a clearly defined responsibility:

```
BeVietnam System
├── S1. Data Acquisition      — Collect raw data from external sources
├── S2. Data Intelligence     — Process, enrich, and translate data
├── S3. Content Delivery      — Deliver content to end users
└── S4. User Platform         — User interaction platform
```

| Subsystem | Problem Addressed | Input | Output |
|-----------|-------------------|-------|--------|
| **S1. Data Acquisition** | Data is scattered across multiple heterogeneous sources (P1, P3) | Source configs, keywords, geo-bounds | Raw data items (JSON) |
| **S2. Data Intelligence** | Raw data requires cleansing, deduplication, cultural enrichment, and translation (P1, P2, P4) | Raw data items | Clean, enriched, translated records |
| **S3. Content Delivery** | Users need relevant, curated content — not raw listings | All records + user context | Ranked, personalized feed |
| **S4. User Platform** | Users need an interface to interact and contribute (P3) | User actions | UI responses, submissions |

### 2.2 Decomposition Level 2 — Agents and Modules

Each subsystem is further decomposed into **agents** or **modules**:

```
S1. Data Acquisition
├── Agent 1: Crawler Agent           — Scrape data from Google Maps, food blogs, news outlets
└── Agent 2: Event Monitor Agent     — Track events from Facebook Events, Ticketbox, venues

S2. Data Intelligence
├── Agent 3: Cleaner Agent           — Validate, deduplicate, normalize
├── Agent 4: Cultural Enricher Agent — Add cultural context via LLM
└── Agent 5: Translator Agent        — Cultural translation Vi → En

S3. Content Delivery
├── Agent 6: Feed Curator Agent      — Rank and personalize the feed
└── Agent 7: Community Moderator Agent — Moderate community-contributed content

S4. User Platform
├── Module: Auth & Identity          — Authentication, session management
├── Module: Mobile App (Expo)        — User interface
└── Module: Community Submission     — Form for contributing venues/events

Cross-cutting:
└── Orchestrator                     — Scheduling, coordination, error handling
```

### 2.3 Decomposition Level 3 — Agent Specifications

#### Agent 1: Crawler Agent

| Attribute | Details |
|-----------|---------|
| **Responsibility** | Collect raw data from multiple external sources |
| **Input** | List of sources + query configuration (region, category, keywords) |
| **Output** | Raw data items persisted to `raw_crawl_data` table |
| **Trigger** | Orchestrator-scheduled daily (2:00 AM) |
| **Sources** | Google Maps Places API, food blogs (foody.vn, diadiemanuong.com), local news outlets |
| **Core Algorithm** | Adaptive crawl — prioritize sources with higher data freshness |
| **Error Handling** | Retry up to 3 times with exponential backoff; skip source on persistent failure |

#### Agent 2: Event Monitor Agent

| Attribute | Details |
|-----------|---------|
| **Responsibility** | Track and update local events on a weekly basis |
| **Input** | Source configurations for events (Facebook Events, Ticketbox, venue pages) |
| **Output** | Event records with status (upcoming / cancelled / completed) |
| **Trigger** | Every 6 hours (events change more frequently than places) |
| **Categories** | Live music, Art, Food festival, Workshop, Night market, Street performance |
| **Core Algorithm** | Change detection — diff new data against existing records, update only on changes |

#### Agent 3: Cleaner Agent

| Attribute | Details |
|-----------|---------|
| **Responsibility** | Validate, deduplicate, and normalize raw data |
| **Input** | Raw data from `raw_crawl_data` |
| **Output** | Clean records into `places`, `events`, `food_items` |
| **Trigger** | Runs immediately after Crawler Agent completes |
| **Core Algorithm** | Fuzzy deduplication (Levenshtein distance on name + address, combined with geo-proximity < 50m) |
| **Validation Rules** | Coordinates within HCMC bounding box, valid phone format, reachable URL |

#### Agent 4: Cultural Enricher Agent

| Attribute | Details |
|-----------|---------|
| **Responsibility** | Leverage LLM to add cultural context for each venue / dish |
| **Input** | Clean record (place or food_item) + category |
| **Output** | `cultural_context_vi` field — cultural narrative, history, significance |
| **Trigger** | After Cleaner Agent, only processes items lacking cultural context |
| **LLM** | Gemini 2.0 Flash (free tier: 15 RPM, 1M tokens/day) |
| **Core Algorithm** | Category-based prompt selection → LLM invocation → quality validation → persist or retry |

#### Agent 5: Translator Agent

| Attribute | Details |
|-----------|---------|
| **Responsibility** | Cultural translation Vi → En (beyond literal translation) |
| **Input** | Vietnamese content + cultural context |
| **Output** | `description_en`, `cultural_context_en` fields |
| **Trigger** | After Cultural Enricher Agent |
| **LLM** | Gemini Flash (shared quota with Enricher Agent) |
| **Key Differentiator** | "Bánh mì" → not "bread" but "Vietnamese baguette sandwich — a fusion of French colonial bread with local herbs, pâté, and pickled vegetables" |

#### Agent 6: Feed Curator Agent

| Attribute | Details |
|-----------|---------|
| **Responsibility** | Rank and generate a personalized feed for each user |
| **Input** | User context (location, locale, preferences, history) + all content |
| **Output** | Ranked list of feed items |
| **Trigger** | On-demand when user opens app or refreshes feed |
| **Core Algorithm** | Weighted scoring formula (6 factors) — detailed in Section 5.3 |

#### Agent 7: Community Moderator Agent

| Attribute | Details |
|-----------|---------|
| **Responsibility** | Moderate community-contributed content |
| **Input** | Community submission (new place, edit suggestion, event report) |
| **Output** | Status: Approved / Rejected / Needs Review |
| **Trigger** | On-demand when a new submission is received |
| **Pipeline** | Spam filter → Duplicate check → Content quality (LLM) → Auto-approve or queue |

#### Orchestrator

| Attribute | Details |
|-----------|---------|
| **Responsibility** | Scheduling, execution ordering, error handling, logging |
| **Scheduling** | Cron-based: daily crawl (2 AM), event monitor (every 6h) |
| **Dependency Resolution** | DAG: Crawler → Cleaner → Enricher → Translator |
| **Error Handling** | Retry up to 3 times, exponential backoff, log to `agent_logs` |
| **Observability** | Each agent run records: agent_name, duration_ms, status, input/output summary |

### 2.4 Agent Responsibility Matrix

The matrix below shows each agent's data operations:

| Agent | Read External Source | Write Raw Data | Read Raw Data | Write Clean Data | Read Clean Data | Invoke LLM | Write Feed | Read User Context |
|-------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| Crawler Agent | ✓ | ✓ | | | | | | |
| Event Monitor Agent | ✓ | ✓ | | | | | | |
| Cleaner Agent | | | ✓ | ✓ | | | | |
| Cultural Enricher Agent | | | | ✓ | ✓ | ✓ | | |
| Translator Agent | | | | ✓ | ✓ | ✓ | | |
| Feed Curator Agent | | | | | ✓ | | ✓ | ✓ |
| Community Moderator Agent | | | | ✓ | ✓ | ✓ | | |
| Orchestrator | | | | | | | | |

The Orchestrator does not directly manipulate data — it solely coordinates execution order and monitors the status of other agents.

---

## 3. Pattern Recognition

### 3.1 Data Patterns

**Pattern D1 — Cross-source duplicates.** The same phở restaurant appears on Google Maps (English name), on foody.vn (Vietnamese name), and on Facebook (abbreviated name). Three records with different names but referring to the same entity.
- **Detection**: Different names + similar addresses + geo-proximity (< 50m)
- **Reusable Solution**: Fuzzy matching + geo-proximity clustering → applicable across all entity types (places, events, food items)

**Pattern D2 — Source-specific missing fields.** Data from food blogs typically includes name + description but lacks coordinates. Data from Google Maps has coordinates but lacks pricing. Each source consistently lacks different fields in a predictable pattern.
- **Detection**: Source X always lacks field Y
- **Reusable Solution**: Source-specific field mapping + cross-source enrichment (merge data from multiple sources for the same entity)

**Pattern D3 — Spam and low-quality content.** Community submissions exhibit spam patterns: content too short (< 10 characters), containing advertising URLs, or duplicate of existing locations.
- **Detection**: Heuristic rules based on length, URL patterns, duplicate check
- **Reusable Solution**: Rule-based pre-filter executed before LLM quality check → saves API calls

### 3.2 User Behavior Patterns

**Pattern U1 — Content preference by locale.** Vietnamese tourists prioritize: events > food > culture. International tourists prioritize: culture > food > events. This pattern influences feed ordering.
- **Application**: Feed Curator Agent uses `user.locale` as a weight modifier in the ranking formula.

**Pattern U2 — Time-based interest.** Weekends: users search for events and entertainment more frequently. Noon: searching for restaurants. Evening: searching for cafes, bars, shows.
- **Application**: Feed Curator Agent applies time-based boost for categories matching the current time of day/week.

**Pattern U3 — Proximity decay.** User interest decays rapidly with distance — a restaurant 500m away is more attractive than one 5km away, even if the farther one has a higher rating.
- **Application**: Inverse distance weighting in the Feed Curator Agent.

### 3.3 Agent Pipeline Patterns

**Pattern P1 — ETL (Extract → Transform → Load).** The core pipeline Crawler → Cleaner → Enricher → Translator follows the classic ETL pattern. Recognizing this pattern allows leveraging established best practices: idempotency, checkpoint/restart, per-stage logging.
- **Reuse**: All 4 agents in the chain implement the same interface `process(batch) → results + errors`.

**Pattern P2 — Event-driven trigger.** When a user submits new content → triggers Community Moderator Agent. When Crawler completes → triggers Cleaner. This is a recurring event-driven pattern.
- **Reuse**: Design Orchestrator around a callback/event system rather than polling.

**Pattern P3 — Request-response with caching.** Each time a user opens the feed → Feed Curator Agent computes ranking. However, underlying data (places, events) does not change continuously. Pattern: cache ranking results, invalidate when underlying data changes.
- **Reuse**: Cache layer before Feed Curator Agent, TTL = 30 minutes for feed, 6 hours for place data.

### 3.4 Summary: Pattern → Design Decision

| Pattern ID | Pattern | Design Decision |
|------------|---------|-----------------|
| D1 | Cross-source duplicates | Fuzzy deduplication algorithm in Cleaner Agent |
| D2 | Source-specific missing fields | Source-specific adapters + cross-source merge |
| D3 | Spam patterns | Rule-based pre-filter before LLM moderation |
| U1 | Locale-based preference | Locale weight in Feed ranking formula |
| U2 | Time-based interest | Time-of-day/week boost in Feed ranking |
| U3 | Proximity decay | Inverse distance weighting |
| P1 | ETL pipeline | Shared agent interface: `process(batch)` |
| P2 | Event-driven trigger | Orchestrator callback system |
| P3 | Request-response caching | Cache layer with TTL-based invalidation |

---

## 4. Abstraction

This section identifies the abstraction layers that hide unnecessary implementation details and make the system easier to understand and extend.

### 4.1 Agent Interface Abstraction

Although the 7 agents have vastly different internal logic (web crawling, LLM invocation, ranking computation), externally they all conform to the same interface:

```
BaseAgent
├── name: string                          — Unique identifier
├── process(input: AgentInput) → AgentOutput  — Core processing
├── validate(input: AgentInput) → bool    — Input validation
└── on_error(error: Error) → RetryOrSkip  — Error handling
```

```
AgentInput
├── batch: list[Record]    — Data to process
├── config: dict           — Agent-specific configuration
└── run_id: string         — ID for tracing

AgentOutput
├── results: list[Record]  — Processed data
├── errors: list[Error]    — Errors encountered
└── metrics: dict          — duration_ms, items_processed, items_failed
```

Thanks to this uniform interface, the Orchestrator does not need to know the internal details of each agent — it simply invokes `agent.process(input)` and receives `output`. Modifying the internal logic of one agent does not affect the Orchestrator or other agents.

### 4.2 Data Model Abstraction

The three primary entity types (Place, Event, FoodItem) share many common fields. Rather than designing three entirely separate models, common fields are abstracted into a base entity:

```
BaseEntity (abstract)
├── id: UUID
├── name: string
├── description_vi: text
├── description_en: text
├── cultural_context_vi: text
├── cultural_context_en: text
├── category: string
├── source: string             — Original data source
├── source_id: string          — ID on the original source
├── verified: boolean          — Has been verified?
├── created_at: timestamp
└── updated_at: timestamp

Place extends BaseEntity
├── lat: float
├── lng: float
├── address: string
├── district: string
├── avg_rating: float
└── sentiment_score: float

Event extends BaseEntity
├── venue_place_id: FK → Place
├── start_time: timestamp
├── end_time: timestamp
├── recurrence: string         — "weekly", "monthly", "one-time"
├── status: string             — "upcoming", "cancelled", "completed"
└── ticket_url: string

FoodItem extends BaseEntity
├── place_id: FK → Place
├── name_vi: string
├── name_en: string
├── cultural_story_vi: text
├── cultural_story_en: text
├── price_range: string
└── is_signature: boolean
```

Through this abstraction, data-processing agents (Cleaner, Enricher, Translator) can share common logic for base fields — only requiring entity-specific logic for type-specific fields.

### 4.3 Pipeline Abstraction

Three recurring pipeline patterns are abstracted into three templates:

**Template 1 — Sequential Pipeline** (Crawler → Cleaner → Enricher → Translator)
```
SequentialPipeline
├── stages: list[Agent]            — List of agents to execute sequentially
├── run() → PipelineResult         — Execute stage 1 → output → stage 2 → ...
└── checkpoint_after_each: bool    — Persist intermediate results for resume on failure
```

**Template 2 — On-Demand Pipeline** (Feed Curator, Community Moderator)
```
OnDemandPipeline
├── agent: Agent                   — Agent handling the request
├── handle(request) → response     — Process a single request
└── cache_ttl: int                 — Cache duration in seconds
```

**Template 3 — Scheduled Pipeline** (Event Monitor)
```
ScheduledPipeline
├── agent: Agent                   — Agent running periodically
├── interval: string               — "6h", "24h", "weekly"
└── run_and_diff() → changes       — Execute and return only deltas from previous run
```

### 4.4 System-Wide Abstraction Layers

The system is organized into abstraction layers, where each layer communicates only with its adjacent layers:

```
┌─────────────────────────────────────────────────┐
│  Layer 5: Mobile App (Expo/React Native)        │  ← User interaction
├─────────────────────────────────────────────────┤
│  Layer 4: REST API (FastAPI)                    │  ← Client-server communication
├─────────────────────────────────────────────────┤
│  Layer 3: Agent Layer (7 Agents + Orchestrator) │  ← AI business logic
├─────────────────────────────────────────────────┤
│  Layer 2: Data Store (Postgres + Object Storage)│  ← Persistence
├─────────────────────────────────────────────────┤
│  Layer 1: External Adapters (APIs + Scrapers)   │  ← External data sources
└─────────────────────────────────────────────────┘
```

Example: The Mobile App (Layer 5) never directly invokes the Gemini API (Layer 1). It calls the REST API (Layer 4), which routes the request to the Feed Curator Agent (Layer 3), which reads data from Postgres (Layer 2). Each layer encapsulates the implementation details of the layer below.

---

## 5. Algorithm Design

### 5.1 Orchestrator Scheduling Algorithm

The Orchestrator manages agent execution order based on a DAG (Directed Acyclic Graph) of dependencies:

```
DAG Dependencies:
  Crawler Agent      → Cleaner Agent
  Event Monitor Agent → Cleaner Agent
  Cleaner Agent      → Cultural Enricher Agent
  Cultural Enricher  → Translator Agent
  Translator Agent   → (pipeline complete)
  Feed Curator Agent → (on-demand, not in DAG)
  Community Moderator → (on-demand, not in DAG)
```

**Pseudocode — DAG Executor:**

```
function execute_pipeline(dag):
    ready_queue = agents with 0 unresolved dependencies
    completed = {}

    while ready_queue is not empty:
        agent = ready_queue.dequeue()
        result = run_with_retry(agent, max_retries=3)

        if result.status == SUCCESS:
            completed.add(agent)
            for each downstream in dag.dependents(agent):
                if all dependencies of downstream in completed:
                    ready_queue.enqueue(downstream)
        else:
            log_failure(agent, result.errors)
            // Downstream agents are not enqueued → pipeline halts on this branch

    return completed
```

**Pseudocode — Retry with Exponential Backoff:**

```
function run_with_retry(agent, max_retries):
    for attempt in 1..max_retries:
        try:
            output = agent.process(input)
            log(agent.name, "SUCCESS", output.metrics)
            return output
        catch error:
            wait_time = 2^attempt * 1000ms    // 2s, 4s, 8s
            log(agent.name, "RETRY", attempt, error)
            sleep(wait_time)

    log(agent.name, "FAILED after max retries")
    return FailureResult(errors)
```

### 5.2 Deduplication Algorithm (Cleaner Agent)

When data arrives from multiple sources, the same restaurant may appear multiple times under different names. The deduplication algorithm combines three signals:

**Pseudocode:**

```
function deduplicate(new_record, existing_records):
    candidates = []

    for each existing in existing_records:
        score = 0

        // Signal 1: Source ID match (same source, same ID → definite duplicate)
        if new_record.source == existing.source
           AND new_record.source_id == existing.source_id:
            return DUPLICATE(existing)     // exact match

        // Signal 2: Geo-proximity
        distance = haversine(new_record.lat, new_record.lng,
                             existing.lat, existing.lng)
        if distance < 50 meters:
            score += 0.5

        // Signal 3: Fuzzy name matching
        name_similarity = 1 - levenshtein_ratio(
            normalize(new_record.name),
            normalize(existing.name)
        )
        if name_similarity > 0.7:
            score += name_similarity * 0.5

        if score >= 0.7:
            candidates.append((existing, score))

    if candidates:
        best_match = max(candidates, key=score)
        if best_match.score >= 0.85:
            return DUPLICATE(best_match)   // high confidence
        else:
            return MAYBE_DUPLICATE(best_match)  // requires review

    return NEW_RECORD

function normalize(name):
    // Remove Vietnamese diacritics, lowercase, strip special characters
    return remove_diacritics(name).lower().strip()
```

**Haversine distance** (compute distance between two GPS coordinates):

```
function haversine(lat1, lng1, lat2, lng2):
    R = 6371000  // Earth's radius (meters)
    φ1, φ2 = radians(lat1), radians(lat2)
    Δφ = radians(lat2 - lat1)
    Δλ = radians(lng2 - lng1)

    a = sin(Δφ/2)² + cos(φ1) * cos(φ2) * sin(Δλ/2)²
    c = 2 * atan2(√a, √(1-a))

    return R * c   // distance in meters
```

### 5.3 Feed Ranking Algorithm (Feed Curator Agent)

Each content item in the feed is scored using a weighted scoring formula:

```
feed_score(item, user) =
      w₁ × recency(item)
    + w₂ × proximity(item, user)
    + w₃ × popularity(item)
    + w₄ × diversity_bonus(item, current_feed)
    + w₅ × cultural_depth(item)
    + w₆ × event_urgency(item)
```

**Factor details:**

```
function recency(item):
    // Exponential decay: more recent → higher score
    hours_ago = (now - item.updated_at).total_hours()
    return e^(-0.01 × hours_ago)     // decay rate λ = 0.01

function proximity(item, user):
    // Inverse distance: closer → higher score
    d = haversine(item.lat, item.lng, user.lat, user.lng)
    return 1 / (1 + d / 1000)        // normalize by km

function popularity(item):
    // Log-scaled to prevent popular items from dominating
    interactions = item.view_count + 2 × item.save_count + 3 × item.like_count
    return log(1 + interactions) / log(1 + MAX_INTERACTIONS)

function diversity_bonus(item, current_feed):
    // Bonus if category has not appeared frequently in the current feed
    category_count = count(current_feed, item.category)
    return 1 / (1 + category_count)   // diminishes as same category appears more

function cultural_depth(item):
    // Items with deeper cultural context → higher priority
    if item.cultural_context is not empty:
        return len(item.cultural_context) / MAX_CONTEXT_LENGTH
    return 0

function event_urgency(item):
    // Applies only to events: imminent events → boosted
    if item.type != "event": return 0
    hours_until = (item.start_time - now).total_hours()
    if hours_until < 0: return 0      // already past
    if hours_until < 24: return 1.0   // within 24 hours
    if hours_until < 72: return 0.5   // within 3 days
    return 0.1
```

**Default weights** (adjusted based on `user.locale`):

| Weight | Domestic Tourists | International Tourists |
|--------|:-:|:-:|
| w₁ (recency) | 0.20 | 0.15 |
| w₂ (proximity) | 0.25 | 0.20 |
| w₃ (popularity) | 0.15 | 0.10 |
| w₄ (diversity) | 0.10 | 0.10 |
| w₅ (cultural_depth) | 0.10 | 0.25 |
| w₆ (event_urgency) | 0.20 | 0.20 |

International tourists receive a higher `cultural_depth` weight (0.25 vs. 0.10) because this is the core value proposition BeVietnam delivers to them.

### 5.4 Cultural Enrichment Algorithm (Cultural Enricher Agent)

```
function enrich_batch(items):
    prompt_templates = load_templates_by_category()
    results = []

    for item in items:
        if item.cultural_context_vi is not empty:
            skip    // already enriched

        template = prompt_templates[item.category]
        prompt = template.format(
            name=item.name,
            district=item.district,
            category=item.category,
            description=item.description_vi
        )

        response = call_llm(prompt, model="gemini-flash")

        // Quality validation
        if len(response) < 50:
            retry with more specific prompt
        if contains_hallucination_markers(response):
            discard and log warning

        item.cultural_context_vi = response
        results.append(item)

    return results

function contains_hallucination_markers(text):
    // Detect indicators of LLM fabrication
    markers = ["according to legend", "there is no exact information",
               "I am not certain", "it could be"]
    return any(marker in text.lower() for marker in markers)
```

### 5.5 Community Moderation Algorithm (Community Moderator Agent)

```
function moderate(submission):
    // Stage 1: Rule-based spam filter (fast, no LLM cost)
    spam_score = 0
    if len(submission.description) < 10: spam_score += 0.4
    if contains_url_patterns(submission.description): spam_score += 0.3
    if submission.user.account_age < 1 day: spam_score += 0.2
    if user_submission_count_today(submission.user) > 5: spam_score += 0.3

    if spam_score >= 0.7:
        return REJECTED(reason="spam detected")

    // Stage 2: Duplicate check
    dedup_result = deduplicate(submission, existing_records)
    if dedup_result == DUPLICATE:
        return REJECTED(reason="duplicate of " + dedup_result.match.id)

    // Stage 3: LLM content quality assessment
    quality_prompt = f"""
    Evaluate the following community contribution for a tourism system:
    Name: {submission.name}
    Description: {submission.description}
    Type: {submission.entity_type}

    Score 1-10 on: (a) accuracy, (b) usefulness for tourists,
    (c) does it contain inappropriate content?
    Respond in JSON: {{"accuracy": N, "usefulness": N, "inappropriate": bool}}
    """

    quality = call_llm(quality_prompt, model="gemini-flash")

    if quality.inappropriate:
        return REJECTED(reason="inappropriate content")
    if quality.accuracy >= 7 AND quality.usefulness >= 6:
        return APPROVED
    else:
        return NEEDS_REVIEW(quality_scores=quality)
```

---

## 6. System Architecture

### 6.1 High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                    EXTERNAL SOURCES (Layer 1)                       │
│  ┌──────────┐  ┌──────────────┐  ┌───────────┐  ┌───────────────┐  │
│  │ Google   │  │ Facebook     │  │ Food      │  │ Gemini Flash  │  │
│  │ Maps API │  │ Events API   │  │ Blogs     │  │ LLM API       │  │
│  └────┬─────┘  └───────┬──────┘  └──────┬────┘  └────────┬──────┘  │
└───────┼────────────────┼────────────────┼────────────────┼──────────┘
        │                │                │                │
┌───────▼────────────────▼────────────────▼────────────────▼──────────┐
│                    AGENT LAYER (Layer 3)                             │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                     ORCHESTRATOR                            │    │
│  │  ┌─────────┐  ┌─────────────┐  ┌──────────┐  ┌──────────┐  │    │
│  │  │Scheduler│  │DAG Executor │  │Retry Mgr │  │Logger    │  │    │
│  │  └─────────┘  └─────────────┘  └──────────┘  └──────────┘  │    │
│  └─────────────────────┬───────────────────────────────────────┘    │
│                        │ orchestrates                                │
│  ┌──────────┐ ┌────────┴──┐ ┌───────────┐ ┌────────────┐           │
│  │ Crawler  │→│  Cleaner  │→│ Cultural  │→│ Translator │           │
│  │ Agent    │ │  Agent    │ │ Enricher  │ │ Agent      │           │
│  └──────────┘ └───────────┘ └───────────┘ └────────────┘           │
│  ┌──────────────┐  ┌────────────┐  ┌─────────────────┐             │
│  │Event Monitor │  │Feed Curator│  │Community        │             │
│  │Agent         │  │Agent       │  │Moderator Agent  │             │
│  └──────────────┘  └────────────┘  └─────────────────┘             │
└────────────────────────────┬────────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────────┐
│                    DATA STORE (Layer 2)                              │
│  ┌──────────────────┐  ┌─────────────────┐  ┌───────────────────┐  │
│  │ Postgres (Supa.) │  │ Object Storage  │  │ Auth (Supabase)   │  │
│  │ places, events,  │  │ images          │  │ JWT tokens        │  │
│  │ food_items, users│  │                 │  │                   │  │
│  └────────┬─────────┘  └─────────────────┘  └───────────────────┘  │
└───────────┼─────────────────────────────────────────────────────────┘
            │
┌───────────▼─────────────────────────────────────────────────────────┐
│                    API LAYER (Layer 4) — FastAPI                     │
│  GET /feed     GET /places    GET /events    POST /community        │
│  GET /place/:id   GET /event/:id   GET /search   POST /auth        │
└───────────┬─────────────────────────────────────────────────────────┘
            │ REST (JSON)
┌───────────▼─────────────────────────────────────────────────────────┐
│                    CLIENT (Layer 5) — Expo / React Native           │
│  ┌──────────┐ ┌───────────┐ ┌────────┐ ┌───────────┐ ┌─────────┐  │
│  │Feed      │ │Explore    │ │Detail  │ │Events     │ │Community│  │
│  │Screen    │ │Map Screen │ │Screen  │ │Calendar   │ │Submit   │  │
│  └──────────┘ └───────────┘ └────────┘ └───────────┘ └─────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

### 6.2 Primary Data Flow

```
[Google Maps] ──┐
[Food Blogs] ───┤──→ Crawler Agent ──→ raw_crawl_data (Postgres)
[Local News] ───┘                              │
                                               ▼
[Facebook Events] ──→ Event Monitor ──→ Cleaner Agent ──→ places / events / food_items
[Ticketbox]  ───────────────┘                  │
                                               ▼
                                    Cultural Enricher Agent
                                    (+ Gemini Flash LLM)
                                               │
                                               ▼
                                    Translator Agent
                                    (+ Gemini Flash LLM)
                                               │
                                               ▼
                                    Clean, enriched, translated data
                                    stored in Postgres
                                               │
                                               ▼
                   User opens app ──→ Feed Curator Agent ──→ Ranked feed ──→ Mobile App
```

### 6.3 User Interaction Flows

**Flow 1 — View Feed (primary):**
```
User opens app → GET /feed?lat=X&lng=Y&locale=vi
  → API invokes Feed Curator Agent
    → Agent reads places + events + food_items from Postgres
    → Computes feed_score for each item
    → Sort descending, take top 20
  → API returns JSON response
→ Mobile renders feed cards
```

**Flow 2 — View Venue Details:**
```
User taps feed card → GET /place/:id
  → API reads place + food_items + media from Postgres
  → Returns full detail (including cultural_context)
→ Mobile renders detail screen
  → Displays cultural story, photos, map, signature dishes
```

**Flow 3 — Community Contribution:**
```
User taps "Add Place" → Fills form → POST /community
  → API creates community_submission
  → Triggers Community Moderator Agent
    → Spam filter → Dedup check → LLM quality check
    → If APPROVED: insert into places/events
    → If REJECTED: notify with reason
  → API returns result
→ Mobile displays submission status
```

### 6.4 Agent Orchestration Diagram

```
                    ┌───────────────┐
                    │  Orchestrator │
                    │  (Scheduler)  │
                    └──────┬────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
        ┌─────▼──────┐ ┌───▼────┐ ┌────▼───────────┐
        │ Daily Job  │ │ 6h Job │ │ On-Demand Jobs │
        │ (2:00 AM)  │ │        │ │                │
        └─────┬──────┘ └───┬────┘ └────┬───────────┘
              │            │           │
    ┌─────────▼──────┐  ┌──▼──────────┐│  ┌──────────────────┐
    │ Crawler Agent  │  │Event Monitor││  │ Feed Curator     │ ← user request
    └─────────┬──────┘  └──┬──────────┘│  └──────────────────┘
              │            │           │  ┌──────────────────┐
              └─────┬──────┘           └──│Community Mod     │ ← user submission
                    ▼                     └──────────────────┘
           ┌────────────────┐
           │ Cleaner Agent  │
           └───────┬────────┘
                   ▼
           ┌────────────────────┐
           │Cultural Enricher   │
           └───────┬────────────┘
                   ▼
           ┌────────────────┐
           │Translator Agent│
           └───────┬────────┘
                   ▼
           Pipeline Complete
           (log to agent_logs)
```

---

## 7. Detailed Pipeline Specifications

### 7.1 Data Acquisition Pipeline

**Objective:** Collect raw data about venues, restaurants, and events from multiple external sources.

**Sources and per-source strategy:**

| Source | Method | Data Collected | Limitations |
|--------|--------|---------------|-------------|
| Google Maps Places API | REST API (Nearby Search) | Name, coordinates, rating, reviews, operating hours, photos | $200/month free credit |
| Food blogs (foody.vn, diadiemanuong.com) | Web scraping (BeautifulSoup + httpx) | Restaurant name, description, price, photos, reviews | Must respect robots.txt, rate limiting |
| Facebook Events | Graph API (public events) | Event name, time, venue, description | Public events only, API restrictions |
| Ticketbox | Web scraping | Live shows, workshops, times, ticket prices | Structured data, low churn |
| Local news outlets | Web scraping (RSS + HTML) | Events, festivals, tourism news | Unstructured, requires NLP extraction |

**Processing flow:**

```
1. Orchestrator invokes Crawler Agent with configuration:
   - sources: ["google_maps", "foody", "diadiemanuong", "ticketbox"]
   - geo_bounds: {sw: [10.65, 106.55], ne: [10.90, 106.85]}  // HCMC
   - categories: ["food", "entertainment", "culture", "event"]

2. Crawler Agent runs each source adapter:
   a. google_maps_adapter.crawl(bounds, categories) → raw items
   b. foody_adapter.crawl(bounds) → raw items
   c. ...

3. Each raw item is normalized to a common schema:
   {
     source: "google_maps",
     source_id: "ChIJ...",
     raw_data: { ... original response ... },
     entity_type: "place",
     crawled_at: "2026-04-06T02:15:00Z",
     status: "pending"
   }

4. Insert into raw_crawl_data table.

5. Log: "Crawled 150 items from 4 sources in 45s"
```

### 7.2 Data Processing Pipeline

**Objective:** Transform raw data into clean, culturally enriched, multilingual records.

**Stage 1 — Cleaner Agent:**

```
Input: raw_crawl_data WHERE status = 'pending'

Step 1: Validate
  - Coordinates within HCMC bounding box? (10.65 ≤ lat ≤ 10.90, 106.55 ≤ lng ≤ 106.85)
  - Name not empty?
  - Category belongs to permitted taxonomy?
  → Invalid items: status = 'rejected', log reason

Step 2: Normalize
  - Address: standardize "Q.1" → "Quận 1", "p.Bến Nghé" → "Phường Bến Nghé"
  - Category: map from source-specific → unified taxonomy
  - Phone: standardize format (+84...)
  - Name: trim, capitalize

Step 3: Deduplicate
  - Run dedup algorithm (Section 5.2) against existing records
  - DUPLICATE → merge fields (prefer newer data), update existing record
  - MAYBE_DUPLICATE → flag for review
  - NEW_RECORD → insert into places/events/food_items

Step 4: Update status
  - raw_crawl_data.status = 'processed'

Output: Clean records in places/events/food_items
```

**Stage 2 — Cultural Enricher Agent:**

```
Input: places/food_items WHERE cultural_context_vi IS NULL

Step 1: Select prompt template by category
  - "food" → food_culture_prompt.txt
  - "entertainment" → entertainment_prompt.txt
  - "historical" → history_prompt.txt

Step 2: Invoke Gemini Flash API
  - Rate limit: max 15 requests/minute (free tier)
  - Batch processing: process 15 items per minute

Step 3: Validate response
  - Length >= 50 chars? (too short = generic LLM response)
  - No hallucination markers detected?
  - Pass → save cultural_context_vi
  - Fail → retry once with a more specific prompt; if still fails → skip, log warning

Output: Records with cultural_context_vi populated
```

**Stage 3 — Translator Agent:**

```
Input: records WHERE description_en IS NULL AND description_vi IS NOT NULL

Step 1: Invoke Gemini Flash with cultural translation prompt
  - Does not use conventional machine translation
  - Prompt requires "cultural translation" — explaining Vietnamese concepts to foreigners
  - Example: "Translate and explain for a foreign tourist who knows nothing
    about Vietnamese culture. Don't just translate words — explain meaning."

Step 2: Persist description_en, cultural_context_en

Output: Records with complete Vi + En content
```

### 7.3 Event Monitoring Pipeline

**Objective:** Track local events and update more frequently than static data.

```
Runs every 6 hours:

1. Crawl events from sources (Facebook Events, Ticketbox, venue pages)

2. Diff against existing events in DB:
   a. New event (source_id not in DB) → Insert with status "upcoming"
   b. Existing event with changes (time, venue, description) → Update
   c. Event past end_time → status = "completed"
   d. Event no longer appearing on source → status = "cancelled" (soft delete)

3. Route new events through Cleaner → Enricher → Translator pipeline

4. Notify Feed Curator: invalidate cache for event-related feeds

5. Log: "Event monitor: 5 new, 3 updated, 12 completed, 1 cancelled"
```

### 7.4 Feed Generation Pipeline

**Objective:** When the user opens the app, return a personalized feed within < 500ms.

```
1. User request: GET /feed?lat=10.78&lng=106.70&locale=en&page=1

2. Check cache:
   - Cache key = hash(lat_rounded, lng_rounded, locale, page)
   - If cache hit and TTL < 30 minutes → return cached feed

3. If cache miss → Feed Curator Agent:
   a. Query candidates from Postgres:
      - Places within 5km radius of user
      - Events with status "upcoming" and start_time within next 7 days
      - Food items at queried places
      - Limit: 200 candidates

   b. Compute feed_score for each candidate (algorithm in Section 5.3)

   c. Sort by feed_score descending

   d. Diversity pass:
      - Traverse top-down; if 3 consecutive items share the same category → swap
        the 3rd item with the next item of a different category
      - Ensures feed diversity, preventing all-food or all-events feeds

   e. Paginate: retrieve items for requested page (20 items/page)

4. Cache result (TTL = 30 minutes)

5. Return JSON response with feed items
```

### 7.5 Community Pipeline

**Objective:** Enable community contributions of new venues and maintain data freshness.

```
1. User fills out form on app:
   - Venue/event name
   - Description (Vi or En)
   - Category
   - Coordinates (from map pin or current GPS)
   - Photos (optional)

2. POST /community → create community_submission with status "pending"

3. Community Moderator Agent processes (algorithm in Section 5.5):
   a. Spam filter (rule-based, instant)
   b. Duplicate check (against existing data)
   c. LLM quality assessment (Gemini Flash)

4. Result:
   - APPROVED → Insert into places/events → Route through Enricher + Translator
   - REJECTED → Notify user with reason
   - NEEDS_REVIEW → Queue for admin/team review (MVP: manual check)

5. User receives notification about submission status
```

---

## 8. Technical Solution Summary

### 8.1 Proposed Technology Stack

| Layer | Technology | Role |
|-------|-----------|------|
| Mobile | Expo (React Native) + Expo Router | Cross-platform mobile application |
| Backend API | Python FastAPI | REST API, co-located with agent code |
| Agent Framework | Python (custom) | 7 agents + Orchestrator, custom-built to demonstrate algorithm design |
| Database | Supabase (Postgres) | Primary storage, authentication, object storage |
| AI/LLM | Google Gemini 2.0 Flash | Cultural enrichment, translation, moderation |
| Map | react-native-maps + Google Maps SDK | Interactive map on mobile |
| Web Scraping | BeautifulSoup + httpx | Collect data from food blogs, event pages |
| Scheduling | APScheduler | Cron jobs for agent orchestration |
| Hosting | Render Free Tier | Backend deployment |

### 8.2 Cost Analysis

The entire system is designed to run on free tiers, suitable for an academic project budget:

| Service | Free Tier | Optimization Strategy |
|---------|-----------|----------------------|
| Supabase | 500MB DB, 1GB storage, 50K auth users | Scope to HCMC only; purge processed raw_crawl_data; compress images |
| Gemini Flash | 15 RPM, 1M tokens/day | Cache enrichment results; only enrich items lacking context; batch processing |
| Render | 750h/month, sleeps after 15 min inactivity | Use cron-job.org for keep-alive; accept cold start delay |
| Google Maps SDK | Free for mobile apps | No limitations for MVP |

**Estimated usage:**
- 200 POIs × enrichment prompt (~200 tokens/item) = 40K tokens (<<< 1M/day limit)
- 200 POIs × translation prompt (~300 tokens/item) = 60K tokens
- Event monitoring every 6h × ~50 events = 200 LLM calls/day (<<< 15 RPM = 21,600/day)
- DB storage: 200 places × ~2KB + 100 events × ~1KB + metadata ≈ < 1MB (<<< 500MB limit)

### 8.3 Risks and Mitigation

| # | Risk | Severity | Mitigation |
|---|------|----------|------------|
| R1 | **LLM hallucination** — Gemini fabricates incorrect cultural information | High | Hallucination detection in prompt output; only enrich verifiable facts; disclaimer on UI |
| R2 | **Free tier rate limits** — exceeding Gemini or Supabase quotas | Medium | Rate limiter in agent code; aggressive caching; batch processing during off-peak hours |
| R3 | **Web scraping blocked** — Food blogs block scraper | Medium | Respect robots.txt; rate limit requests; fallback to alternative sources; community contributions as supplement |
| R4 | **Stale data** — closed restaurant still displayed | Medium | Community report flow; periodic re-crawl; "Verify" button on UI |
| R5 | **2-month timeline** — scope too large for 4–6 person team | High | Strict MVP: HCMC only, feed + detail + community only; cut map explore if needed; parallelize frontend and backend work |

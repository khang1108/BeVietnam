# Recommendation Database Inspiration

This note translates patterns from large recommendation systems into a practical database design for the BeVietnam tourism app.

## 1. What Big Systems Commonly Do

Large systems usually split recommendation into two stages:

1. Candidate retrieval: quickly find a few hundred or thousand possible items.
2. Ranking: score those candidates using richer context and user/item features.

YouTube describes this as separate candidate generation and ranking models. Pinterest systems such as Pixie and PinSage use graph structure and embeddings to retrieve related items at large scale. Shopee's LightSAGE work also emphasizes item retrieval with graph construction from strong user behavior signals and collaborative filtering.

For this project, the database should support the same idea at MVP scale:

- store clean user behavior events;
- tag places and quests with stable interests;
- maintain user interest weights;
- optionally store recommendation candidates and feature snapshots later.

## 2. Translate To Tourism App

| Big-tech concept | Tourism app equivalent | Tables needed |
|---|---|---|
| User-item interaction | user views/saves/checks in/completes quest | `user_events`, `user_interest_events` |
| Item catalog | tourism places, cultural sites, events, quests | `places`, `quests`, `events` |
| Item taxonomy | product category, video topic, pin topic | `interests`, `place_interests`, `quest_interests` |
| Candidate retrieval | find matching places/quests | tags, embeddings, popularity, context |
| Ranking features | personalized feed score | `user_interests`, context snapshots, ratings |
| Real-time context | session/location/time/weather | `recommendation_requests`, `place_context_snapshots` |

## 3. Recommended Interest Model

Use interests as normalized tags, then connect users, places, and quests to the same catalog.

```sql
CREATE TABLE interests (
    interest_id BINARY(16) PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

Example categories:

- `culture`
- `food`
- `activity`
- `travel_style`
- `context`
- `event`
- `accessibility`

Example interest codes:

- `street_food`
- `history`
- `architecture`
- `museum`
- `hidden_gem`
- `coffee`
- `festival`
- `rainy_day_activity`
- `family_friendly`
- `photography`

## 4. User Preference State

This table stores the current profile that the feed/quest system can query quickly.

```sql
CREATE TABLE user_interests (
    user_interest_id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    interest_id BINARY(16) NOT NULL,
    weight DECIMAL(6,3) NOT NULL DEFAULT 1.000,
    preference_type ENUM('like', 'neutral', 'dislike') NOT NULL DEFAULT 'like',
    source ENUM('onboarding', 'manual', 'behavior', 'ai_inferred') NOT NULL DEFAULT 'onboarding',
    confidence DECIMAL(5,4) NOT NULL DEFAULT 1.0000,
    last_signal_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (interest_id) REFERENCES interests(interest_id),
    UNIQUE (user_id, interest_id),
    INDEX idx_user_interests_user_weight (user_id, weight)
);
```

Design rule:

- `user_interests` is the latest compact state.
- It should be updated from behavior events, not replace behavior events.

## 5. Behavior Event Log

Shopee-style and Pinterest-style systems care a lot about strong user behavior. For tourism, strong signals are actions like saving, checking in, completing a quest, or rating highly.

```sql
CREATE TABLE user_events (
    event_id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    target_type ENUM('place', 'quest', 'event', 'vlog_post', 'interest') NOT NULL,
    target_id BINARY(16) NOT NULL,
    signal_weight DECIMAL(6,3) NOT NULL,
    session_id BINARY(16),
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_user_events_user_time (user_id, occurred_at),
    INDEX idx_user_events_target (target_type, target_id),
    INDEX idx_user_events_type_time (event_type, occurred_at)
);
```

Suggested event weights:

| Event | Weight | Meaning |
|---|---:|---|
| `view_place` | 0.2 | weak interest |
| `open_place_detail` | 0.5 | stronger than impression |
| `save_place` | 1.5 | clear intent |
| `check_in` | 2.5 | visited in real life |
| `complete_quest` | 3.0 | strong cultural interest |
| `skip_quest` | -1.0 | negative preference |
| `rate_high` | 2.0 | positive feedback |
| `rate_low` | -2.0 | negative feedback |

## 6. Interest Event Derivation

This table makes it easier to update `user_interests` without reprocessing every raw event.

```sql
CREATE TABLE user_interest_events (
    user_interest_event_id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    interest_id BINARY(16) NOT NULL,
    source_event_id BINARY(16),
    delta_weight DECIMAL(6,3) NOT NULL,
    reason VARCHAR(120),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (interest_id) REFERENCES interests(interest_id),
    FOREIGN KEY (source_event_id) REFERENCES user_events(event_id),
    INDEX idx_interest_events_user_time (user_id, created_at),
    INDEX idx_interest_events_interest_time (interest_id, created_at)
);
```

Example:

- User completes a `street_food` quest.
- Insert `user_events`: `complete_quest`, weight `3.0`.
- Insert `user_interest_events` for `street_food`, `hidden_gem`, maybe `local_culture`.
- Update `user_interests.weight`.

## 7. Tag Places And Quests

Places and quests should connect to the same `interests` catalog.

```sql
CREATE TABLE place_interests (
    place_id BINARY(16) NOT NULL,
    interest_id BINARY(16) NOT NULL,
    relevance_score DECIMAL(6,3) NOT NULL DEFAULT 1.000,
    source ENUM('manual', 'imported', 'ai_inferred') NOT NULL DEFAULT 'manual',

    PRIMARY KEY (place_id, interest_id),
    FOREIGN KEY (place_id) REFERENCES places(place_id),
    FOREIGN KEY (interest_id) REFERENCES interests(interest_id),
    INDEX idx_place_interests_interest (interest_id, relevance_score)
);
```

```sql
CREATE TABLE quest_interests (
    quest_id BINARY(16) NOT NULL,
    interest_id BINARY(16) NOT NULL,
    relevance_score DECIMAL(6,3) NOT NULL DEFAULT 1.000,
    source ENUM('manual', 'ai_inferred') NOT NULL DEFAULT 'manual',

    PRIMARY KEY (quest_id, interest_id),
    FOREIGN KEY (quest_id) REFERENCES quests(quest_id),
    FOREIGN KEY (interest_id) REFERENCES interests(interest_id),
    INDEX idx_quest_interests_interest (interest_id, relevance_score)
);
```

## 8. Optional Recommendation Debug Tables

Add these later when the backend starts serving feed results.

```sql
CREATE TABLE recommendation_requests (
    request_id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    surface ENUM('home_feed', 'map_feed', 'quest_generation') NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    weather_condition VARCHAR(50),
    local_time DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_reco_requests_user_time (user_id, created_at)
);
```

```sql
CREATE TABLE recommendation_results (
    request_id BINARY(16) NOT NULL,
    rank_position INT NOT NULL,
    target_type ENUM('place', 'quest', 'event') NOT NULL,
    target_id BINARY(16) NOT NULL,
    score DECIMAL(10,6) NOT NULL,
    reason_code VARCHAR(80),

    PRIMARY KEY (request_id, rank_position),
    FOREIGN KEY (request_id) REFERENCES recommendation_requests(request_id)
);
```

These tables help answer: "why did the system recommend this place or quest?"

## 9. MVP Recommendation Formula

Before ML, use a transparent weighted score:

```text
score =
  user_interest_match * 0.40
  + distance_score * 0.20
  + weather_fit * 0.15
  + popularity_score * 0.10
  + freshness_score * 0.10
  + diversity_bonus * 0.05
```

For quest generation:

```text
quest_score =
  user_interest_match * 0.45
  + place_cultural_relevance * 0.25
  + context_fit * 0.15
  + user_progress_fit * 0.10
  + novelty_bonus * 0.05
```

This keeps the database design useful now and leaves room for future embeddings or graph retrieval.

## 10. Practical Takeaway

Do not design only:

```text
users.interests = JSON
```

Design the recommendation database as:

```text
interests
user_interests
user_events
user_interest_events
place_interests
quest_interests
recommendation_requests
recommendation_results
```

This mirrors the big-system pattern at small scale: catalog tags, behavior signals, compact user state, retrieval candidates, and explainable ranking output.


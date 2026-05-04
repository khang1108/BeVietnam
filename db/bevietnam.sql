CREATE DATABASE IF NOT EXISTS bevietnam;
USE bevietnam;

-- Identity Group
-- UUID strategy: store as BINARY(16), insert with UUID_TO_BIN(UUID()).

CREATE TABLE IF NOT EXISTS users (
    user_id BINARY(16) NOT NULL,
    email VARCHAR(255) NULL,
    phone_number VARCHAR(20) NULL,
    status ENUM('active', 'blocked', 'deleted') NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_email (email),
    UNIQUE KEY uq_users_phone (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BINARY(16) NOT NULL,
    display_name VARCHAR(100) NULL,
    avatar_url VARCHAR(1024) NULL,
    bio VARCHAR(500) NULL,
    nationality VARCHAR(80) NULL,
    preferred_language VARCHAR(10) NULL,
    timezone VARCHAR(64) NULL,
    date_of_birth DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_auth_providers (
    auth_provider_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    provider ENUM('local', 'firebase', 'google', 'apple') NOT NULL,
    provider_user_id VARCHAR(255) NULL,
    password_hash VARCHAR(255) NULL,
    is_primary TINYINT(1) NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (auth_provider_id),
    CONSTRAINT fk_user_auth_providers_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    UNIQUE KEY uq_user_provider (user_id, provider),
    UNIQUE KEY uq_provider_subject (provider, provider_user_id),
    KEY idx_user_auth_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_devices (
    device_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    platform ENUM('ios', 'android', 'web') NOT NULL,
    device_name VARCHAR(120) NULL,
    push_token VARCHAR(512) NULL,
    app_version VARCHAR(50) NULL,
    last_seen_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (device_id),
    CONSTRAINT fk_user_devices_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    KEY idx_user_devices_user (user_id),
    KEY idx_user_devices_last_seen (last_seen_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Interest Group (for feed personalization and quest generation)
-- -----------------------------------------------------------------------------
-- Design goal:
-- 1) Keep a stable global interest catalog (`interests`) for all recommendation
--    surfaces.
-- 2) Keep current per-user preference state in a compact table
--    (`user_interest_state`) for fast online ranking.
-- 3) Keep an append-only behavior/audit log (`user_interest_events`) so we can
--    recompute state, debug recommendation outcomes, and train future models.
--
-- Why not store interests as JSON in `users`?
-- - Hard to query, index, and join with places/quests.
-- - Hard to separate long-term taste from short-term intent.
-- - Hard to audit and replay historical behavior.

-- Master taxonomy of interests shared across the system.
-- Example nodes:
--   culture/history, food/street_food, activity/photography.
-- `parent_interest_id` allows hierarchical taxonomy (Pinterest-like pattern).
CREATE TABLE IF NOT EXISTS interests (
    interest_id BINARY(16) NOT NULL,
    parent_interest_id BINARY(16) NULL,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(50) NOT NULL,
    level TINYINT NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (interest_id),
    CONSTRAINT fk_interests_parent
        FOREIGN KEY (parent_interest_id) REFERENCES interests(interest_id)
        ON DELETE SET NULL,
    UNIQUE KEY uq_interests_code (code),
    KEY idx_interests_parent (parent_interest_id),
    KEY idx_interests_category_level (category, level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Current interest profile of one user.
-- This is the "online serving table" used by feed/quest ranking.
-- Scoring convention:
-- - `long_term_score`: stable, accumulated preference (months).
-- - `short_term_score`: recent intent (days/weeks), decays faster.
-- - `confidence`: quality of the inferred preference signal.
CREATE TABLE IF NOT EXISTS user_interest_state (
    user_id BINARY(16) NOT NULL,
    interest_id BINARY(16) NOT NULL,
    long_term_score DECIMAL(7,4) NOT NULL DEFAULT 0.0000,
    short_term_score DECIMAL(7,4) NOT NULL DEFAULT 0.0000,
    preference_type ENUM('like', 'neutral', 'dislike') NOT NULL DEFAULT 'neutral',
    source ENUM('onboarding', 'manual', 'behavior', 'ai_inferred') NOT NULL DEFAULT 'behavior',
    confidence DECIMAL(5,4) NOT NULL DEFAULT 1.0000,
    last_signal_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, interest_id),
    CONSTRAINT fk_user_interest_state_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_interest_state_interest
        FOREIGN KEY (interest_id) REFERENCES interests(interest_id)
        ON DELETE CASCADE,
    KEY idx_interest_state_user_scores (user_id, short_term_score, long_term_score),
    KEY idx_interest_state_interest (interest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Event log used to update `user_interest_state`.
-- Each row is one behavioral signal converted into a score delta.
-- Example `event_type`: view_place, save_place, check_in, complete_quest, skip_quest.
-- `source_object_type/source_object_id` links the event back to the triggering
-- domain object for traceability.
CREATE TABLE IF NOT EXISTS user_interest_events (
    event_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    interest_id BINARY(16) NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    delta_score DECIMAL(7,4) NOT NULL,
    source_object_type ENUM('place', 'quest', 'event', 'post', 'manual') NOT NULL,
    source_object_id BINARY(16) NULL,
    reason VARCHAR(200) NULL,
    event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (event_id),
    CONSTRAINT fk_user_interest_events_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_interest_events_interest
        FOREIGN KEY (interest_id) REFERENCES interests(interest_id)
        ON DELETE CASCADE,
    KEY idx_interest_events_user_time (user_id, event_time),
    KEY idx_interest_events_interest_time (interest_id, event_time),
    KEY idx_interest_events_type_time (event_type, event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

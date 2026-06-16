"""
Trip Advisor Scoring — deterministic recommendation math.

All numeric scoring lives here as pure functions so it stays testable and the
LLM never invents scores. The agent (`agent.py`) calls these helpers, then asks
Gemini only to phrase the explanation text.

Score convention: every component is a float in the 0–100 range.
"""

from __future__ import annotations

from typing import Any

# ── Suitability weights (must sum to 1.0) ─────────────────────────────────────
W_WEATHER = 0.20
W_TRAFFIC = 0.20
W_DISTANCE = 0.15
W_CROWD = 0.15
W_CULTURE = 0.30

# ── Culture-score component weights (must sum to 1.0) ─────────────────────────
C_PRIORITY = 0.40       # base place priority
C_FACT_STRENGTH = 0.30  # retrieved fact strength + source quality
C_INTEREST = 0.20       # user interest match
C_COMPLETENESS = 0.10   # language / content completeness

# Neutral value used when a context factor is missing.
NEUTRAL = 50.0
# Default place priority when the backend does not supply one.
DEFAULT_PLACE_PRIORITY = 60.0
# Neutral interest match when the user declares no interests.
NEUTRAL_INTEREST = 60.0

# Source trust weights for fact strength.
_SOURCE_WEIGHT = {
    "official": 1.0,
    "unesco": 1.0,
    "book": 0.85,
}
_DEFAULT_SOURCE_WEIGHT = 0.7

# Bubble-size thresholds on the final suitability score.
_BUBBLE_LARGE = 70.0
_BUBBLE_MEDIUM = 45.0

# Reason-code thresholds.
_GOOD = 70.0
_CULTURE_MATCH = 65.0

CONTEXT_FACTORS = ("weather_score", "traffic_score", "distance_score", "crowd_score")


def clamp(value: float, low: float = 0.0, high: float = 100.0) -> float:
    """Clamp a value into [low, high]."""
    return max(low, min(high, value))


def _source_weight(source_type: str) -> float:
    return _SOURCE_WEIGHT.get((source_type or "").strip().lower(), _DEFAULT_SOURCE_WEIGHT)


def compute_fact_strength(facts: list[dict[str, Any]]) -> float:
    """
    Combine retrieval similarity with source quality into a 0–100 score.

    strength = avg( similarity * source_weight ) * 100
    Returns 0 when there are no facts.
    """
    if not facts:
        return 0.0
    total = 0.0
    for fact in facts:
        similarity = float(fact.get("score", 0.0) or 0.0)
        total += clamp(similarity, 0.0, 1.0) * _source_weight(fact.get("source_type", ""))
    return clamp((total / len(facts)) * 100.0)


def _interest_tokens(place_category: str, facts: list[dict[str, Any]]) -> set[str]:
    tokens: set[str] = set()
    if place_category:
        tokens.add(place_category.strip().lower())
    for fact in facts:
        category = str(fact.get("category", "")).strip().lower()
        if category:
            tokens.add(category)
        for tag in fact.get("tags", []) or []:
            tag = str(tag).strip().lower()
            if tag:
                tokens.add(tag)
    return tokens


def compute_interest_match(
    interests: list[str],
    place_category: str,
    facts: list[dict[str, Any]],
) -> float:
    """
    Fraction of the user's interests reflected in the place/fact tokens, 0–100.

    With no declared interests we cannot personalize, so return a neutral score.
    """
    interests = [str(i).strip().lower() for i in (interests or []) if str(i).strip()]
    if not interests:
        return NEUTRAL_INTEREST
    tokens = _interest_tokens(place_category, facts)
    if not tokens:
        return 0.0
    matched = sum(1 for interest in interests if interest in tokens)
    return clamp((matched / len(interests)) * 100.0)


def compute_completeness(facts: list[dict[str, Any]], language: str) -> float:
    """
    Fraction of facts that are in the requested language AND fully sourced, 0–100.
    """
    if not facts:
        return 0.0
    language = (language or "").strip().lower()
    complete = 0
    for fact in facts:
        lang_ok = not language or str(fact.get("language", "")).strip().lower() in {"", language}
        sourced = all(
            str(fact.get(field, "")).strip()
            for field in ("source_type", "source_title", "publisher")
        )
        if lang_ok and sourced:
            complete += 1
    return clamp((complete / len(facts)) * 100.0)


def compute_culture_score(
    place_priority: float,
    facts: list[dict[str, Any]],
    interests: list[str],
    place_category: str,
    language: str,
) -> tuple[float, dict[str, float]]:
    """
    Weighted culture score with its component breakdown (all 0–100).
    """
    components = {
        "place_priority": clamp(place_priority),
        "fact_strength": compute_fact_strength(facts),
        "interest_match": compute_interest_match(interests, place_category, facts),
        "completeness": compute_completeness(facts, language),
    }
    culture_score = (
        C_PRIORITY * components["place_priority"]
        + C_FACT_STRENGTH * components["fact_strength"]
        + C_INTEREST * components["interest_match"]
        + C_COMPLETENESS * components["completeness"]
    )
    return clamp(culture_score), components


def resolve_context_scores(context: dict[str, Any]) -> tuple[dict[str, float], list[str]]:
    """
    Read the four backend context scores, substituting NEUTRAL for any that are
    missing or explicitly flagged. Returns (scores, missing_factor_names).
    """
    declared_missing = {str(name).strip() for name in context.get("missing_factors", []) or []}
    scores: dict[str, float] = {}
    missing: list[str] = []
    for factor in CONTEXT_FACTORS:
        raw = context.get(factor)
        if raw is None or factor in declared_missing:
            scores[factor] = NEUTRAL
            missing.append(factor)
        else:
            scores[factor] = clamp(float(raw))
    return scores, missing


def compute_suitability(context_scores: dict[str, float], culture_score: float) -> float:
    """Final weighted suitability score (0–100)."""
    suitability = (
        W_WEATHER * context_scores["weather_score"]
        + W_TRAFFIC * context_scores["traffic_score"]
        + W_DISTANCE * context_scores["distance_score"]
        + W_CROWD * context_scores["crowd_score"]
        + W_CULTURE * culture_score
    )
    return clamp(suitability)


def bubble_size_from(suitability_score: float) -> str:
    """Map a suitability score to a bubble tier for the map UI."""
    if suitability_score >= _BUBBLE_LARGE:
        return "large"
    if suitability_score >= _BUBBLE_MEDIUM:
        return "medium"
    return "small"


def build_reason_codes(
    culture_score: float,
    interest_match: float,
    context_scores: dict[str, float],
    has_interests: bool,
    missing: list[str],
) -> list[str]:
    """Derive human-facing reason codes from the computed scores."""
    codes: list[str] = []
    if culture_score >= _CULTURE_MATCH:
        codes.append("culture_match")
    if has_interests and interest_match >= _GOOD:
        codes.append("interest_match")
    if context_scores["weather_score"] >= _GOOD:
        codes.append("good_weather")
    if context_scores["traffic_score"] >= _GOOD:
        codes.append("low_traffic")
    if context_scores["distance_score"] >= _GOOD:
        codes.append("nearby")
    if context_scores["crowd_score"] >= _GOOD:
        codes.append("not_crowded")
    if missing:
        codes.append("partial_context")
    if not codes:
        codes.append("context_fit")
    return codes

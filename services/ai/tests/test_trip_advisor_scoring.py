"""
Trip Advisor scoring tests.

Plain-assert tests (the AI service has no pytest dependency yet). Run with:

    PYTHONPATH=. services/backend/venv/bin/python services/ai/tests/test_trip_advisor_scoring.py
"""

from services.ai.agents.trip_advisor import scoring
from services.ai.agents.trip_advisor.agent import TripAdvisorAgent


_HUE_FACTS = [
    {
        "text": "Kinh thành Huế được xây dựng từ năm 1805 dưới triều vua Gia Long.",
        "category": "history",
        "tags": ["history", "heritage", "nguyen-dynasty"],
        "language": "vi",
        "source_type": "official",
        "source_title": "Trung tâm Bảo tồn Di tích Cố đô Huế",
        "publisher": "Trung tâm Bảo tồn Di tích Cố đô Huế",
        "source_url": "https://hueworldheritage.org.vn/",
        "page_or_section": "Kinh thành Huế",
        "score": 0.8,
    }
]


class _StubScout:
    """CultureScout stand-in so tests never hit Qdrant/HF/network."""

    def __init__(self, facts):
        self._facts = facts

    def retrieve(self, **_kwargs):
        return list(self._facts)


def _agent(facts):
    return TripAdvisorAgent(scout=_StubScout(facts))


def test_suitability_weights_sum_to_one():
    total = (
        scoring.W_WEATHER + scoring.W_TRAFFIC + scoring.W_DISTANCE
        + scoring.W_CROWD + scoring.W_CULTURE
    )
    assert abs(total - 1.0) < 1e-9, total
    c_total = (
        scoring.C_PRIORITY + scoring.C_FACT_STRENGTH
        + scoring.C_INTEREST + scoring.C_COMPLETENESS
    )
    assert abs(c_total - 1.0) < 1e-9, c_total


def test_bubble_thresholds():
    assert scoring.bubble_size_from(95) == "large"
    assert scoring.bubble_size_from(70) == "large"
    assert scoring.bubble_size_from(69.9) == "medium"
    assert scoring.bubble_size_from(45) == "medium"
    assert scoring.bubble_size_from(44.9) == "small"
    assert scoring.bubble_size_from(0) == "small"


def test_missing_factor_uses_neutral_and_flags():
    scores, missing = scoring.resolve_context_scores(
        {"weather_score": 80, "traffic_score": None, "missing_factors": ["distance_score"]}
    )
    assert scores["weather_score"] == 80
    assert scores["traffic_score"] == scoring.NEUTRAL  # None -> neutral
    assert scores["distance_score"] == scoring.NEUTRAL  # declared missing -> neutral
    assert scores["crowd_score"] == scoring.NEUTRAL     # absent -> neutral
    assert set(missing) == {"traffic_score", "distance_score", "crowd_score"}


def test_culture_score_monotonic_in_priority():
    low, _ = scoring.compute_culture_score(20, _HUE_FACTS, ["history"], "history", "vi")
    high, _ = scoring.compute_culture_score(90, _HUE_FACTS, ["history"], "history", "vi")
    assert high > low


def test_interest_match_full_and_none():
    full = scoring.compute_interest_match(["history"], "history", _HUE_FACTS)
    assert full == 100.0
    miss = scoring.compute_interest_match(["food"], "history", _HUE_FACTS)
    assert miss == 0.0
    neutral = scoring.compute_interest_match([], "history", _HUE_FACTS)
    assert neutral == scoring.NEUTRAL_INTEREST


def test_high_context_yields_large_bubble():
    result = _agent(_HUE_FACTS).explain(
        {
            "user_id": "demo",
            "language": "vi",
            "place": {"place_id": "hue-imperial-city", "name": "Kinh thành Huế",
                      "category": "history", "priority": 90},
            "interests": ["history", "architecture"],
            "context": {"weather_score": 85, "traffic_score": 80,
                        "distance_score": 90, "crowd_score": 75},
        }
    )
    assert result["bubble_size"] == "large"
    assert result["suitability_score"] >= 70
    assert result["source_refs"], "grounded result must carry source refs"
    assert result["fallback"] is False
    assert "culture_match" in result["reason_codes"]
    assert result["cultural_highlight"]
    # Deterministic: numbers must not come from an LLM.
    assert isinstance(result["culture_score"], int)


def test_low_context_yields_small_bubble():
    result = _agent(_HUE_FACTS).explain(
        {
            "user_id": "demo",
            "place": {"place_id": "hue-imperial-city", "name": "Kinh thành Huế",
                      "category": "history", "priority": 30},
            "interests": ["food"],
            "context": {"weather_score": 10, "traffic_score": 15,
                        "distance_score": 20, "crowd_score": 10},
        }
    )
    assert result["bubble_size"] == "small"
    assert result["suitability_score"] < 45


def test_no_facts_marks_fallback():
    result = _agent([]).explain(
        {
            "user_id": "demo",
            "place": {"place_id": "unknown", "name": "Unknown", "category": "history"},
            "interests": ["history"],
            "context": {"weather_score": 60, "traffic_score": 60,
                        "distance_score": 60, "crowd_score": 60},
        }
    )
    assert result["fallback"] is True
    assert result["source_refs"] == []
    assert result["explanation"], "fallback still returns a template explanation"
    assert result["ai_generated"] is False


def test_partial_context_reason_code():
    result = _agent(_HUE_FACTS).explain(
        {
            "user_id": "demo",
            "place": {"place_id": "hue-imperial-city", "name": "Kinh thành Huế",
                      "category": "history"},
            "interests": ["history"],
            "context": {"weather_score": 80, "missing_factors": ["traffic_score"]},
        }
    )
    assert "partial_context" in result["reason_codes"]
    assert "traffic_score" in result["missing_factors"]


def _run_all():
    tests = [v for k, v in sorted(globals().items()) if k.startswith("test_")]
    failures = 0
    for test in tests:
        try:
            test()
            print(f"PASS {test.__name__}")
        except AssertionError as exc:
            failures += 1
            print(f"FAIL {test.__name__}: {exc}")
        except Exception as exc:  # noqa: BLE001
            failures += 1
            print(f"ERROR {test.__name__}: {type(exc).__name__}: {exc}")
    print(f"\n{len(tests) - failures}/{len(tests)} passed")
    return failures


if __name__ == "__main__":
    import sys

    sys.exit(1 if _run_all() else 0)

"""Source-backed fallback spotlight when the LLM is unavailable."""

from typing import Any


def get_fallback_spotlight(
    place_name: str,
    facts: list[dict[str, Any]],
    language: str = "vi",
) -> dict[str, Any]:
    """
    Build a minimal, grounded spotlight from the first retrieved fact.

    No condition tags (always eligible) — the runtime suitability score still
    ranks it. Returns {} when there is no fact to ground a post.
    """
    fact = next((f for f in facts if str(f.get("text", "")).strip()), None)
    if not fact:
        return {}

    hook = str(fact["text"]).strip()
    if language == "vi":
        title = f"Khám phá {place_name}"
        body = f"{place_name} là điểm đến văn hóa đáng ghé thăm. {hook}"
    else:
        title = f"Discover {place_name}"
        body = f"{place_name} is a cultural place worth visiting. {hook}"

    return {
        "title": title[:160],
        "body": body[:900],
        "cultural_hook": hook[:400],
        "weather_tags": [],
        "time_tags": [],
        "season_tags": [],
        "categories": [],
    }

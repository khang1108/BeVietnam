"""
Trip Advisor — Recommendation Scoring & Grounded Explanation Agent.

Trip Advisor turns normalized backend context (weather/traffic/distance/crowd
scores) plus grounded cultural facts into a single recommendation:

    suitability_score, culture_score, bubble_size,
    explanation, cultural_highlight, reason_codes, source_refs.

Key principle (same as Culture Scout / Quest Maker): the numbers are computed in
Python (`scoring.py`). Gemini only phrases the explanation sentence, and if it
fails we fall back to a deterministic template with the *same* scores.
"""

import logging
from typing import Any

from services.ai.agents.culture_scout import CultureScout
from services.ai.agents.trip_advisor import scoring
from services.ai.common.config import settings
from services.ai.common.llm import llm_gateway

logger = logging.getLogger(__name__)

_MAX_FACTS = 3
_HIGHLIGHT_MAX = 240

_EXPLANATION_SYSTEM_PROMPT = (
    "You write one short, warm recommendation sentence for a Vietnamese cultural "
    "tourism app. Use ONLY the cultural highlight and reason codes provided — do "
    "not invent facts, places, or numbers. Do not mention scores. Write in the "
    "requested language. Return strict JSON: {\"explanation\": \"...\"}."
)


class TripAdvisorAgent:
    """Assemble a grounded, score-backed recommendation for one place."""

    def __init__(self, scout: CultureScout | None = None) -> None:
        self._scout = scout or CultureScout()

    def explain(self, context: dict[str, Any]) -> dict[str, Any]:
        """
        Build the recommendation result for a single place.

        Args:
            context: ExplainRecommendationRequest payload
                (user_id, place, interests, context, language).

        Returns:
            A flat result dict ready for the Publisher envelope.
        """
        place = context.get("place", {}) or {}
        interests = context.get("interests", []) or []
        backend_context = context.get("context", {}) or {}
        language = (context.get("language") or "vi").strip().lower()

        place_id = str(place.get("place_id", "")).strip()
        place_name = str(place.get("name", "")).strip()
        place_category = str(place.get("category", "")).strip()
        place_priority = self._resolve_priority(place)

        facts = self._retrieve_facts(place_id, place_name, place_category, language, interests)

        culture_score, culture_components = scoring.compute_culture_score(
            place_priority=place_priority,
            facts=facts,
            interests=interests,
            place_category=place_category,
            language=language,
        )
        context_scores, missing = scoring.resolve_context_scores(backend_context)
        suitability_score = scoring.compute_suitability(context_scores, culture_score)
        bubble_size = scoring.bubble_size_from(suitability_score)
        reason_codes = scoring.build_reason_codes(
            culture_score=culture_score,
            interest_match=culture_components["interest_match"],
            context_scores=context_scores,
            has_interests=bool(interests),
            missing=missing,
        )

        cultural_highlight = self._pick_highlight(facts)
        source_refs = self._build_source_refs(facts)
        is_fallback = not source_refs

        explanation, ai_generated = self._build_explanation(
            place_name=place_name,
            language=language,
            cultural_highlight=cultural_highlight,
            reason_codes=reason_codes,
            is_fallback=is_fallback,
        )

        return {
            "place_id": place_id,
            "suitability_score": round(suitability_score),
            "culture_score": round(culture_score),
            "culture_components": {k: round(v) for k, v in culture_components.items()},
            "bubble_size": bubble_size,
            "explanation": explanation,
            "cultural_highlight": cultural_highlight,
            "reason_codes": reason_codes,
            "source_refs": source_refs,
            "missing_factors": missing,
            "fallback": is_fallback,
            "ai_generated": ai_generated,
            "confidence": self._confidence(culture_components["fact_strength"], is_fallback),
        }

    # ── Retrieval ─────────────────────────────────────────────────────────────

    def _retrieve_facts(
        self,
        place_id: str,
        place_name: str,
        place_category: str,
        language: str,
        interests: list[str],
    ) -> list[dict[str, Any]]:
        try:
            return self._scout.retrieve(
                query=place_name,
                place_name=place_name,
                place_id=place_id,
                category=place_category,
                language=language,
                interests=interests,
                limit=_MAX_FACTS,
            )
        except Exception as exc:  # retrieval must never break the recommendation
            logger.warning("Culture Scout retrieval failed in Trip Advisor: %s", exc)
            return []

    # ── Helpers ───────────────────────────────────────────────────────────────

    @staticmethod
    def _resolve_priority(place: dict[str, Any]) -> float:
        raw = place.get("priority")
        if raw is None:
            return scoring.DEFAULT_PLACE_PRIORITY
        try:
            return scoring.clamp(float(raw))
        except (TypeError, ValueError):
            return scoring.DEFAULT_PLACE_PRIORITY

    @staticmethod
    def _pick_highlight(facts: list[dict[str, Any]]) -> str:
        for fact in facts:
            text = str(fact.get("text", "")).strip()
            if text:
                return text if len(text) <= _HIGHLIGHT_MAX else text[:_HIGHLIGHT_MAX].rstrip() + "…"
        return ""

    @staticmethod
    def _build_source_refs(facts: list[dict[str, Any]]) -> list[dict[str, Any]]:
        refs: list[dict[str, Any]] = []
        seen: set[tuple[str, str]] = set()
        for fact in facts:
            source_type = str(fact.get("source_type", "")).strip()
            title = str(fact.get("source_title", "")).strip()
            publisher = str(fact.get("publisher", "")).strip()
            if not (source_type and title and publisher):
                continue
            key = (title, str(fact.get("page_or_section", "")).strip())
            if key in seen:
                continue
            seen.add(key)
            refs.append(
                {
                    "source_type": source_type,
                    "title": title,
                    "url": str(fact.get("source_url", "")).strip(),
                    "publisher": publisher,
                    "page_or_section": str(fact.get("page_or_section", "")).strip(),
                }
            )
        return refs

    @staticmethod
    def _confidence(fact_strength: float, is_fallback: bool) -> float:
        if is_fallback:
            return 0.4
        # Map fact strength (0–100) into a 0.5–0.95 confidence band.
        return round(0.5 + 0.45 * (fact_strength / 100.0), 2)

    # ── Explanation (Gemini optional, deterministic fallback) ─────────────────

    def _build_explanation(
        self,
        place_name: str,
        language: str,
        cultural_highlight: str,
        reason_codes: list[str],
        is_fallback: bool,
    ) -> tuple[str, bool]:
        """Return (explanation_text, ai_generated)."""
        if settings.llm_provider == "gemini" and cultural_highlight and not is_fallback:
            text = self._gemini_explanation(place_name, language, cultural_highlight, reason_codes)
            if text:
                return text, True
        return self._template_explanation(place_name, language, reason_codes), False

    def _gemini_explanation(
        self,
        place_name: str,
        language: str,
        cultural_highlight: str,
        reason_codes: list[str],
    ) -> str:
        user_prompt = (
            f"Language: {language}\n"
            f"Place: {place_name}\n"
            f"Cultural highlight: {cultural_highlight}\n"
            f"Reason codes: {', '.join(reason_codes)}\n"
            "Write one recommendation sentence."
        )
        try:
            result = llm_gateway.generate_json(_EXPLANATION_SYSTEM_PROMPT, user_prompt)
            explanation = str(result.get("explanation", "")).strip()
            return explanation
        except Exception as exc:
            logger.warning("Gemini explanation failed, using template: %s", exc)
            return ""

    @staticmethod
    def _template_explanation(
        place_name: str,
        language: str,
        reason_codes: list[str],
    ) -> str:
        name = place_name or ("địa điểm này" if language == "vi" else "this place")
        codes = set(reason_codes)
        if language == "en":
            parts = [f"{name} is a good choice right now"]
            extras = []
            if "good_weather" in codes:
                extras.append("the weather is pleasant")
            if "low_traffic" in codes:
                extras.append("traffic is light")
            if "nearby" in codes:
                extras.append("it is close by")
            if "not_crowded" in codes:
                extras.append("it is not crowded")
            if "interest_match" in codes:
                extras.append("it matches your interests")
            if extras:
                parts.append("because " + ", ".join(extras))
            return " ".join(parts) + "."

        parts = [f"{name} là lựa chọn phù hợp lúc này"]
        extras = []
        if "good_weather" in codes:
            extras.append("thời tiết dễ chịu")
        if "low_traffic" in codes:
            extras.append("đường ít kẹt xe")
        if "nearby" in codes:
            extras.append("ở gần bạn")
        if "not_crowded" in codes:
            extras.append("không quá đông")
        if "interest_match" in codes:
            extras.append("hợp với sở thích của bạn")
        if extras:
            parts.append("vì " + ", ".join(extras))
        return " ".join(parts) + "."

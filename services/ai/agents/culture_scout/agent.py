"""
Culture Scout — Cultural Knowledge Retrieval Agent.

Culture Scout retrieves trustworthy cultural facts from Qdrant
before any generation happens. This ensures Quest Maker's tasks
are grounded in real knowledge, not hallucinated claims.

Key principle: Culture Scout should NEVER invent facts.
If retrieval returns weak results, it reports low confidence
so downstream agents can use fallback behavior.

Usage:
    from services.ai.agents.culture_scout import CultureScout

    scout = CultureScout()
    facts = scout.retrieve(
        query="Kinh thành Huế lịch sử kiến trúc",
        place_name="Kinh thành Huế",
        interests=["history", "architecture"],
    )
"""

import logging
from typing import Any

from services.ai.agents.culture_scout.fallback import get_fallback_facts
from services.ai.common.qdrant_store import qdrant_store

logger = logging.getLogger(__name__)

# Minimum similarity score to consider a result trustworthy
_MIN_CONFIDENCE = 0.3
_DEFAULT_LIMIT = 5
_SUPPORTED_LANGUAGES = {"vi", "en"}
_SUPPORTED_FACT_CATEGORIES = {
    "architecture",
    "art",
    "festival",
    "food",
    "history",
    "nature",
    "religion",
    "tradition",
}
_SOURCE_FIELDS = (
    "source_type",
    "source_title",
    "publisher",
)


class CultureScout:
    """
    Cultural Retrieval Agent.

    Searches Qdrant for cultural facts related to a place,
    query, or user interests. Falls back to hardcoded facts
    if Qdrant is unavailable or results are too weak.
    """

    def retrieve(
        self,
        query: str = "",
        place_name: str = "",
        place_id: str = "",
        category: str = "",
        language: str = "vi",
        interests: list[str] | None = None,
        limit: int = _DEFAULT_LIMIT,
    ) -> list[dict[str, Any]]:
        """
        Retrieve cultural facts relevant to the query and context.

        Args:
            query: Free-text search query.
            place_name: Optional place name to focus the search.
            place_id: Optional stable POI ID to filter results.
            category: Optional cultural category filter.
            language: Preferred fact language, currently 'vi' or 'en'.
            interests: Optional user interests to enrich the query.
            limit: Maximum number of facts to return.

        Returns:
            List of cultural fact dicts with source metadata. Each returned
            fact has at least text, place/category fields, source refs, and
            score.
        """
        normalized_limit = max(1, min(limit, 20))
        normalized_language = self._normalize_language(language)
        query_category = category.strip().lower()
        filter_category = self._normalize_category_filter(category)
        normalized_place_id = place_id.strip().lower()

        search_query = self._build_query(
            query=query,
            place_name=place_name,
            interests=interests,
            category=query_category,
        )

        if not search_query.strip():
            logger.info("No query context provided, using fallback facts.")
            return self._fallback(
                place_name=place_name,
                place_id=normalized_place_id,
                category=filter_category,
                language=normalized_language,
                limit=normalized_limit,
            )

        logger.info("Culture Scout searching: '%s'", search_query[:80])
        results = qdrant_store.search(
            query=search_query,
            limit=normalized_limit,
            place_filter=place_name if place_name else None,
            place_id_filter=normalized_place_id or None,
            category_filter=filter_category or None,
            language_filter=normalized_language,
        )

        if not results:
            logger.info("Qdrant returned no results, using fallback facts.")
            return self._fallback(
                place_name=place_name,
                place_id=normalized_place_id,
                category=filter_category,
                language=normalized_language,
                limit=normalized_limit,
            )

        strong_results = self._filter_results(
            results=results,
            category=filter_category,
            language=normalized_language,
        )

        if not strong_results:
            logger.info(
                "All %d results were weak or missing source metadata; using fallback.",
                len(results),
            )
            return self._fallback(
                place_name=place_name,
                place_id=normalized_place_id,
                category=filter_category,
                language=normalized_language,
                limit=normalized_limit,
            )

        logger.info(
            "Culture Scout found %d strong results (best score: %.3f)",
            len(strong_results),
            strong_results[0].get("score", 0),
        )
        return strong_results[:normalized_limit]

    def _build_query(
        self,
        query: str,
        place_name: str,
        interests: list[str] | None,
        category: str = "",
    ) -> str:
        """
        Combine query, place name, and interests into a single search string.

        Example:
            query="lịch sử", place_name="Huế", interests=["history", "food"]
            → "lịch sử Huế history food"
        """
        parts = []
        for value in (query, place_name, category):
            if value and value.strip():
                parts.append(value.strip())
        if interests:
            parts.extend(
                str(interest).strip()
                for interest in interests
                if str(interest).strip()
            )
        return " ".join(parts)

    def _filter_results(
        self,
        results: list[dict[str, Any]],
        category: str,
        language: str,
    ) -> list[dict[str, Any]]:
        """Keep only trustworthy, source-backed retrieval results."""
        strong_results: list[dict[str, Any]] = []

        for result in results:
            if result.get("score", 0) < _MIN_CONFIDENCE:
                continue
            if category and result.get("category", "").lower() != category:
                continue
            if language and result.get("language") not in {"", language}:
                continue
            if not self._has_source_metadata(result):
                continue
            strong_results.append(result)

        return strong_results

    def _has_source_metadata(self, fact: dict[str, Any]) -> bool:
        """Return True when a fact has enough source metadata to show users."""
        return all(str(fact.get(field, "")).strip() for field in _SOURCE_FIELDS)

    def _normalize_language(self, language: str) -> str:
        """Normalize language and default unsupported values to Vietnamese."""
        normalized = (language or "vi").strip().lower()
        if normalized not in _SUPPORTED_LANGUAGES:
            return "vi"
        return normalized

    def _normalize_category_filter(self, category: str) -> str:
        """Use category as a hard filter only for known fact categories."""
        normalized = category.strip().lower()
        if normalized in _SUPPORTED_FACT_CATEGORIES:
            return normalized
        return ""

    def _fallback(
        self,
        place_name: str,
        place_id: str,
        category: str,
        language: str,
        limit: int,
    ) -> list[dict[str, Any]]:
        """Return local source-backed fallback facts."""
        facts = get_fallback_facts(
            place_name=place_name,
            place_id=place_id,
            category=category,
            language=language,
            limit=limit,
        )
        if facts:
            return facts

        facts = get_fallback_facts(
            place_name=place_name,
            category=category,
            language=language,
            limit=limit,
        )
        if facts:
            return facts

        return get_fallback_facts(
            place_name=place_name,
            language=language,
            limit=limit,
        )

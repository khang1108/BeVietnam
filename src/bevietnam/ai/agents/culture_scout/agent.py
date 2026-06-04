"""
Culture Scout — Cultural Knowledge Retrieval Agent.

Culture Scout retrieves trustworthy cultural facts from Qdrant
before any generation happens. This ensures Quest Maker's tasks
are grounded in real knowledge, not hallucinated claims.

Key principle: Culture Scout should NEVER invent facts.
If retrieval returns weak results, it reports low confidence
so downstream agents can use fallback behavior.

Usage:
    from ai.culture_scout import CultureScout

    scout = CultureScout()
    facts = scout.retrieve(
        query="Kinh thành Huế lịch sử kiến trúc",
        place_name="Kinh thành Huế",
        interests=["history", "architecture"],
    )
"""

import logging
from typing import Any

from src.bevietnam.ai.common.qdrant_store import qdrant_store
from ai.culture_scout.fallback import get_fallback_facts

logger = logging.getLogger(__name__)

# Minimum similarity score to consider a result trustworthy
_MIN_CONFIDENCE = 0.3


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
        interests: list[str] | None = None,
        limit: int = 5,
    ) -> list[dict[str, Any]]:
        """
        Retrieve cultural facts relevant to the query and context.

        Args:
            query:      Free-text search query.
            place_name: Optional place name to focus the search.
            interests:  Optional user interests to enrich the query.
            limit:      Maximum number of facts to return.

        Returns:
            List of cultural fact dicts with keys:
              text, place_name, category, source, score
        """
        # ── Build search query from all available context ─────────────────
        search_query = self._build_query(query, place_name, interests)

        if not search_query.strip():
            logger.info("No query context provided, using fallback facts.")
            return get_fallback_facts(place_name, limit)

        # ── Search Qdrant ─────────────────────────────────────────────────
        logger.info("Culture Scout searching: '%s'", search_query[:80])
        results = qdrant_store.search(
            query=search_query,
            limit=limit,
            place_filter=place_name if place_name else None,
        )

        # ── Check result quality ──────────────────────────────────────────
        if not results:
            logger.info("Qdrant returned no results, using fallback facts.")
            return get_fallback_facts(place_name, limit)

        # Filter out low-confidence results
        strong_results = [r for r in results if r.get("score", 0) >= _MIN_CONFIDENCE]

        if not strong_results:
            logger.info(
                "All %d results below confidence threshold (%.2f), using fallback.",
                len(results),
                _MIN_CONFIDENCE,
            )
            return get_fallback_facts(place_name, limit)

        logger.info(
            "Culture Scout found %d strong results (best score: %.3f)",
            len(strong_results),
            strong_results[0].get("score", 0),
        )
        return strong_results

    def _build_query(
        self,
        query: str,
        place_name: str,
        interests: list[str] | None,
    ) -> str:
        """
        Combine query, place name, and interests into a single search string.

        Example:
            query="lịch sử", place_name="Huế", interests=["history", "food"]
            → "lịch sử Huế history food"
        """
        parts = []
        if query:
            parts.append(query)
        if place_name:
            parts.append(place_name)
        if interests:
            parts.extend(interests)
        return " ".join(parts)

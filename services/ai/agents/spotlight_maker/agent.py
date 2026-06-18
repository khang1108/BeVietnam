"""
Spotlight Maker — feed post generator.

Composes an authored SpotlightPost for a place, grounded ONLY in cultural facts
retrieved by Culture Scout, and assigns condition tags (weather/time/season)
from the controlled vocab. The LLM (self-hosted vLLM) phrases the post; on any
failure a source-backed fallback post is used so the feed never goes empty.

The agent is stateless — facts and context come from the caller. A batch
authoring script iterates POIs, validates each post (SpotlightPost schema +
SafetyKeeper), and writes the dataset.

Usage:
    from services.ai.agents.spotlight_maker import SpotlightMaker

    maker = SpotlightMaker()
    draft = maker.generate(place_name="Chùa Thiên Mụ", facts=facts, language="vi")
"""

import logging
from typing import Any

from services.ai.agents.spotlight_maker.fallback import get_fallback_spotlight
from services.ai.agents.spotlight_maker.prompts import SYSTEM_PROMPT, build_user_prompt
from services.ai.common.llm import vllm_gateway
from services.ai.common.post_schema import SEASON_TAGS, TIME_TAGS, WEATHER_TAGS

logger = logging.getLogger(__name__)

_TAG_VOCAB = {
    "weather_tags": WEATHER_TAGS,
    "time_tags": TIME_TAGS,
    "season_tags": SEASON_TAGS,
}


class SpotlightMaker:
    """Generates a grounded, condition-tagged feed spotlight for one place."""

    used_fallback: bool = False

    def generate(
        self,
        place_name: str,
        facts: list[dict[str, Any]],
        language: str = "vi",
    ) -> dict[str, Any]:
        """
        Return a spotlight draft dict (title, body, cultural_hook, *_tags,
        categories). Falls back to a source-backed post on LLM failure.
        """
        self.used_fallback = False

        if not any(str(f.get("text", "")).strip() for f in facts):
            logger.warning("SpotlightMaker: no grounded facts for '%s'", place_name)
            return {}

        result = vllm_gateway.generate_json(
            SYSTEM_PROMPT, build_user_prompt(place_name, facts, language)
        )

        if not result or not str(result.get("body", "")).strip():
            logger.info("SpotlightMaker: LLM empty for '%s' — using fallback", place_name)
            self.used_fallback = True
            return get_fallback_spotlight(place_name, facts, language)

        return self._sanitize(result)

    def _sanitize(self, draft: dict[str, Any]) -> dict[str, Any]:
        """Drop out-of-vocab condition tags so the post passes schema validation."""
        for field, allowed in _TAG_VOCAB.items():
            raw = draft.get(field) or []
            draft[field] = [
                t for t in (str(x).strip().lower() for x in raw) if t in allowed
            ]
        draft["categories"] = [
            str(c).strip().lower() for c in (draft.get("categories") or []) if str(c).strip()
        ]
        return draft

"""
Quest Maker Agent — Cultural Task Generator.

Generates geocaching-style cultural exploration tasks using Gemini.
This class is called by the LangGraph workflow nodes, but can also
be used standalone for testing.

The agent itself is stateless — all context comes from the workflow state.
This makes it easy to test with different inputs.

Usage:
    from ai.quest_maker.agent import QuestMaker

    maker = QuestMaker()
    task = maker.generate(context={
        "cultural_context": [...],
        "interests": ["history"],
    })
"""

import logging
import uuid
from typing import Any

from ai.common.llm import llm_gateway
from ai.quest_maker.prompts import SYSTEM_PROMPT, build_user_prompt

logger = logging.getLogger(__name__)


class QuestMaker:
    """
    Cultural Task Generator Agent.

    Uses Gemini to generate structured task JSON from:
      - User context (location, interests)
      - Cultural facts from Culture Scout
      - Current quest chain state
    """

    def generate(self, context: dict[str, Any]) -> dict:
        """
        Generate a cultural exploration task.

        Args:
            context: Dict with keys from QuestMakerState:
                     user_id, latitude, longitude, interests,
                     quest_state, cultural_context, nearby_places, language

        Returns:
            Task dict with quest chain fields, or empty dict on failure.
        """
        # Build the prompt from context
        user_prompt = build_user_prompt(
            user_id=context.get("user_id", "anonymous"),
            latitude=context.get("latitude"),
            longitude=context.get("longitude"),
            interests=context.get("interests", []),
            quest_state=context.get("quest_state", {}),
            cultural_context=context.get("cultural_context", []),
            nearby_places=context.get("nearby_places", []),
            language=context.get("language", "vi"),
        )

        # Call Gemini for structured JSON generation
        task = llm_gateway.generate_json(
            system_prompt=SYSTEM_PROMPT,
            user_prompt=user_prompt,
        )

        # Ensure task has a unique ID
        if task and "task_id" not in task:
            task["task_id"] = f"gen-{uuid.uuid4().hex[:8]}"

        if task:
            logger.info("QuestMaker generated: '%s'", task.get("title", "?"))
        else:
            logger.warning("QuestMaker: Gemini returned empty result")

        return task

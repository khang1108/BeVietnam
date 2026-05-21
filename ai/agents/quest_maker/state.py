"""
Quest Maker State — TypedDict for the LangGraph workflow.

This state is passed between LangGraph nodes during task generation.
Each node reads from and writes to this state object.

Kept in a separate file so state schema is clear and
easy to find without reading workflow logic.
"""

from __future__ import annotations

from typing import TypedDict


class QuestMakerState(TypedDict, total=False):
    """
    State object for the Quest Maker LangGraph workflow.

    Fields are populated progressively as the workflow executes:
      1. prepare_context fills user_id, location, interests, quest_state
      2. retrieve_culture fills cultural_context
      3. generate_task fills generated_task
      4. validate_task fills is_valid and errors
      5. publish fills final_response
    """

    # ── Input (from API request) ──────────────────────────────────────────────
    user_id: str
    latitude: float | None
    longitude: float | None
    interests: list[str]
    quest_state: dict            # current quest chain progress
    nearby_places: list[dict]    # places near the user
    language: str                # preferred output language ("vi" or "en")

    # ── Intermediate (filled by workflow nodes) ───────────────────────────────
    cultural_context: list[dict] # facts from Culture Scout
    generated_task: dict         # raw task from Gemini

    # ── Output (filled by validation + publishing) ────────────────────────────
    is_valid: bool
    errors: list[str]
    final_response: dict         # wrapped response from Publisher

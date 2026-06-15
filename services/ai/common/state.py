"""
Shared State Schema for AI Agent Workflows.

All LangGraph workflows in the AI Core use TypedDict-based state objects.
This module defines the base state shared across agents.
Each agent module may extend this with its own fields in a local state.py.

Usage:
    from src.bevietnam.ai.common.state import AgentState
"""

from __future__ import annotations

from typing import TypedDict


class AgentState(TypedDict, total=False):
    """
    Base state shared across all AI agent workflows.

    Fields:
        user_id:          Unique identifier for the requesting user.
        latitude:         User's current latitude (optional).
        longitude:        User's current longitude (optional).
        interests:        User's interest tags (e.g. ["history", "food"]).
        nearby_places:    Places near the user, provided by the backend.
        cultural_context: Cultural facts retrieved by Culture Scout.
        generated_task:   The task dict produced by Quest Maker.
        is_valid:         Whether Safety Keeper approved the output.
        errors:           Validation error messages, if any.
    """

    user_id: str
    latitude: float | None
    longitude: float | None
    interests: list[str]
    nearby_places: list[dict]
    cultural_context: list[dict]
    generated_task: dict
    is_valid: bool
    errors: list[str]

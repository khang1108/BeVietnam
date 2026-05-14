"""
State Schema for Agents
"""

from __future__ import annotations

from typing import Any, Literal, TypeDict, List

class AgentState(TypeDict, total=False):
    """
    Base class for all agent states
    """
    user_id: str
    latitude: float | None
    longitude: float | None
    interests: List[str]
    nearby_places: List[str]
    cultural_context: List[str]
    generated_task: dict
    is_valid: bool
    errors: List[str]

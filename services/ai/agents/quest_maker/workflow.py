"""
Quest Maker Workflow — LangGraph StateGraph.

Orchestrates the full task generation pipeline:

    prepare_context
         ↓
    retrieve_culture (Culture Scout → Qdrant)
         ↓
    generate_task (Quest Maker → Gemini)
         ↓
    validate_task (Safety Keeper)
         ↓
    ┌─────────┐
    │ valid?  │──yes──→ publish_response
    │         │──no───→ fallback_task → publish_response
    └─────────┘

Usage:
    from src.bevietnam.ai.agents.quest_maker.workflow import generate_task_workflow

    result = generate_task_workflow({
        "user_id": "user-001",
        "interests": ["history", "food"],
    })
"""

import logging

from langgraph.graph import END, StateGraph

from src.bevietnam.ai.agents.publisher import PublisherAgent
from src.bevietnam.ai.agents.quest_maker.nodes import (
    fallback_task,
    generate_task,
    prepare_context,
    retrieve_culture,
    validate_task,
)
from src.bevietnam.ai.agents.quest_maker.state import QuestMakerState

logger = logging.getLogger(__name__)


def _publish(state: QuestMakerState) -> dict:
    """Wrap the final task in a standard response envelope."""
    task = state.get("generated_task", {})
    publisher = PublisherAgent()

    # Add metadata about whether this was AI-generated or fallback
    is_fallback = "fallback" in task.get("reason_codes", [])
    return {
        "final_response": publisher.publish_response(
            payload=task,
            status="ok",
            metadata={
                "ai_generated": not is_fallback,
                "fallback": is_fallback,
            },
        ),
    }


def _route_validation(state: QuestMakerState) -> str:
    """
    Route based on Safety Keeper validation result.

    Returns the name of the next node to execute.
    """
    if state.get("is_valid", False):
        return "publish"
    return "fallback"


def _build_graph() -> StateGraph:
    """
    Build the Quest Maker LangGraph StateGraph.

    This graph is built once and reused for all requests.
    """
    graph = StateGraph(QuestMakerState)

    # ── Add nodes ─────────────────────────────────────────────────────────────
    graph.add_node("prepare_context", prepare_context)
    graph.add_node("retrieve_culture", retrieve_culture)
    graph.add_node("generate_task", generate_task)
    graph.add_node("validate_task", validate_task)
    graph.add_node("fallback", fallback_task)
    graph.add_node("publish", _publish)

    # ── Define edges ──────────────────────────────────────────────────────────
    graph.set_entry_point("prepare_context")
    graph.add_edge("prepare_context", "retrieve_culture")
    graph.add_edge("retrieve_culture", "generate_task")
    graph.add_edge("generate_task", "validate_task")

    # Conditional: valid → publish, invalid → fallback → publish
    graph.add_conditional_edges(
        "validate_task",
        _route_validation,
        {"publish": "publish", "fallback": "fallback"},
    )
    graph.add_edge("fallback", "publish")
    graph.add_edge("publish", END)

    return graph


# ── Compile graph once at module level ────────────────────────────────────────
_compiled_graph = _build_graph().compile()


def generate_task_workflow(context: dict) -> dict:
    """
    Run the full Quest Maker pipeline.

    Args:
        context: Dict with user_id, interests, quest_state, etc.

    Returns:
        Publisher-wrapped response dict:
        {
            "status": "ok",
            "data": { ...task fields... },
            "metadata": { "ai_generated": true/false, "fallback": true/false }
        }
    """
    logger.info("═══ Quest Maker workflow started ═══")

    try:
        # Run the LangGraph workflow
        result = _compiled_graph.invoke(context)
        response = result.get("final_response", {})

        logger.info(
            "═══ Quest Maker workflow completed (status: %s) ═══",
            response.get("status", "unknown"),
        )
        return response

    except Exception as exc:
        logger.error("Quest Maker workflow crashed: %s", exc, exc_info=True)

        # Ultimate fallback — return a safe task even if the graph crashes
        from src.bevietnam.ai.agents.quest_maker.fallback import get_fallback_task

        publisher = PublisherAgent()
        return publisher.publish_response(
            payload=get_fallback_task(step_index=1),
            status="ok",
            metadata={"ai_generated": False, "fallback": True, "error": str(exc)},
        )

"""
AI Core API Routes.

Defines the FastAPI endpoints that the Backend calls.
Each endpoint validates the request, delegates to the appropriate
agent workflow, and returns a structured response.

All endpoints follow the same pattern:
  1. Validate request with Pydantic schema
  2. Call agent workflow
  3. Return structured JSON response
"""

from fastapi import APIRouter

from ai.capture_judge import verify_capture_workflow
from src.bevietnam.ai.common.schemas import (
    ExplainRecommendationRequest,
    GenerateTaskRequest,
    GenerateVlogRequest,
    VerifyCaptureRequest,
)
from ai.quest_maker import generate_task_workflow
from ai.quest_maker.fallback import get_fallback_chain
from ai.story_weaver import generate_vlog_workflow
from ai.trip_advisor import explain_recommendation_workflow

router = APIRouter()


@router.post("/generate-task")
def generate_task(request: GenerateTaskRequest) -> dict:
    """
    Generate the next cultural exploration task.

    This is the main endpoint for the Quest Maker pipeline:
    Culture Scout (Qdrant) → Quest Maker (Gemini) → Safety Keeper → Publisher.

    Falls back to a curated task if any step fails.
    """
    return generate_task_workflow(request.model_dump())


@router.get("/quest-chain")
def get_quest_chain(quest_id: str = "quest-hue-imperial") -> dict:
    """
    Get the full fallback quest chain.

    Used by the website to render the Duolingo-style path.
    In production, quest state would come from the backend database.
    For the demo, we return the pre-built Huế chain.
    """
    chain = get_fallback_chain()
    return {
        "status": "ok",
        "quest_id": quest_id,
        "place_name": "Kinh thành Huế",
        "total_tasks": len(chain),
        "tasks": chain,
    }


@router.post("/explain-recommendation")
def explain_recommendation(request: ExplainRecommendationRequest) -> dict:
    """Explain why a place is recommended (stub for future implementation)."""
    return explain_recommendation_workflow(request.model_dump())


@router.post("/verify-capture")
def verify_capture(request: VerifyCaptureRequest) -> dict:
    """Verify if a capture completes a task (stub for future implementation)."""
    return verify_capture_workflow(request.model_dump())


@router.post("/generate-vlog")
def generate_vlog(request: GenerateVlogRequest) -> dict:
    """Generate a travel memory vlog (stub for future implementation)."""
    return generate_vlog_workflow(request.model_dump())

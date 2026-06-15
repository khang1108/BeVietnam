"""Question pool endpoints."""

from fastapi import APIRouter

from src.bevietnam.backend.app.schemas.question_pool import (
    QuestionPoolResponse,
    RuntimeContextRequest,
    SelectQuestionResponse,
)
from src.bevietnam.backend.app.services.question_pool_service import question_pool_service

router = APIRouter(prefix="/question-pool", tags=["Question Pool"])


@router.get("", response_model=QuestionPoolResponse)
async def list_question_pool():
    """
    List the currently loaded reusable question pool.

    The pool is generated offline from books and stored as JSON for this first
    vertical slice. Later this contract can be backed by PostgreSQL.
    """
    return question_pool_service.list_questions()


@router.post("/select", response_model=SelectQuestionResponse)
async def select_question(body: RuntimeContextRequest):
    """
    Select questions/tasks that match the user's live context.

    Context comes from client GPS plus optional weather/time/interests.
    If GOONG_API_KEY is configured, the service enriches location context via
    Goong reverse geocoding before ranking the pool.
    """
    return await question_pool_service.select(body)

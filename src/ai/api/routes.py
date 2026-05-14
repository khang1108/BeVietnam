from fastapi import APIRouter

from ai.capture_judge import verify_capture_workflow
from ai.common.schemas import (
    ExplainRecommendationRequest,
    GenerateTaskRequest,
    GenerateVlogRequest,
    VerifyCaptureRequest,
)
from ai.quest_maker import generate_task_workflow
from ai.story_weaver import generate_vlog_workflow
from ai.trip_advisor import explain_recommendation_workflow

router = APIRouter()


@router.post("/generate-task")
def generate_task(request: GenerateTaskRequest) -> dict:
    return generate_task_workflow(request.model_dump())


@router.post("/explain-recommendation")
def explain_recommendation(request: ExplainRecommendationRequest) -> dict:
    return explain_recommendation_workflow(request.model_dump())


@router.post("/verify-capture")
def verify_capture(request: VerifyCaptureRequest) -> dict:
    return verify_capture_workflow(request.model_dump())


@router.post("/generate-vlog")
def generate_vlog(request: GenerateVlogRequest) -> dict:
    return generate_vlog_workflow(request.model_dump())

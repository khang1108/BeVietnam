from services.ai.agents.capture_judge.agent import CaptureJudge
from services.ai.agents.publisher import PublisherAgent
from services.ai.common.tracing import traceable


@traceable(name="Capture Judge", run_type="chain")
def verify_capture_workflow(context: dict) -> dict:
    verification = CaptureJudge().verify(context)
    return PublisherAgent().publish_response(
        payload=verification,
        status="ok",
        metadata={
            "ai_generated": verification.get("ai_generated", False),
            "fallback": verification.get("fallback", False),
        },
    )

from src.bevietnam.ai.agents.capture_judge.agent import CaptureJudge
from src.bevietnam.ai.agents.publisher import PublisherAgent


def verify_capture_workflow(context: dict) -> dict:
    verification = CaptureJudge().verify(context)
    return PublisherAgent().publish_response(verification)

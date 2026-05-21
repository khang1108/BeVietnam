from ai.capture_judge.agent import CaptureJudge
from ai.publisher import PublisherAgent


def verify_capture_workflow(context: dict) -> dict:
    verification = CaptureJudge().verify(context)
    return PublisherAgent().publish_response(verification)

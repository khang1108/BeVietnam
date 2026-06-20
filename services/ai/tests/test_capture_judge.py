"""
Capture Judge tests.

Run with:
    PYTHONPATH=. services/backend/venv/bin/python services/ai/tests/test_capture_judge.py
"""

from services.ai.agents.capture_judge.agent import CaptureJudge
from services.ai.common.config import settings


class _StubGateway:
    def __init__(self, result):
        self.result = result
        self.calls = 0

    def generate_vision_json(self, **_kwargs):
        self.calls += 1
        return dict(self.result)


def _context(media_url="https://example.com/citadel.jpg"):
    return {
        "user_id": "demo",
        "language": "en",
        "task": {
            "title": "Find the Ngo Mon Gate",
            "description": "Take a photo of the main gate of Hue Imperial City.",
            "completion_requirement": "Photo must show the gate.",
            "place_name": "Hue Imperial City",
        },
        "capture": {"media_url": media_url},
    }


def test_missing_image_rejects_without_approving():
    result = CaptureJudge(gateway=_StubGateway({"match": True})).verify(_context(media_url=""))
    assert result["match"] is False
    assert result["status"] == "rejected"
    assert result["confidence"] == 0.0
    assert result["fallback"] is True


def test_mock_mode_needs_review_not_approved():
    old_provider = settings.llm_provider
    settings.llm_provider = "mock"
    try:
        result = CaptureJudge(gateway=_StubGateway({"match": True})).verify(_context())
    finally:
        settings.llm_provider = old_provider

    assert result["match"] is False
    assert result["status"] == "needs_review"
    assert result["confidence"] == 0.2
    assert result["fallback"] is True


def test_vision_match_approves_when_confident():
    old_provider = settings.llm_provider
    settings.llm_provider = "vllm"
    gateway = _StubGateway(
        {"match": True, "confidence": 0.88, "reason": "The image shows the gate."}
    )
    try:
        result = CaptureJudge(gateway=gateway).verify(_context())
    finally:
        settings.llm_provider = old_provider

    assert gateway.calls == 1
    assert result["match"] is True
    assert result["status"] == "approved"
    assert result["confidence"] == 0.88
    assert result["ai_generated"] is True
    assert result["fallback"] is False


def test_vision_mismatch_rejects():
    old_provider = settings.llm_provider
    settings.llm_provider = "vllm"
    gateway = _StubGateway(
        {"match": False, "confidence": 0.81, "reason": "The image is unrelated."}
    )
    try:
        result = CaptureJudge(gateway=gateway).verify(_context())
    finally:
        settings.llm_provider = old_provider

    assert result["match"] is False
    assert result["status"] == "rejected"
    assert result["confidence"] == 0.81


def test_vision_failure_needs_review():
    old_provider = settings.llm_provider
    settings.llm_provider = "vllm"
    try:
        result = CaptureJudge(gateway=_StubGateway({})).verify(_context())
    finally:
        settings.llm_provider = old_provider

    assert result["match"] is False
    assert result["status"] == "needs_review"
    assert result["fallback"] is True
    assert result["reason_code"] == "vision_unavailable"


def _run_all():
    tests = [v for k, v in sorted(globals().items()) if k.startswith("test_")]
    failures = 0
    for test in tests:
        try:
            test()
            print(f"PASS {test.__name__}")
        except AssertionError as exc:
            failures += 1
            print(f"FAIL {test.__name__}: {exc}")
        except Exception as exc:  # noqa: BLE001
            failures += 1
            print(f"ERROR {test.__name__}: {type(exc).__name__}: {exc}")
    print(f"\n{len(tests) - failures}/{len(tests)} passed")
    return failures


if __name__ == "__main__":
    import sys

    sys.exit(1 if _run_all() else 0)

class CaptureJudge:
    def verify(self, context: dict) -> dict:
        return {
            "status": "needs_review",
            "reason": "Vision verification is not enabled yet.",
            "confidence": 0.0,
        }

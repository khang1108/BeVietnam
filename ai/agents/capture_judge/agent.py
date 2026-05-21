class CaptureJudge:
    """
    Validates task completion evidence. Production would use vision + metadata.

    Demo: approve when the client sends a non-empty photo (data URL or URL).
    """

    def verify(self, context: dict) -> dict:
        capture = context.get("capture") or {}
        media = capture.get("media_url") or capture.get("image_url") or ""
        note = capture.get("note") or ""

        if isinstance(media, str) and "base64," in media and len(media) > 200:
            return {
                "status": "approved",
                "reason": "Demo mode: nhận ảnh hợp lệ để chứng minh hoàn thành.",
                "confidence": 0.85,
            }
        if isinstance(media, str) and media.startswith(("http://", "https://")):
            return {
                "status": "approved",
                "reason": "Demo mode: đã có liên kết ảnh.",
                "confidence": 0.8,
            }
        return {
            "status": "rejected",
            "reason": (
                note
                if note
                else "Chưa có ảnh minh chứng. Hãy tải lên một ảnh chụp tại địa điểm."
            ),
            "confidence": 0.0,
        }

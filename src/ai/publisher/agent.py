class PublisherAgent:
    def publish_response(self, payload: dict, status: str = "ok") -> dict:
        return {
            "status": status,
            "data": payload,
        }

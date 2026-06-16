"""
Publisher Agent — Response Packaging.

Prepares final AI workflow output for backend consumption.
Wraps task data in a standard envelope with status and metadata.

Usage:
    from services.ai.agents.publisher import PublisherAgent

    publisher = PublisherAgent()
    response = publisher.publish_response(task, status="ok")
"""

from datetime import datetime, timezone
from typing import Any


class PublisherAgent:
    """
    Publishing Agent.

    Wraps AI output in a standard response format that
    the backend can reliably parse and forward to clients.
    """

    def publish_response(
        self,
        payload: dict,
        status: str = "ok",
        metadata: dict[str, Any] | None = None,
    ) -> dict:
        """
        Package a task payload into a standard response envelope.

        Args:
            payload:  The task or result dict from the workflow.
            status:   "ok" or "error".
            metadata: Optional metadata (ai_generated, fallback, etc.)

        Returns:
            Standard response dict:
            {
                "status": "ok",
                "data": { ...task... },
                "metadata": { "ai_generated": true, "timestamp": "..." },
            }
        """
        response_metadata = {
            "timestamp": datetime.now(timezone.utc).isoformat(),
        }
        if metadata:
            response_metadata.update(metadata)

        return {
            "status": status,
            "data": payload,
            "metadata": response_metadata,
        }

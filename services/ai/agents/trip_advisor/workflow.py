"""
Trip Advisor Workflow.

Runs the recommendation agent and wraps the result in the standard Publisher
envelope, exposing whether the explanation was AI-generated or a fallback.
"""

from services.ai.agents.publisher import PublisherAgent
from services.ai.agents.trip_advisor.agent import TripAdvisorAgent
from services.ai.common.tracing import traceable


@traceable(name="Trip Advisor", run_type="chain")
def explain_recommendation_workflow(context: dict) -> dict:
    result = TripAdvisorAgent().explain(context)
    return PublisherAgent().publish_response(
        payload=result,
        status="ok",
        metadata={
            "ai_generated": result.get("ai_generated", False),
            "fallback": result.get("fallback", False),
        },
    )

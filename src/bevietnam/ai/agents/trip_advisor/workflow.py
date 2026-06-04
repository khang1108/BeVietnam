from ai.publisher import PublisherAgent
from ai.trip_advisor.agent import TripAdvisorAgent


def explain_recommendation_workflow(context: dict) -> dict:
    explanation = TripAdvisorAgent().explain(context)
    return PublisherAgent().publish_response(explanation)

from src.bevietnam.ai.agents.publisher import PublisherAgent
from src.bevietnam.ai.agents.trip_advisor.agent import TripAdvisorAgent


def explain_recommendation_workflow(context: dict) -> dict:
    explanation = TripAdvisorAgent().explain(context)
    return PublisherAgent().publish_response(explanation)

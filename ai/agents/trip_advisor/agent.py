class TripAdvisorAgent:
    def explain(self, context: dict) -> dict:
        return {
            "explanation": "Recommended because it matches the traveler's interests and current context.",
            "reason_codes": ["interest_match", "context_fit"],
        }

class SafetyKeeper:
    def validate_required_fields(self, payload: dict, required_fields: list[str]) -> bool:
        return all(field in payload and payload[field] for field in required_fields)

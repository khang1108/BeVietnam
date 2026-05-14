class QuestMaker:
    def generate(self, context: dict) -> dict:
        return {
            "title": "Find a local cultural detail",
            "description": "Capture one photo of a place detail that represents local culture.",
            "cultural_explanation": "Small visual details often reveal local identity.",
            "completion_requirement": "Upload one photo with location metadata.",
            "difficulty": "easy",
            "reason_codes": ["culture", "beginner_friendly"],
        }

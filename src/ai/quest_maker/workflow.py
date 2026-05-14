from ai.publisher import PublisherAgent
from ai.quest_maker.agent import QuestMaker
from ai.safety_keeper import SafetyKeeper


def generate_task_workflow(context: dict) -> dict:
    task = QuestMaker().generate(context)
    required_fields = ["title", "description", "cultural_explanation", "completion_requirement", "difficulty"]
    is_valid = SafetyKeeper().validate_required_fields(task, required_fields)
    if not is_valid:
        task = QuestMaker().generate({})
    return PublisherAgent().publish_response(task)

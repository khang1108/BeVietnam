from src.bevietnam.ai.agents.memory_curator import MemoryCurator
from src.bevietnam.ai.agents.publisher import PublisherAgent
from src.bevietnam.ai.agents.story_weaver.agent import StoryWeaver


def generate_vlog_workflow(context: dict) -> dict:
    captures = MemoryCurator().select_captures(context.get("captures", []))
    vlog = StoryWeaver().write({**context, "captures": captures})
    return PublisherAgent().publish_response(vlog)

"""
Quest Maker Nodes — Individual steps in the LangGraph workflow.

Each function is a LangGraph node that reads from and writes to
the QuestMakerState. Nodes are composed into a StateGraph in workflow.py.

Node execution order:
  prepare_context → retrieve_culture → generate_task → validate_task
                                                          ↓ (invalid)
                                                     fallback_task

Keeping nodes in a separate file makes the workflow logic easy to read
and each step easy to test independently.
"""

import logging
import uuid

from services.ai.common.llm import llm_gateway
from services.ai.agents.culture_scout import CultureScout
from services.ai.agents.quest_maker.fallback import get_fallback_task
from services.ai.agents.quest_maker.prompts import SYSTEM_PROMPT, build_user_prompt
from services.ai.agents.quest_maker.state import QuestMakerState
from services.ai.agents.safety_keeper import SafetyKeeper

logger = logging.getLogger(__name__)


# ── Required fields that every generated task must have ───────────────────────
REQUIRED_TASK_FIELDS = [
    "quest_id",
    "task_id",
    "step_index",
    "title",
    "description",
    "cultural_explanation",
    "completion_requirement",
    "difficulty",
]


def prepare_context(state: QuestMakerState) -> dict:
    """
    Node 1: Prepare and normalize the input context.

    Ensures all expected fields have safe defaults
    so downstream nodes don't need to check for None.
    """
    logger.info("🔧 Preparing context for user: %s", state.get("user_id", "unknown"))
    return {
        "user_id": state.get("user_id", "anonymous"),
        "latitude": state.get("latitude"),
        "longitude": state.get("longitude"),
        "interests": state.get("interests", []),
        "quest_state": state.get("quest_state", {}),
        "nearby_places": state.get("nearby_places", []),
        "language": state.get("language", "vi"),
    }


def retrieve_culture(state: QuestMakerState) -> dict:
    """
    Node 2: Retrieve cultural context using Culture Scout.

    Builds a search query from user interests and nearby places,
    then searches Qdrant for relevant cultural facts.
    """
    # Build a meaningful search query
    interests = state.get("interests", [])
    nearby = state.get("nearby_places", [])
    quest_state = state.get("quest_state", {})

    # Use place names from nearby or quest state as the query focus
    place_names = [p.get("name", "") for p in nearby if p.get("name")]
    query_parts = place_names + interests
    primary_place = nearby[0] if nearby else {}

    # If we have a current quest, add its context
    current_task = quest_state.get("current_task", {})
    if current_task.get("title"):
        query_parts.append(current_task["title"])

    query = " ".join(query_parts) if query_parts else "văn hóa Huế Hội An du lịch"
    place_filter = place_names[0] if place_names else ""

    logger.info("🔍 Culture Scout searching: '%s'", query[:60])
    scout = CultureScout()
    facts = scout.retrieve(
        query=query,
        place_name=place_filter,
        place_id=str(
            primary_place.get("place_id")
            or primary_place.get("id")
            or ""
        ),
        category=str(primary_place.get("category") or ""),
        language=state.get("language", "vi"),
        interests=interests,
    )

    logger.info("📚 Culture Scout returned %d facts", len(facts))
    return {"cultural_context": facts}


def generate_task(state: QuestMakerState) -> dict:
    """
    Node 3: Generate a cultural exploration task using Gemini.

    Builds a prompt from all available context and calls the LLM.
    Returns the raw generated task dict.
    """
    logger.info("🤖 Generating task with Gemini...")

    user_prompt = build_user_prompt(
        user_id=state.get("user_id", "anonymous"),
        latitude=state.get("latitude"),
        longitude=state.get("longitude"),
        interests=state.get("interests", []),
        quest_state=state.get("quest_state", {}),
        cultural_context=state.get("cultural_context", []),
        nearby_places=state.get("nearby_places", []),
        language=state.get("language", "vi"),
    )

    task = llm_gateway.generate_json(
        system_prompt=SYSTEM_PROMPT,
        user_prompt=user_prompt,
    )

    # Ensure task has an ID even if Gemini didn't generate one
    if task and "task_id" not in task:
        task["task_id"] = f"gen-{uuid.uuid4().hex[:8]}"

    if task:
        logger.info("✅ Gemini generated task: '%s'", task.get("title", "untitled"))
    else:
        logger.warning("⚠️ Gemini returned empty response")

    return {"generated_task": task}


def validate_task(state: QuestMakerState) -> dict:
    """
    Node 4: Validate the generated task with Safety Keeper.

    Checks that all required fields are present and non-empty.
    Sets is_valid flag to control workflow routing.
    """
    task = state.get("generated_task", {})

    if not task:
        logger.warning("❌ No task to validate (empty dict)")
        return {"is_valid": False, "errors": ["Generated task is empty"]}

    keeper = SafetyKeeper()
    is_valid = keeper.validate_required_fields(task, REQUIRED_TASK_FIELDS)

    if is_valid:
        logger.info("✅ Safety Keeper approved task")
        return {"is_valid": True, "errors": []}
    else:
        missing = [f for f in REQUIRED_TASK_FIELDS if f not in task or not task[f]]
        logger.warning("❌ Safety Keeper rejected: missing fields %s", missing)
        return {"is_valid": False, "errors": [f"Missing fields: {missing}"]}


def fallback_task(state: QuestMakerState) -> dict:
    """
    Fallback node: Return a curated task from the fallback chain.

    Called when Gemini fails or Safety Keeper rejects the output.
    Uses quest_state to determine which step to return.
    """
    quest_state = state.get("quest_state", {})
    current_step = quest_state.get("current_step_index", 0)
    next_step = current_step + 1

    logger.info("🔄 Using fallback task (step %d)", next_step)
    task = get_fallback_task(step_index=next_step)

    return {
        "generated_task": task,
        "is_valid": True,
        "errors": [],
    }

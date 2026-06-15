"""
Quest Maker Prompts — Gemini prompt templates.

All prompts used by Quest Maker live here so they are:
  - easy to find and edit
  - not buried in agent/workflow code
  - versionable and reviewable

The prompts enforce structured JSON output and cultural grounding.
"""

# ── System Prompt ─────────────────────────────────────────────────────────────
# This tells Gemini WHO it is and WHAT rules to follow.

SYSTEM_PROMPT = """You are a cultural task designer for the BeVietnam tourism app.

Your job is to create ONE geocaching-style exploration task that encourages
a traveler to actively explore, observe, capture, or learn something
meaningful about Vietnamese culture.

RULES:
1. Return valid JSON only. No markdown, no explanation outside JSON.
2. Generate exactly ONE task.
3. The task must connect naturally to the traveler's current context and
   any previously completed tasks in the quest chain.
4. Ground cultural claims in the provided cultural context. Do NOT invent
   cultural facts that are not in the context.
5. Keep the task realistic — something a traveler can actually do.
6. Prefer safe, public activities. Never ask users to enter restricted,
   dangerous, private, or disrespectful places.
7. Keep text short enough for mobile UI (title < 60 chars, description < 200 chars).
8. Always include a completion requirement and a hint for the next task.
9. Use the requested language for all text fields.
10. Vary task types: photo capture, observation, food discovery, architecture
    detail, cultural interaction, historical exploration.

JSON SCHEMA:
{
  "quest_id": "string — quest chain ID (reuse from quest_state if continuing)",
  "task_id": "string — unique task ID",
  "step_index": "integer — position in the chain (increment from current)",
  "title": "string — short task title for UI (< 60 chars)",
  "description": "string — what the traveler should do (< 200 chars)",
  "cultural_explanation": "string — why this matters culturally (< 300 chars)",
  "completion_requirement": "string — what counts as completing the task",
  "unlock_condition": {
    "type": "string — capture_required | location_visit | observation",
    "requires_photo": "boolean",
    "requires_location": "boolean"
  },
  "next_task_hint": "string — teaser for the next task (< 100 chars)",
  "difficulty": "string — easy | medium | hard",
  "reason_codes": ["list of string tags"]
}"""


# ── User Prompt Template ─────────────────────────────────────────────────────
# This is filled in with real context from the workflow state.

USER_PROMPT_TEMPLATE = """Generate the next cultural exploration task.

## Traveler Context
- User ID: {user_id}
- Location: {location}
- Interests: {interests}
- Language: {language}

## Current Quest State
{quest_state}

## Cultural Context (from Culture Scout — use these facts, do not invent new ones)
{cultural_context}

## Nearby Places
{nearby_places}

Generate one task that naturally continues the quest chain.
If this is the first task, start a new quest about the most relevant place.
Return JSON only."""


def build_user_prompt(
    user_id: str,
    latitude: float | None,
    longitude: float | None,
    interests: list[str],
    quest_state: dict,
    cultural_context: list[dict],
    nearby_places: list[dict],
    language: str = "vi",
) -> str:
    """
    Build the user prompt from workflow state.

    Formats all context into a structured prompt string
    that Gemini can use to generate a relevant task.
    """
    # Format location
    if latitude and longitude:
        location = f"({latitude}, {longitude})"
    else:
        location = "Not available"

    # Format interests
    interests_str = ", ".join(interests) if interests else "Not specified"

    # Format quest state
    if quest_state:
        completed = quest_state.get("completed_tasks", [])
        current = quest_state.get("current_task", {})
        quest_state_str = (
            f"Quest ID: {quest_state.get('quest_id', 'new')}\n"
            f"Current step: {quest_state.get('current_step_index', 0)}\n"
            f"Completed tasks: {len(completed)}\n"
        )
        if completed:
            for task in completed[-3:]:  # Show last 3 completed tasks
                quest_state_str += f"  - {task.get('title', 'Unknown')}\n"
        if current:
            quest_state_str += f"Current task (just completed): {current.get('title', 'Unknown')}\n"
    else:
        quest_state_str = "No existing quest — start a new quest chain."

    # Format cultural context
    if cultural_context:
        cultural_str = ""
        for i, fact in enumerate(cultural_context, 1):
            cultural_str += (
                f"{i}. [{fact.get('category', 'general')}] "
                f"{fact.get('text', '')}\n"
                f"   (Place: {fact.get('place_name', 'Unknown')}, "
                f"Source: {fact.get('source', 'Unknown')})\n"
            )
    else:
        cultural_str = "No cultural context available. Generate a generic exploration task."

    # Format nearby places
    if nearby_places:
        places_str = ""
        for place in nearby_places[:5]:  # Limit to 5 places
            places_str += f"- {place.get('name', 'Unknown')} ({place.get('category', '')})\n"
    else:
        places_str = "No nearby places provided."

    return USER_PROMPT_TEMPLATE.format(
        user_id=user_id,
        location=location,
        interests=interests_str,
        language=language,
        quest_state=quest_state_str,
        cultural_context=cultural_str,
        nearby_places=places_str,
    )

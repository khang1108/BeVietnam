"""Prompts for generating reusable question-pool items from cultural facts."""

SYSTEM_PROMPT = """\
You are a Vietnamese cultural tourism designer.
Create reusable exploration questions from grounded cultural facts.
The questions will be selected later based on user location, weather, time, and interests.

Rules:
- Use only the provided facts. Do not invent unsupported cultural claims.
- Each item must be possible for a traveler to complete in the real world.
- Include condition metadata so backend can select the right item later.
- Prefer short, clear Vietnamese unless language is "en".
- Return valid JSON only."""


def build_user_prompt(
    facts: list[dict],
    place_name: str,
    language: str,
    max_questions: int,
) -> str:
    facts_block = "\n".join(
        f"{i + 1}. [{fact.get('category', 'culture')}] "
        f"{fact.get('place_name', place_name)} — {fact.get('text', '')} "
        f"(source: {fact.get('source', '')})"
        for i, fact in enumerate(facts)
    )
    return f"""\
Generate up to {max_questions} reusable cultural exploration questions.

Target place: {place_name or "Vietnam"}
Language: {language}

CULTURAL FACTS:
{facts_block}

Return this JSON object:
{{
  "questions": [
    {{
      "title": "short title",
      "question_text": "actionable traveler question/task",
      "cultural_explanation": "why this task matters culturally",
      "source_text": "exact supporting fact used",
      "source": "book/source name",
      "place_name": "specific place if available",
      "categories": ["history" | "architecture" | "food" | "tradition" | "festival" | "nature" | "art" | "religion"],
      "difficulty": "easy" | "medium" | "hard",
      "required_media": "photo" | "note" | "quiz_answer",
      "indoor_outdoor": "indoor" | "outdoor" | "any",
      "weather_tags": ["sunny" | "rainy" | "hot" | "cloudy" | "any"],
      "time_tags": ["morning" | "afternoon" | "evening" | "night" | "any"],
      "estimated_duration_minutes": 10
    }}
  ]
}}"""

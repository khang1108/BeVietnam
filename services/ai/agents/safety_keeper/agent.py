"""
Safety Keeper — Validation and Safety Agent.

Validates AI-generated outputs before they reach the user.
Currently uses rule-based checks (field presence, length limits, safe content).
Can be extended with LLM-based safety checks later.

Usage:
    from services.ai.agents.safety_keeper import SafetyKeeper

    keeper = SafetyKeeper()
    is_valid = keeper.validate_required_fields(task, ["title", "description"])
    is_safe = keeper.validate_task_safety(task)
"""

import logging

logger = logging.getLogger(__name__)


class SafetyKeeper:
    """
    Validation and Safety Agent.

    Checks AI outputs for:
      - Required field presence
      - Text length limits (for mobile UI)
      - Safe content (no restricted/dangerous suggestions)
      - Schema compliance
    """

    def validate_required_fields(
        self,
        payload: dict,
        required_fields: list[str],
    ) -> bool:
        """
        Check that all required fields exist and are non-empty.

        Args:
            payload:         The dict to validate.
            required_fields: List of field names that must be present.

        Returns:
            True if all required fields are present and non-empty.
        """
        for field in required_fields:
            if field not in payload or not payload[field]:
                logger.warning("Validation failed: missing or empty field '%s'", field)
                return False
        return True

    def validate_task_safety(self, task: dict) -> bool:
        """
        Check that a generated task is safe for travelers.

        Rules:
          - Title must be < 100 characters
          - Description must be < 500 characters
          - Difficulty must be a known value
          - No unsafe keywords in text fields

        Returns:
            True if the task passes all safety checks.
        """
        # Length checks
        title = task.get("title", "")
        description = task.get("description", "")

        if len(title) > 100:
            logger.warning("Task title too long (%d chars)", len(title))
            return False

        if len(description) > 500:
            logger.warning("Task description too long (%d chars)", len(description))
            return False

        # Difficulty must be a known value
        valid_difficulties = {"easy", "medium", "hard"}
        difficulty = task.get("difficulty", "")
        if difficulty and difficulty not in valid_difficulties:
            logger.warning("Unknown difficulty: '%s'", difficulty)
            return False

        # Basic unsafe content check
        unsafe_keywords = ["nguy hiểm", "cấm", "trái phép", "dangerous", "illegal", "restricted"]
        all_text = f"{title} {description}".lower()
        for keyword in unsafe_keywords:
            if keyword in all_text:
                logger.warning("Unsafe keyword detected: '%s'", keyword)
                return False

        return True

    def validate_full(self, task: dict, required_fields: list[str]) -> tuple[bool, list[str]]:
        """
        Run all validations and return detailed results.

        Returns:
            Tuple of (is_valid, list_of_error_messages).
        """
        errors: list[str] = []

        if not self.validate_required_fields(task, required_fields):
            missing = [f for f in required_fields if f not in task or not task[f]]
            errors.append(f"Missing required fields: {missing}")

        if not self.validate_task_safety(task):
            errors.append("Task failed safety checks")

        is_valid = len(errors) == 0
        return is_valid, errors

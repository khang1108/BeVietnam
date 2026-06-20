"""
Optional LangSmith tracing helpers.

LangSmith custom instrumentation uses @traceable and is controlled by
LANGSMITH_TRACING / LANGSMITH_API_KEY / LANGSMITH_PROJECT.
Source: https://docs.langchain.com/langsmith/annotate-code#use-traceable--traceable
"""

from collections.abc import Callable
from functools import wraps
from typing import Any, TypeVar

from services.ai.common.config import settings

F = TypeVar("F", bound=Callable[..., Any])


try:
    from langsmith import traceable as _langsmith_traceable
except Exception:  # pragma: no cover - keeps local/dev envs dependency-safe
    _langsmith_traceable = None


def traceable(name: str, run_type: str = "chain") -> Callable[[F], F]:
    """Return a LangSmith traceable decorator, or a no-op if unavailable."""

    def decorator(func: F) -> F:
        if _langsmith_traceable is None:
            return func

        traced = _langsmith_traceable(
            name=name,
            run_type=run_type,
            project_name=settings.langsmith_project,
        )(func)

        @wraps(func)
        def wrapper(*args: Any, **kwargs: Any) -> Any:
            return traced(*args, **kwargs)

        return wrapper  # type: ignore[return-value]

    return decorator

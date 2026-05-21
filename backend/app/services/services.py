"""
Service layer — business logic between endpoints and repositories.

Services orchestrate repositories, apply business rules,
and are the correct place to put logic like:
- Auth token creation
- Data validation beyond Pydantic
- Cross-entity operations

TODO(Backend): Implement each method by calling the corresponding repository.
"""
import uuid
from datetime import datetime, timezone
from typing import Optional

from app.schemas.capture import CaptureCreateRequest, CaptureResponse

# ── In-memory fallback (Sprint 1 only — replace with DB calls) ──────────────
_captures_store: list[CaptureResponse] = []


class CaptureService:

    async def create_capture(self, body: CaptureCreateRequest) -> CaptureResponse:
        """
        Create a capture record.
        Sprint 1: in-memory store.
        TODO: Replace with capture_repo.create(data) + DB session.
        """
        now = datetime.now(timezone.utc)
        capture = CaptureResponse(
            id=str(uuid.uuid4()),
            user_id=body.user_id,
            task_id=body.task_id,
            place_id=body.place_id,
            timestamp=body.timestamp or now,
            latitude=body.latitude,
            longitude=body.longitude,
            media_url=body.media_url,
            note=body.note,
            created_at=now,
        )
        _captures_store.append(capture)
        return capture


class PlaceService:

    async def get_places(
        self,
        category: Optional[str] = None,
        limit: int = 10,
        offset: int = 0,
    ) -> tuple[int, list]:
        """
        Get paginated places list.
        TODO: Replace with place_repo.get_all(category, limit, offset)
        """
        raise NotImplementedError("Connect to DB via PlaceRepository")

    async def get_place_detail(self, place_id: str):
        """
        Get single place by ID.
        TODO: Replace with place_repo.get_by_id(place_id)
        """
        raise NotImplementedError("Connect to DB via PlaceRepository")


# Singleton service instances
capture_service = CaptureService()
place_service = PlaceService()

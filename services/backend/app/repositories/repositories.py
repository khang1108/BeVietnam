"""
Repository layer — thin DB access wrappers.

TODO(Backend): Replace NotImplementedError with SQLAlchemy session calls.
Each method receives a db: AsyncSession from dependency injection.
"""
from typing import Optional, List


class PlaceRepository:
    """CRUD operations for Place entity."""

    async def get_all(
        self,
        category: Optional[str] = None,
        limit: int = 10,
        offset: int = 0,
    ) -> tuple[int, list]:
        """
        TODO: SELECT * FROM places WHERE category=? LIMIT ? OFFSET ?
        Returns (total_count, items)
        """
        raise NotImplementedError

    async def get_by_id(self, place_id: str):
        """TODO: SELECT * FROM places WHERE id=?"""
        raise NotImplementedError


class CaptureRepository:
    """CRUD operations for Capture entity."""

    async def create(self, data: dict) -> dict:
        """TODO: INSERT INTO captures VALUES (...)"""
        raise NotImplementedError

    async def get_by_user(self, user_id: str) -> list:
        """TODO: SELECT * FROM captures WHERE user_id=?"""
        raise NotImplementedError


class UserRepository:
    """CRUD operations for User entity."""

    async def get_by_email(self, email: str) -> Optional[dict]:
        """TODO: SELECT * FROM users WHERE email=?"""
        raise NotImplementedError

    async def create(self, data: dict) -> dict:
        """TODO: INSERT INTO users VALUES (...)"""
        raise NotImplementedError


# Singleton instances — will accept db session via DI when implemented
place_repo = PlaceRepository()
capture_repo = CaptureRepository()
user_repo = UserRepository()

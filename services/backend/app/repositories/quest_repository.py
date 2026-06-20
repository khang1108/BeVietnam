# repositories/quest_repository.py
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from services.backend.app.models.quest import QuestTask, UserProgress

class QuestRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_user_progress(self, user_id: str, chain_id: int) -> UserProgress | None:
        stmt = select(UserProgress).where(
            UserProgress.user_id == user_id, 
            UserProgress.chain_id == chain_id
        )
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def create_progress(self, progress: UserProgress):
        self.db.add(progress)
        await self.db.commit()
        await self.db.refresh(progress)
        return progress

    async def update_progress(self):
        await self.db.commit()

    async def get_next_task(self, chain_id: int, current_order: int) -> QuestTask | None:
        stmt = select(QuestTask).where(
            QuestTask.chain_id == chain_id,
            QuestTask.order_index > current_order
        ).order_by(QuestTask.order_index.asc()).limit(1)
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

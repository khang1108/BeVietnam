# services/quest_service.py
from services.backend.app.models.quest import UserProgress
from services.backend.app.repositories.quest_repository import QuestRepository

class QuestService:
    def __init__(self, repo: QuestRepository):
        self.repo = repo

    async def start_quest(self, user_id: str, chain_id: int, first_task_id: int):
        existing = await self.repo.get_user_progress(user_id, chain_id)
        if existing:
            return existing # Hoặc raise Exception tùy business
        
        new_progress = UserProgress(
            user_id=user_id, 
            chain_id=chain_id, 
            current_task_id=first_task_id
        )
        return await self.repo.create_progress(new_progress)

    async def complete_current_task(self, user_id: str, chain_id: int, current_order_index: int):
        progress = await self.repo.get_user_progress(user_id, chain_id)
        if not progress or progress.is_completed:
            raise ValueError("Invalid progress state")

        # Logic unlock task tiếp theo
        next_task = await self.repo.get_next_task(chain_id, current_order_index)
        
        if next_task:
            progress.current_task_id = next_task.id
        else:
            progress.is_completed = True # Hết task -> Hoàn thành chain

        await self.repo.update_progress()
        return progress

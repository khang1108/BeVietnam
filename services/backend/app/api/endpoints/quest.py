# api/endpoints/quests.py
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from app.api.dependencies import get_db
from app.repositories.quest_repository import QuestRepository
from app.services.quest_service import QuestService

router = APIRouter()

def get_quest_service(db: AsyncSession = Depends(get_db)):
    repo = QuestRepository(db)
    return QuestService(repo)

@router.post("/quest/{chain_id}/complete")
async def complete_task(
    chain_id: int, 
    user_id: int, # Tạm thời fake user_id do BE-01 không làm Auth
    current_order: int,
    service: QuestService = Depends(get_quest_service)
):
    result = await service.complete_current_task(user_id, chain_id, current_order)
    return {"status": "success", "data": result}
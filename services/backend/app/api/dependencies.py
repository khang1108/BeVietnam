from typing import AsyncGenerator

from services.backend.app.core.database import AsyncSessionLocal

async def get_db() -> AsyncGenerator:
    """Dependency provider cho FastAPI routes"""
    async with AsyncSessionLocal() as session:
        try:
            yield session
        finally:
            await session.close()

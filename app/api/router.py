from fastapi import APIRouter
from app.core.config import settings
from app.api.endpoints import health, places, feed, storyline, captures, logs, auth
api_router = APIRouter(prefix=settings.API_PREFIX)

api_router.include_router(health.router)
api_router.include_router(places.router)
api_router.include_router(feed.router)
api_router.include_router(storyline.router)
api_router.include_router(captures.router)
api_router.include_router(logs.router)
api_router.include_router(auth.router)

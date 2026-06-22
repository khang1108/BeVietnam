"""
API v1 router — registers all feature routers under /api/v1 prefix.

To add a new feature:
    1. Create app/api/v1/endpoints/<feature>.py
    2. Import and include router here
"""
from fastapi import APIRouter
from services.backend.app.api.endpoints import storyline
from services.backend.app.api.endpoints import (
    auth,
    captures,
    feed,
    health,
    logs,
    places,
    preferences,
    quest,
    question_pool,
    uploads,
    weather,
)

api_router = APIRouter()

api_router.include_router(health.router)
api_router.include_router(auth.router)
api_router.include_router(preferences.router)
api_router.include_router(places.router)
api_router.include_router(feed.router)
api_router.include_router(storyline.router)
api_router.include_router(captures.router)
api_router.include_router(logs.router)
api_router.include_router(quest.router)
api_router.include_router(question_pool.router)
api_router.include_router(uploads.router)
api_router.include_router(weather.router)

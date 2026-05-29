from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.core.config import settings
from app.api.router import api_router
from app.core.database import engine, Base, SessionLocal
from app.models import user, place, capture  

Base.metadata.create_all(bind=engine)

from app.services.place_service import PlaceService
db = SessionLocal()
try:
    PlaceService(db).seed()
finally:
    db.close()

app = FastAPI(  
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    description="Tourism App Backend API",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router)


@app.on_event("startup")
async def startup_event():
    print(f"🚀 {settings.PROJECT_NAME} v{settings.VERSION} started")
    print(f"📡 AI Core base URL: {settings.AI_CORE_BASE_URL}")

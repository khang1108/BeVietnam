from fastapi import FastAPI

from ai.api.routes import router

app = FastAPI(title="BeVietnam AI Core")
app.include_router(router)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "service": "ai-core"}

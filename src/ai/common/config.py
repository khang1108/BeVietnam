from pydantic import BaseModel


class Settings(BaseModel):
    qdrant_host: str = "vector-db"
    qdrant_port: int = 6333
    llm_provider: str = "mock"


settings = Settings()

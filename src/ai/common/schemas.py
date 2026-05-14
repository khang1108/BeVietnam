from pydantic import BaseModel


class GenerateTaskRequest(BaseModel):
    user_id: str
    latitude: float | None = None
    longitude: float | None = None
    interests: list[str] = []


class GeneratedTask(BaseModel):
    title: str
    description: str
    cultural_explanation: str
    completion_requirement: str
    difficulty: str
    reason_codes: list[str] = []


class ExplainRecommendationRequest(BaseModel):
    user_id: str
    place: dict
    interests: list[str] = []
    context: dict = {}


class RecommendationExplanation(BaseModel):
    explanation: str
    reason_codes: list[str] = []


class VerifyCaptureRequest(BaseModel):
    user_id: str
    task: dict
    capture: dict


class CaptureVerification(BaseModel):
    status: str
    reason: str
    confidence: float


class GenerateVlogRequest(BaseModel):
    user_id: str
    local_date: str
    captures: list[dict] = []


class GeneratedVlog(BaseModel):
    title: str
    summary: str
    body: str

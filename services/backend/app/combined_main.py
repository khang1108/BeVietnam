"""Combined Azure entrypoint for backend + AI Core in one ASGI app."""

from services.ai.main import app as ai_app
from services.backend.app.main import app as backend_app

backend_app.mount("/ai-core", ai_app)

app = backend_app

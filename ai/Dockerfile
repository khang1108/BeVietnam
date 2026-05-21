FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

# Install dependencies first (cached layer)
COPY requirements.txt .
RUN pip install --upgrade pip && pip install -r requirements.txt

# Copy AI source code
COPY . ./ai

ENV PYTHONPATH=/app

EXPOSE 8000

CMD ["uvicorn", "ai.main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]

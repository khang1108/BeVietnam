FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

COPY requirements.txt .
RUN pip install --upgrade pip && pip install -r requirements.txt

COPY app ./src/bevietnam/backend/app
COPY alembic ./src/bevietnam/backend/alembic
COPY alembic.ini ./src/bevietnam/backend/alembic.ini

ENV PYTHONPATH=/app

EXPOSE 8000

CMD ["uvicorn", "src.bevietnam.backend.app.main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]

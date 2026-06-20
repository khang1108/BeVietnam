"""
Auth endpoint smoke tests without a live database.

Run with:
    PYTHONPATH=. services/backend/venv/bin/python services/backend/tests/test_auth_flow.py
"""

from datetime import datetime, timezone

from fastapi.testclient import TestClient

from services.backend.app.api.dependencies import get_current_user, get_db
from services.backend.app.main import app
from services.backend.app.models.models import UserModel
from services.backend.app.services.auth_service import AuthService


class _FakeSession:
    pass


async def _override_db():
    yield _FakeSession()


async def _override_current_user():
    return UserModel(
        id="user-1",
        name="Demo User",
        email="demo@example.com",
        hashed_password="not-returned",
        created_at=datetime.now(timezone.utc),
    )


def _client() -> TestClient:
    app.dependency_overrides[get_db] = _override_db
    app.dependency_overrides[get_current_user] = _override_current_user
    return TestClient(app)


def test_auth_register_login_me():
    from services.backend.app.api.endpoints import auth

    class StubAuthService:
        def __init__(self, _db):
            pass

        async def register(self, name, email, _password):
            return UserModel(
                id="user-1",
                name=name,
                email=email,
                hashed_password="hash",
                created_at=datetime.now(timezone.utc),
            )

        async def login(self, email, _password):
            user = UserModel(
                id="user-1",
                name="Demo User",
                email=email,
                hashed_password="hash",
                created_at=datetime.now(timezone.utc),
            )
            return "jwt-token", user

    old_service = auth.AuthService
    auth.AuthService = StubAuthService
    try:
        client = _client()
        register = client.post(
            "/api/v1/auth/register",
            json={
                "name": "Demo User",
                "email": "DEMO@example.com",
                "password": "super-secret-123",
            },
        )
        assert register.status_code == 201, register.text
        assert register.json()["email"] == "DEMO@example.com"
        assert "password" not in register.text

        login = client.post(
            "/api/v1/auth/login",
            json={"email": "demo@example.com", "password": "super-secret-123"},
        )
        assert login.status_code == 200, login.text
        assert login.json()["access_token"] == "jwt-token"
        assert login.json()["token_type"] == "bearer"

        token = client.post(
            "/api/v1/auth/token",
            data={"username": "demo@example.com", "password": "super-secret-123"},
        )
        assert token.status_code == 200, token.text
        assert token.json()["access_token"] == "jwt-token"

        me = client.get("/api/v1/auth/me", headers={"Authorization": "Bearer jwt-token"})
        assert me.status_code == 200, me.text
        assert me.json()["id"] == "user-1"
    finally:
        auth.AuthService = old_service
        app.dependency_overrides.clear()


def test_auth_service_register_rejects_duplicate_email():
    import asyncio
    from fastapi import HTTPException

    class StubRepo:
        def __init__(self, _db):
            self.created = None

        async def get_by_email(self, _email):
            return UserModel(
                id="existing",
                name="Existing User",
                email="demo@example.com",
                hashed_password="hash",
                created_at=datetime.now(timezone.utc),
            )

        async def create(self, name, email, hashed_password):
            self.created = (name, email, hashed_password)

    import services.backend.app.services.auth_service as auth_service

    old_repo = auth_service.UserRepository
    auth_service.UserRepository = StubRepo
    try:
        try:
            asyncio.run(
                AuthService(_FakeSession()).register(
                    "Demo User",
                    "demo@example.com",
                    "super-secret-123",
                )
            )
        except HTTPException as exc:
            assert exc.status_code == 400
            assert exc.detail == "Email already registered"
        else:
            raise AssertionError("duplicate email should fail")
    finally:
        auth_service.UserRepository = old_repo


def _run_all():
    tests = [v for k, v in sorted(globals().items()) if k.startswith("test_")]
    failures = 0
    for test in tests:
        try:
            test()
            print(f"PASS {test.__name__}")
        except AssertionError as exc:
            failures += 1
            print(f"FAIL {test.__name__}: {exc}")
        except Exception as exc:  # noqa: BLE001
            failures += 1
            print(f"ERROR {test.__name__}: {type(exc).__name__}: {exc}")
    print(f"\n{len(tests) - failures}/{len(tests)} passed")
    return failures


if __name__ == "__main__":
    import sys

    sys.exit(1 if _run_all() else 0)

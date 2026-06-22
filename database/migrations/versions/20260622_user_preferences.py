"""Create user_preferences table for per-user feed personalization.

Revision ID: 20260622_user_prefs
Revises: 20260620_auth_users
Create Date: 2026-06-22
"""

from typing import Sequence

from alembic import op


revision: str = "20260622_user_prefs"
down_revision: str | Sequence[str] | None = "20260620_auth_users"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.execute(
        """
        CREATE TABLE IF NOT EXISTS user_preferences (
            user_id VARCHAR PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
            interests JSONB NOT NULL DEFAULT '[]'::jsonb,
            home_latitude DOUBLE PRECISION,
            home_longitude DOUBLE PRECISION,
            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
        )
        """
    )


def downgrade() -> None:
    op.execute("DROP TABLE IF EXISTS user_preferences")

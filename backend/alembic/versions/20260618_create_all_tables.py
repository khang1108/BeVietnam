"""Create all app tables

Revision ID: 20260618_create_all_tables
Revises: 0aacb856469b
Create Date: 2026-06-18 00:00:00.000000
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '20260618_create_all_tables'
down_revision: Union[str, Sequence[str], None] = '0aacb856469b'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        'users',
        sa.Column('id', sa.String(length=36), primary_key=True, nullable=False),
        sa.Column('name', sa.String(length=100), nullable=False),
        sa.Column('email', sa.String(length=200), nullable=False),
        sa.Column('hashed_password', sa.String(length=200), nullable=False),
        sa.Column('is_active', sa.Boolean(), nullable=False, server_default=sa.text('true')),
        sa.Column('created_at', sa.DateTime(), nullable=True),
        sa.UniqueConstraint('email', name='uq_users_email'),
    )
    op.create_index(op.f('ix_users_email'), 'users', ['email'], unique=True)

    op.create_table(
        'places',
        sa.Column('id', sa.String(length=36), primary_key=True, nullable=False),
        sa.Column('name', sa.String(length=200), nullable=False),
        sa.Column('category', sa.String(length=50), nullable=False),
        sa.Column('description', sa.Text(), nullable=False),
        sa.Column('latitude', sa.Float(), nullable=False),
        sa.Column('longitude', sa.Float(), nullable=False),
        sa.Column('image_url', sa.String(length=500), nullable=True),
        sa.Column('reference_url', sa.String(length=500), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
    )

    op.create_table(
        'captures',
        sa.Column('id', sa.String(length=36), primary_key=True, nullable=False),
        sa.Column('user_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=False),
        sa.Column('task_id', sa.String(length=200), nullable=True),
        sa.Column('place_id', sa.String(length=36), sa.ForeignKey('places.id'), nullable=False),
        sa.Column('timestamp', sa.DateTime(), nullable=False),
        sa.Column('latitude', sa.Float(), nullable=True),
        sa.Column('longitude', sa.Float(), nullable=True),
        sa.Column('media_url', sa.String(length=500), nullable=True),
        sa.Column('note', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=True),
    )
    op.create_index(op.f('ix_captures_user_id'), 'captures', ['user_id'], unique=False)
    op.create_index(op.f('ix_captures_place_id'), 'captures', ['place_id'], unique=False)

    op.create_table(
        'quest_chains',
        sa.Column('id', sa.Integer(), primary_key=True, nullable=False),
        sa.Column('name', sa.String(), nullable=False),
        sa.Column('description', sa.String(), nullable=True),
    )

    op.create_table(
        'quest_tasks',
        sa.Column('id', sa.Integer(), primary_key=True, nullable=False),
        sa.Column('chain_id', sa.Integer(), sa.ForeignKey('quest_chains.id'), nullable=True),
        sa.Column('order_index', sa.Integer(), nullable=False),
        sa.Column('title', sa.String(), nullable=False),
        sa.Column('requirement', sa.String(), nullable=True),
    )

    op.create_table(
        'user_progress',
        sa.Column('id', sa.Integer(), primary_key=True, nullable=False),
        sa.Column('user_id', sa.String(length=36), sa.ForeignKey('users.id'), nullable=True),
        sa.Column('chain_id', sa.Integer(), sa.ForeignKey('quest_chains.id'), nullable=True),
        sa.Column('current_task_id', sa.Integer(), sa.ForeignKey('quest_tasks.id'), nullable=True),
        sa.Column('is_completed', sa.Boolean(), nullable=False, server_default=sa.text('false')),
    )


def downgrade() -> None:
    op.drop_table('user_progress')
    op.drop_table('quest_tasks')
    op.drop_table('quest_chains')
    op.drop_table('captures')
    op.drop_table('places')
    op.drop_table('users')

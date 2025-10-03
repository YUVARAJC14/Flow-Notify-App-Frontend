"""Add created_at to tasks

Revision ID: d6508aeb50b3
Revises: a259f5cb8040
Create Date: 2025-09-28 12:12:10.909130

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from datetime import datetime


# revision identifiers, used by Alembic.
revision: str = 'd6508aeb50b3'
down_revision: Union[str, Sequence[str], None] = 'a259f5cb8040'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    with op.batch_alter_table('tasks', schema=None) as batch_op:
        batch_op.add_column(sa.Column('created_at', sa.DateTime(), nullable=False, server_default=sa.func.now()))


def downgrade() -> None:
    """Downgrade schema."""
    with op.batch_alter_table('tasks', schema=None) as batch_op:
        batch_op.drop_column('created_at')
"""Change task and event id columns to string

Revision ID: a259f5cb8040
Revises: d88beb3eb479
Create Date: 2025-09-27 21:36:40.093698

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = 'a259f5cb8040'
down_revision: Union[str, Sequence[str], None] = 'd88beb3eb479'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    with op.batch_alter_table('events', schema=None) as batch_op:
        batch_op.alter_column('id',
                   existing_type=sa.INTEGER(),
                   type_=sa.String(),
                   existing_nullable=False)

    with op.batch_alter_table('tasks', schema=None) as batch_op:
        batch_op.alter_column('id',
                   existing_type=sa.INTEGER(),
                   type_=sa.String(),
                   existing_nullable=False)


def downgrade() -> None:
    """Downgrade schema."""
    with op.batch_alter_table('tasks', schema=None) as batch_op:
        batch_op.alter_column('id',
                   existing_type=sa.String(),
                   type_=sa.INTEGER(),
                   existing_nullable=False)

    with op.batch_alter_table('events', schema=None) as batch_op:
        batch_op.alter_column('id',
                   existing_type=sa.String(),
                   type_=sa.INTEGER(),
                   existing_nullable=False)
"""Add event support to kanban cards

Revision ID: c547ac2ec9da
Revises: c6f281db0d05
Create Date: 2025-09-30 12:46:43.231074

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = 'c547ac2ec9da'
down_revision: Union[str, Sequence[str], None] = 'c6f281db0d05'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    """Upgrade schema."""
    with op.batch_alter_table('kanban_cards', schema=None) as batch_op:
        batch_op.add_column(sa.Column('event_id', sa.String(), nullable=True))
        batch_op.alter_column('task_id', existing_type=sa.String(), nullable=True)
        batch_op.create_foreign_key(batch_op.f('kanban_cards_event_id_fkey'), 'events', ['event_id'], ['id'])
        batch_op.create_check_constraint(
            'cc_card_task_or_event',
            '(task_id IS NOT NULL AND event_id IS NULL) OR (task_id IS NULL AND event_id IS NOT NULL)'
        )

def downgrade() -> None:
    """Downgrade schema."""
    with op.batch_alter_table('kanban_cards', schema=None) as batch_op:
        batch_op.drop_constraint('cc_card_task_or_event', type_='check')
        batch_op.drop_constraint(batch_op.f('kanban_cards_event_id_fkey'), type_='foreignkey')
        batch_op.alter_column('task_id', existing_type=sa.String(), nullable=False)
        batch_op.drop_column('event_id')


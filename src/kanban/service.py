from sqlalchemy.orm import Session, joinedload
from . import models
from src.tasks.models import Task
from src.events.models import Event
from sqlalchemy import and_
from datetime import datetime, timedelta

def get_boards_by_user(db: Session, user_id: str):
    # Eagerly load the entire board structure
    board = db.query(models.KanbanBoard).options(
        joinedload(models.KanbanBoard.columns).joinedload(models.KanbanColumn.cards)
    ).filter(models.KanbanBoard.owner_id == user_id).first()

    # 1. SETUP: Create board and default columns if they don't exist
    if not board:
        board = models.KanbanBoard(name="My Board", owner_id=user_id)
        db.add(board)
        db.flush() # Ensure board.id is available
        db.add_all([
            models.KanbanColumn(name="To Do", position=0, board_id=board.id),
            models.KanbanColumn(name="In Progress", position=1, board_id=board.id),
            models.KanbanColumn(name="Done", position=2, board_id=board.id)
        ])
        db.commit()
        # Re-query to get the newly created structure
        board = db.query(models.KanbanBoard).options(
            joinedload(models.KanbanBoard.columns).joinedload(models.KanbanColumn.cards)
        ).filter(models.KanbanBoard.owner_id == user_id).first()

    todo_col = next((c for c in board.columns if c.name == 'To Do'), None)
    inprogress_col = next((c for c in board.columns if c.name == 'In Progress'), None)
    done_col = next((c for c in board.columns if c.name == 'Done'), None)

    # This should ideally not happen after the setup block, but as a safeguard:
    if not all([todo_col, inprogress_col, done_col]):
        # Handle missing default columns if necessary
        # For now, we'll assume the initial setup is sufficient.
        pass

    # 2. STATE CALCULATION: Determine the correct state for all items
    all_user_tasks = db.query(Task).filter(Task.owner_id == user_id).all()
    all_user_events = db.query(Event).filter(Event.owner_id == user_id).all()
    all_items = all_user_tasks + all_user_events

    cards_by_item_id = {
        card.task_id or card.event_id: card for col in board.columns for card in col.cards
    }

    now = datetime.now()
    one_hour_from_now = now + timedelta(hours=1)
    needs_commit = False

    for item in all_items:
        target_col = todo_col
        item_due_time = None

        if isinstance(item, Task) and item.due_date:
            item_due_time = datetime.combine(item.due_date, item.due_time or datetime.min.time())
        elif isinstance(item, Event):
            item_due_time = item.start_datetime

        if item.completed:
            target_col = done_col
        elif item_due_time and now <= item_due_time < one_hour_from_now:
            target_col = inprogress_col
        
        card = cards_by_item_id.get(str(item.id))

        if card:
            if card.column_id != target_col.id:
                card.column_id = target_col.id
                # Position will be re-calculated later
                needs_commit = True
            # Mark this card as processed
            cards_by_item_id.pop(str(item.id), None)
        else: # New item, create a card
            new_card = models.KanbanCard(
                column_id=target_col.id,
                task_id=str(item.id) if isinstance(item, Task) else None,
                event_id=str(item.id) if isinstance(item, Event) else None,
                position=0 # Placeholder position
            )
            db.add(new_card)
            needs_commit = True

    # 3. CLEANUP: Remove cards for deleted items
    if cards_by_item_id: # Any cards left in the dict are for deleted items
        for card_id_to_delete in cards_by_item_id.values():
            db.delete(card_id_to_delete)
            needs_commit = True

    if needs_commit:
        db.commit()

    # 4. RE-ORDER: Normalize positions in all columns
    for col in board.columns:
        for i, card in enumerate(sorted(col.cards, key=lambda c: c.position)):
            if card.position != i:
                card.position = i
                needs_commit = True

    if needs_commit:
        db.commit()
        # Re-fetch the final state of the board
        board = db.query(models.KanbanBoard).options(
            joinedload(models.KanbanBoard.columns).joinedload(models.KanbanColumn.cards)
        ).filter(models.KanbanBoard.owner_id == user_id).first()

    return [board] if board else []

def move_card(db: Session, card_id: str, new_column_id: str, new_position: int):
    card_to_move = db.query(models.KanbanCard).filter(models.KanbanCard.id == card_id).first()
    if not card_to_move:
        return None

    old_column_id = card_to_move.column_id
    old_position = card_to_move.position

    # Remove card from old column and shift positions
    if old_column_id != new_column_id:
        old_column_cards = db.query(models.KanbanCard).filter(
            models.KanbanCard.column_id == old_column_id,
            models.KanbanCard.id != card_id
        ).order_by(models.KanbanCard.position).all()
        for i, card in enumerate(old_column_cards):
            if card.position > old_position:
                card.position -= 1

    # Add card to new column and shift positions
    new_column_cards = db.query(models.KanbanCard).filter(
        models.KanbanCard.column_id == new_column_id,
        models.KanbanCard.id != card_id
    ).order_by(models.KanbanCard.position).all()

    # Make space for the moved card
    for card in new_column_cards:
        if card.position >= new_position:
            card.position += 1
    
    # Update the card itself
    card_to_move.column_id = new_column_id
    card_to_move.position = new_position

    db.commit()
    db.refresh(card_to_move)
    return card_to_move

# More business logic will be added here

from sqlalchemy.orm import Session, joinedload
from . import models
from src.tasks.models import Task
from src.events.models import Event

def get_boards_by_user(db: Session, user_id: str):
    # Eagerly load columns and cards to prevent multiple queries
    board = db.query(models.KanbanBoard).options(
        joinedload(models.KanbanBoard.columns).joinedload(models.KanbanColumn.cards)
    ).filter(models.KanbanBoard.owner_id == user_id).first()

    # If no board exists, create a default one
    if not board:
        new_board = models.KanbanBoard(name="My Board", owner_id=user_id)
        db.add(new_board)
        db.flush()
        todo_column = models.KanbanColumn(name="To Do", position=0, board_id=new_board.id)
        inprogress_column = models.KanbanColumn(name="In Progress", position=1, board_id=new_board.id)
        done_column = models.KanbanColumn(name="Done", position=2, board_id=new_board.id)
        db.add_all([todo_column, inprogress_column, done_column])
        db.commit()
        # Re-query to get the full board structure
        board = db.query(models.KanbanBoard).options(
            joinedload(models.KanbanBoard.columns).joinedload(models.KanbanColumn.cards)
        ).filter(models.KanbanBoard.owner_id == user_id).first()

    # Find the "To Do" column
    todo_column = next((col for col in board.columns if col.name == "To Do"), None)
    if not todo_column:
        # If for some reason "To Do" column is missing, create it
        todo_column = models.KanbanColumn(name="To Do", position=0, board_id=board.id)
        db.add(todo_column)
        db.commit()
        db.refresh(todo_column)

    # Sync new tasks and events
    existing_card_task_ids = {card.task_id for col in board.columns for card in col.cards if card.task_id}
    existing_card_event_ids = {card.event_id for col in board.columns for card in col.cards if card.event_id}

    new_tasks = db.query(Task).filter(
        Task.owner_id == user_id, 
        Task.completed == False, 
        ~Task.id.in_(existing_card_task_ids)
    ).all()

    new_events = db.query(Event).filter(
        Event.owner_id == user_id, 
        Event.completed == False, 
        ~Event.id.in_(existing_card_event_ids)
    ).all()

    card_position = len(todo_column.cards)
    needs_commit = False

    for task in new_tasks:
        new_card = models.KanbanCard(position=card_position, column_id=todo_column.id, task_id=task.id)
        db.add(new_card)
        card_position += 1
        needs_commit = True

    for event in new_events:
        new_card = models.KanbanCard(position=card_position, column_id=todo_column.id, event_id=event.id)
        db.add(new_card)
        card_position += 1
        needs_commit = True

    if needs_commit:
        db.commit()
        # Re-fetch the entire board to get the most up-to-date state with all relationships loaded
        board = db.query(models.KanbanBoard).options(
            joinedload(models.KanbanBoard.columns).joinedload(models.KanbanColumn.cards)
        ).filter(models.KanbanBoard.owner_id == user_id).first()

    return [board] if board else []

def move_card(db: Session, card_id: str, new_column_id: str, new_position: int):
    card = db.query(models.KanbanCard).filter(models.KanbanCard.id == card_id).first()
    if not card:
        return None

    # Logic to re-order other cards in the old and new columns might be needed here
    # For now, just update the card's column and position
    card.column_id = new_column_id
    card.position = new_position
    db.commit()
    db.refresh(card)
    return card

# More business logic will be added here

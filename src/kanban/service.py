from sqlalchemy.orm import Session
from . import models
from src.tasks.models import Task
from src.events.models import Event

def get_boards_by_user(db: Session, user_id: str):
    boards = db.query(models.KanbanBoard).filter(models.KanbanBoard.owner_id == user_id).all()

    if not boards:
        # Create a default board
        new_board = models.KanbanBoard(name="My Board", owner_id=user_id)
        db.add(new_board)
        db.flush() # Flush to get the new_board.id

        # Create default columns
        todo_column = models.KanbanColumn(name="To Do", position=0, board_id=new_board.id)
        inprogress_column = models.KanbanColumn(name="In Progress", position=1, board_id=new_board.id)
        done_column = models.KanbanColumn(name="Done", position=2, board_id=new_board.id)
        db.add_all([todo_column, inprogress_column, done_column])
        db.flush() # Flush to get column ids

        # Get user's existing tasks
        existing_tasks = db.query(Task).filter(Task.owner_id == user_id, Task.completed == False).all()
        card_position = 0
        for task in existing_tasks:
            new_card = models.KanbanCard(
                position=card_position,
                column_id=todo_column.id,
                task_id=task.id
            )
            db.add(new_card)
            card_position += 1

        # Get user's existing events
        existing_events = db.query(Event).filter(Event.owner_id == user_id, Event.completed == False).all()
        for event in existing_events:
            new_card = models.KanbanCard(
                position=card_position,
                column_id=todo_column.id,
                event_id=event.id
            )
            db.add(new_card)
            card_position += 1
        
        db.commit()
        db.refresh(new_board)
        return [new_board]

    return boards

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

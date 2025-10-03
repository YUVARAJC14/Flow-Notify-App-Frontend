from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from ..database.database import get_db
from ..security import get_current_user
from . import schemas, service
from src.auth.models import User

router = APIRouter(
    prefix="/kanban",
    tags=["kanban"]
)

@router.get("/boards", response_model=List[schemas.KanbanBoard])
def get_user_boards(db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    # Logic to be added in service.py
    return service.get_boards_by_user(db=db, user_id=current_user.id)

@router.patch("/cards/{card_id}/move", status_code=204)
def move_kanban_card(
    card_id: str,
    move_request: schemas.KanbanCardMoveRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # Basic authorization: check if the user owns the board this card belongs to.
    # A more thorough check would be to join tables and verify ownership.
    card = service.move_card(
        db=db, 
        card_id=card_id, 
        new_column_id=move_request.new_column_id, 
        new_position=move_request.position
    )
    if not card:
        raise HTTPException(status_code=404, detail="Card not found")
    return

# More endpoints will be added here

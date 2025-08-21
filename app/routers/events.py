from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session
from .. import crud, models
from ..schemas import schemas
from ..database.database import get_db
from ..security import get_current_user
from typing import List, Optional
from datetime import date

router = APIRouter(
    prefix="/events",
    tags=["events"]
)

@router.post("/", response_model=schemas.Event, status_code=status.HTTP_201_CREATED)
def create_event(
    event: schemas.EventCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    return crud.create_user_event(db=db, event=event, user_id=current_user.id)

@router.get("/", response_model=List[schemas.Event])
def read_events(
    start_date: Optional[date] = Query(None, alias="start_date"),
    end_date: Optional[date] = Query(None, alias="end_date"),
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    return crud.get_events(db=db, user_id=current_user.id, start_date=start_date, end_date=end_date)

@router.put("/{event_id}", response_model=schemas.Event)
def update_event(
    event_id: int,
    event: schemas.EventCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    db_event = crud.get_event_by_id(db=db, event_id=event_id, user_id=current_user.id)
    if db_event is None:
        raise HTTPException(status_code=404, detail="Event not found")
    return crud.update_event(db=db, event=db_event, event_update=event)

@router.delete("/{event_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_event(
    event_id: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    db_event = crud.get_event_by_id(db=db, event_id=event_id, user_id=current_user.id)
    if db_event is None:
        raise HTTPException(status_code=404, detail="Event not found")
    crud.delete_event(db=db, event=db_event)
    return
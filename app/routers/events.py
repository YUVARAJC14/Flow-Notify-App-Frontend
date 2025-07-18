from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from .. import crud, models, schemas
from ..database.database import SessionLocal
from ..routers.users import get_current_user
from typing import List, Optional
from datetime import datetime

router = APIRouter()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.post("/events/", response_model=schemas.Event)
def create_event(
    event: schemas.EventCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    return crud.create_user_event(db=db, event=event, user_id=current_user.id)

@router.get("/events/", response_model=List[schemas.Event])
def read_events(
    start_datetime: Optional[datetime] = Query(None),
    end_datetime: Optional[datetime] = Query(None),
    category: Optional[schemas.CategoryEnum] = Query(None),
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    return crud.get_events(db=db, user_id=current_user.id, start_datetime=start_datetime, end_datetime=end_datetime, category=category)

@router.get("/events/{event_id}", response_model=schemas.Event)
def read_event(
    event_id: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    db_event = crud.get_event_by_id(db=db, event_id=event_id, user_id=current_user.id)
    if db_event is None:
        raise HTTPException(status_code=404, detail="Event not found")
    return db_event

@router.put("/events/{event_id}", response_model=schemas.Event)
def update_event(
    event_id: int,
    event: schemas.EventCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    db_event = crud.get_event_by_id(db=db, event_id=event_id, user_id=current_user.id)
    if db_event is None:
        raise HTTPException(status_code=404, detail="Event not found")
    return crud.update_event(db=db, event=db_event, event_update=event)

@router.delete("/events/{event_id}", status_code=204)
def delete_event(
    event_id: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    db_event = crud.get_event_by_id(db=db, event_id=event_id, user_id=current_user.id)
    if db_event is None:
        raise HTTPException(status_code=404, detail="Event not found")
    crud.delete_event(db=db, event=db_event)
    return {"ok": True}

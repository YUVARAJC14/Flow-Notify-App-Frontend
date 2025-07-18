from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import crud, models, schemas
from ..database.database import SessionLocal
from ..routers.users import get_current_user
from typing import List
from datetime import date

router = APIRouter()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.get("/calendar/{year}/{month}", response_model=List[schemas.Event])
def get_calendar_month(
    year: int,
    month: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    return crud.get_events_by_month(db=db, user_id=current_user.id, year=year, month=month)

@router.get("/calendar/today", response_model=List[schemas.Event])
def get_todays_schedule(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    today = date.today()
    return crud.get_events_by_date(db=db, user_id=current_user.id, event_date=today)

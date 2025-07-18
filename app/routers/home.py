from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from datetime import date, timedelta
from .. import crud
from ..database.database import SessionLocal, engine
from ..models import models
from ..schemas import home as schemas_home
from ..routers.users import get_current_user

router = APIRouter()

models.Base.metadata.create_all(bind=engine)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.get("/home", response_model=schemas_home.HomePage)
def get_home_page(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    
    today = date.today()
    
    # Calculate Today's Flow
    total_tasks_today = crud.get_tasks_by_date(db, user_id=current_user.id, task_date=today)
    completed_tasks_today = [task for task in total_tasks_today if task.completed]
    todays_flow = (len(completed_tasks_today) / len(total_tasks_today)) * 100 if total_tasks_today else 0

    # Upcoming Tasks (next 7 days)
    upcoming_tasks = crud.get_tasks_due_in_days(db, user_id=current_user.id, days=7)

    # Today's Schedule
    todays_schedule = crud.get_events(db, user_id=current_user.id, start_datetime=today, end_datetime=today + timedelta(days=1))

    print(f"total_tasks_today: {total_tasks_today}")
    print(f"completed_tasks_today: {completed_tasks_today}")
    print(f"todays_flow: {todays_flow}")
    print(f"upcoming_tasks: {upcoming_tasks}")
    print(f"todays_schedule: {todays_schedule}")

    return {
        "user_name": current_user.username,
        "todays_flow": todays_flow,
        "upcoming_tasks": upcoming_tasks,
        "todays_schedule": todays_schedule
    }
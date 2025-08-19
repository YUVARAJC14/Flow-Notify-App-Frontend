from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import crud, models
from ..schemas import dashboard as schemas_dashboard
from ..database.database import get_db
from ..security import get_current_user

router = APIRouter(
    tags=["dashboard"],
    dependencies=[Depends(get_current_user)]
)

@router.get("/dashboard", response_model=schemas_dashboard.DashboardData)
def get_dashboard(db: Session = Depends(get_db), current_user: models.User = Depends(get_current_user)):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    
    todays_flow = crud.get_today_progress(db, user_id=current_user.id)
    upcoming_tasks = crud.get_upcoming_tasks(db, user_id=current_user.id)
    todays_schedule = crud.get_today_schedule(db, user_id=current_user.id)

    return {
        "greetingName": current_user.full_name,
        "todaysFlow": {
            "percentage": int(todays_flow),
            "message": "Great progress!"
        },
        "upcomingTasks": upcoming_tasks,
        "todaysSchedule": todays_schedule
    }

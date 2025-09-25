from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import crud
from src.auth import models as auth_models
from . import schemas as dashboard_schema
from ..database.database import get_db
from ..security import get_current_user

router = APIRouter(
    prefix="/dashboard",
    tags=["dashboard"]
)

@router.get("/summary", response_model=dashboard_schema.DashboardSummaryResponse)
def get_dashboard_summary(db: Session = Depends(get_db), current_user: auth_models.User = Depends(get_current_user)):
    """
    Fetches all aggregated data needed for the main dashboard screen.
    """
    # 1. Get Today's Flow
    todays_flow_percentage = crud.get_today_progress(db, user_id=current_user.id)
    flow_percentage = int(todays_flow_percentage)

    if flow_percentage >= 80:
        flow_message = "Great progress!"
    elif 50 <= flow_percentage <= 79:
        flow_message = "Keep going!"
    else:
        flow_message = "Stay focused!"

    flow_data = dashboard_schema.TodaysFlow(percentage=flow_percentage, message=flow_message)

    # 2. Get Upcoming Tasks
    upcoming_tasks_db = crud.get_upcoming_tasks(db, user_id=current_user.id, limit=3)
    upcoming_tasks_response = [
        dashboard_schema.UpcomingTask(
            id=str(task.id),
            title=task.title,
            time=task.due_time.strftime("%I:%M %p") if task.due_time else "",
            priority=task.priority.value if task.priority else ""
        ) for task in upcoming_tasks_db
    ]

    # 3. Get Today's Schedule
    todays_schedule_db = crud.get_today_schedule(db, user_id=current_user.id)
    todays_schedule_response = [
        dashboard_schema.ScheduleEvent(
            id=str(event.id),
            title=event.title,
            time=event.start_datetime.strftime("%I:%M %p"),
            location=event.location or "",
            category=event.category.value if event.category else ""
        ) for event in todays_schedule_db
    ]

    return dashboard_schema.DashboardSummaryResponse(
        todaysFlow=flow_data,
        upcomingTasks=upcoming_tasks_response,
        todaysSchedule=todays_schedule_response
    )
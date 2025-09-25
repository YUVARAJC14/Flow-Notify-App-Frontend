from sqlalchemy.orm import Session
from .. import crud
from src.tasks import models as task_models
from src.events import models as event_models
from datetime import date, timedelta, datetime

def _calculate_period_progress(db: Session, user_id: int, start_date: date, end_date: date) -> float:
    # Get tasks for the period
    total_tasks = db.query(task_models.Task).filter(
        task_models.Task.owner_id == user_id,
        task_models.Task.due_date >= start_date,
        task_models.Task.due_date <= end_date
    ).count()
    completed_tasks = db.query(task_models.Task).filter(
        task_models.Task.owner_id == user_id,
        task_models.Task.due_date >= start_date,
        task_models.Task.due_date <= end_date,
        task_models.Task.completed == True
    ).count()

    # Get events for the period
    start_datetime_period = datetime.combine(start_date, datetime.min.time())
    end_datetime_period = datetime.combine(end_date, datetime.max.time())

    total_events = db.query(event_models.Event).filter(
        event_models.Event.owner_id == user_id,
        event_models.Event.start_datetime >= start_datetime_period,
        event_models.Event.end_datetime <= end_datetime_period
    ).count()
    # Events are considered 'completed' if their end_datetime has passed
    completed_events = db.query(event_models.Event).filter(
        event_models.Event.owner_id == user_id,
        event_models.Event.start_datetime >= start_datetime_period,
        event_models.Event.end_datetime <= end_datetime_period,
        event_models.Event.end_datetime < datetime.now() # Completed if end time is in the past
    ).count()

    total_items = total_tasks + total_events
    completed_items = completed_tasks + completed_events

    if total_items == 0:
        return 0.0
    return (completed_items / total_items) * 100

def get_insights(db: Session, user_id: int, period: str):
    today = date.today()
    if period == "week":
        start_date = today - timedelta(days=today.weekday())
        end_date = start_date + timedelta(days=6)
        last_period_start_date = start_date - timedelta(days=7)
        last_period_end_date = end_date - timedelta(days=7)
    elif period == "month":
        start_date = today.replace(day=1)
        end_date = (start_date + timedelta(days=32)).replace(day=1) - timedelta(days=1)
        last_month = start_date - timedelta(days=1)
        last_period_start_date = last_month.replace(day=1)
        last_period_end_date = (last_period_start_date + timedelta(days=32)).replace(day=1) - timedelta(days=1)
    else: # year
        start_date = today.replace(month=1, day=1)
        end_date = today.replace(month=12, day=31)
        last_period_start_date = start_date.replace(year=start_date.year - 1)
        last_period_end_date = end_date.replace(year=end_date.year - 1)

    # Flow Score
    current_flow = _calculate_period_progress(db, user_id, start_date, end_date)
    last_period_flow = _calculate_period_progress(db, user_id, last_period_start_date, last_period_end_date)
    flow_change = current_flow - last_period_flow

    # Task Completion
    task_completion = []
    if period == "week":
        for i in range(7):
            day = start_date + timedelta(days=i)
            tasks = crud.get_tasks_by_date(db, user_id, day)
            completed = sum(1 for task in tasks if task.completed)
            task_completion.append({"label": day.strftime("%a"), "completed": completed, "total": len(tasks)})
    else: # month or year
        # Simplified for brevity
        pass

    # Productive Times
    productive_times = []
    completed_tasks = crud.get_completed_tasks_by_date_range(db, user_id, start_date, end_date)
    for task in completed_tasks:
        if task.completed_at:
            productive_times.append({"day": task.completed_at.weekday(), "hour": task.completed_at.hour, "intensity": 0.5})

    return {
        "flowScore": {"score": int(current_flow), "comparison": {"change": int(flow_change), "period": f"last {period}"}},
        "taskCompletion": task_completion,
        "productiveTimes": productive_times
    }

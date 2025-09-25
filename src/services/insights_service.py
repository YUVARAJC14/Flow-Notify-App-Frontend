from sqlalchemy.orm import Session
from .. import crud
from datetime import date, timedelta, datetime

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
    current_tasks = crud.get_tasks_by_date_range(db, user_id, start_date, end_date)
    last_period_tasks = crud.get_tasks_by_date_range(db, user_id, last_period_start_date, last_period_end_date)
    current_completed = sum(1 for task in current_tasks if task.completed)
    last_period_completed = sum(1 for task in last_period_tasks if task.completed)
    current_flow = (current_completed / len(current_tasks)) * 100 if current_tasks else 0
    last_period_flow = (last_period_completed / len(last_period_tasks)) * 100 if last_period_tasks else 0
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

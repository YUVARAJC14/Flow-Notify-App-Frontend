from sqlalchemy.orm import Session
from sqlalchemy import and_, func
from . import models, schemas
from .security import get_password_hash
from datetime import date, timedelta, datetime

def get_user_by_username(db: Session, username: str):
    return db.query(models.User).filter(models.User.username == username).first()

def create_user(db: Session, user: schemas.UserCreate):
    hashed_password = get_password_hash(user.password)
    db_user = models.User(username=user.username, hashed_password=hashed_password)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

def create_user_task(db: Session, task: schemas.TaskCreate, user_id: int):
    db_task = models.Task(**task.model_dump(), owner_id=user_id)
    db.add(db_task)
    db.commit()
    db.refresh(db_task)
    return db_task

def get_tasks(db: Session, user_id: int, search: str = None, date_filter: str = "all"):
    query = db.query(models.Task).filter(models.Task.owner_id == user_id)

    if search:
        query = query.filter(models.Task.title.contains(search) | models.Task.description.contains(search))

    today = date.today()

    if date_filter == "today":
        query = query.filter(models.Task.due_date == today)
    elif date_filter == "upcoming":
        query = query.filter(models.Task.due_date > today, models.Task.completed == False)
    elif date_filter == "completed":
        query = query.filter(models.Task.completed == True)

    return query.order_by(models.Task.due_date).all()

def get_task_by_id(db: Session, task_id: int, user_id: int):
    return db.query(models.Task).filter(and_(models.Task.id == task_id, models.Task.owner_id == user_id)).first()

def update_task(db: Session, task: models.Task, task_update: schemas.TaskCreate):
    task_data = task_update.model_dump(exclude_unset=True)
    for key, value in task_data.items():
        setattr(task, key, value)
    if "completed" in task_data and task_data["completed"] and task.completed_at is None:
        task.completed_at = datetime.now()
    elif "completed" in task_data and not task_data["completed"] and task.completed_at is not None:
        task.completed_at = None
    db.add(task)
    db.commit()
    db.refresh(task)
    return task

def delete_task(db: Session, task: models.Task):
    db.delete(task)
    db.commit()

def get_tasks_by_date_range(db: Session, user_id: int, start_date: date, end_date: date):
    return db.query(models.Task).filter(
        models.Task.owner_id == user_id,
        models.Task.due_date >= start_date,
        models.Task.due_date <= end_date
    ).all()

def get_completed_tasks_by_date_range(db: Session, user_id: int, start_date: date, end_date: date):
    return db.query(models.Task).filter(
        models.Task.owner_id == user_id,
        models.Task.completed == True,
        models.Task.completed_at >= start_date,
        models.Task.completed_at <= end_date
    ).all()

def get_today_progress(db: Session, user_id: int) -> float:
    today = date.today()
    total_tasks_today = db.query(models.Task).filter(
        models.Task.owner_id == user_id,
        models.Task.due_date == today
    ).count()
    completed_tasks_today = db.query(models.Task).filter(
        models.Task.owner_id == user_id,
        models.Task.due_date == today,
        models.Task.completed == True
    ).count()

    if total_tasks_today == 0:
        return 100.0  # If no tasks, consider 100% complete
    return (completed_tasks_today / total_tasks_today) * 100

def get_upcoming_tasks(db: Session, user_id: int):
    today = date.today()
    return db.query(models.Task).filter(
        models.Task.owner_id == user_id,
        models.Task.due_date > today,
        models.Task.completed == False
    ).order_by(models.Task.due_date).all()

def get_today_schedule(db: Session, user_id: int):
    today = date.today()
    return db.query(models.Task).filter(
        models.Task.owner_id == user_id,
        models.Task.due_date == today
    ).order_by(models.Task.due_time).all()

def get_tasks_by_date(db: Session, user_id: int, task_date: date):
    return db.query(models.Task).filter(models.Task.owner_id == user_id, models.Task.due_date == task_date).all()

def get_tasks_due_in_days(db: Session, user_id: int, days: int):
    today = date.today()
    end_date = today + timedelta(days=days)
    return db.query(models.Task).filter(
        models.Task.owner_id == user_id,
        models.Task.due_date >= today,
        models.Task.due_date < end_date
    ).all()

def create_user_event(db: Session, event: schemas.EventCreate, user_id: int):
    db_event = models.Event(**event.model_dump(), owner_id=user_id)
    db.add(db_event)
    db.commit()
    db.refresh(db_event)
    return db_event

def get_events(db: Session, user_id: int, start_datetime: datetime = None, end_datetime: datetime = None, category: schemas.CategoryEnum = None):
    query = db.query(models.Event).filter(models.Event.owner_id == user_id)
    if start_datetime:
        query = query.filter(models.Event.start_datetime >= start_datetime)
    if end_datetime:
        query = query.filter(models.Event.end_datetime <= end_datetime)
    if category:
        query = query.filter(models.Event.category == category)
    return query.all()

def get_event_by_id(db: Session, event_id: int, user_id: int):
    return db.query(models.Event).filter(and_(models.Event.id == event_id, models.Event.owner_id == user_id)).first()

def update_event(db: Session, event: models.Event, event_update: schemas.EventCreate):
    event_data = event_update.model_dump(exclude_unset=True)
    for key, value in event_data.items():
        setattr(event, key, value)
    db.add(event)
    db.commit()
    db.refresh(event)
    return event

def delete_event(db: Session, event: models.Event):
    db.delete(event)
    db.commit()

def get_events_by_datetime_range(db: Session, user_id: int, start_datetime: datetime, end_datetime: datetime):
    return db.query(models.Event).filter(
        models.Event.owner_id == user_id,
        models.Event.start_datetime >= start_datetime,
        models.Event.start_datetime <= end_datetime
    ).all()

def get_events_by_month(db: Session, user_id: int, year: int, month: int):
    start_date = date(year, month, 1)
    if month == 12:
        end_date = date(year + 1, 1, 1)
    else:
        end_date = date(year, month + 1, 1)
    
    start_datetime = datetime.combine(start_date, datetime.min.time())
    end_datetime = datetime.combine(end_date, datetime.min.time())

    return db.query(models.Event).filter(
        models.Event.owner_id == user_id,
        models.Event.start_datetime >= start_datetime,
        models.Event.start_datetime < end_datetime
    ).all()

def get_events_by_date(db: Session, user_id: int, event_date: date):
    start_datetime = datetime.combine(event_date, datetime.min.time())
    end_datetime = datetime.combine(event_date, datetime.max.time())
    return db.query(models.Event).filter(
        models.Event.owner_id == user_id,
        models.Event.start_datetime >= start_datetime,
        models.Event.start_datetime <= end_datetime
    ).order_by(models.Event.start_datetime).all()

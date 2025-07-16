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
    db.add(task)
    db.commit()
    db.refresh(task)
    return task

def delete_task(db: Session, task: models.Task):
    db.delete(task)
    db.commit()

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

def get_events_by_date(db: Session, user_id: int, event_date: date):
    return db.query(models.Event).filter(models.Event.owner_id == user_id, models.Event.date == event_date).all()

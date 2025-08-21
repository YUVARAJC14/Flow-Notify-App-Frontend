from sqlalchemy.orm import Session
from sqlalchemy import and_, func
from .models import models
from .schemas import schemas as schemas_all
from .auth_utils import get_password_hash
from datetime import date, timedelta, datetime
from typing import Optional

def get_user_by_email(db: Session, email: str):
    return db.query(models.User).filter(models.User.email == email).first()

def create_password_reset_token(db: Session, user: models.User):
    # This should be implemented with a secure token generation and storage mechanism
    # For now, returning a dummy token
    return {"token": "dummy_reset_token"}

def get_user_by_reset_token(db: Session, token: str):
    # This should be implemented with a secure token verification mechanism
    # For now, returning a dummy user
    return db.query(models.User).first()

def get_user_by_email_or_username(db: Session, identifier: str):
    # Try to find by email first
    user = db.query(models.User).filter(models.User.email == identifier).first()
    if user:
        return user
    # If not found by email, try to find by full_name (if applicable, though email is preferred for login)
    # Note: For a real application, you might want to enforce unique full_name if using it for login
    # or stick strictly to email for login.
    user = db.query(models.User).filter(models.User.full_name == identifier).first()
    return user

def add_token_to_blocklist(db: Session, jti: str):
    db_token = models.TokenBlocklist(jti=jti)
    db.add(db_token)
    db.commit()
    db.refresh(db_token)
    return db_token

def is_token_blocklisted(db: Session, jti: str):
    return db.query(models.TokenBlocklist).filter(models.TokenBlocklist.jti == jti).first() is not None


def create_user(db: Session, user: schemas_all.UserCreate):
    hashed_password = get_password_hash(user.password)
    db_user = models.User(full_name=user.fullName, email=user.email, hashed_password=hashed_password)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

def create_user_task(db: Session, task: schemas_all.TaskCreate, user_id: int):
    # Convert string dates and times to Python objects
    due_date_obj = datetime.strptime(task.dueDate, '%Y-%m-%d').date()
    due_time_obj = datetime.strptime(task.dueTime, '%H:%M').time()

    # Convert priority string to PriorityEnum
    priority_enum = models.PriorityEnum[task.priority.lower()]

    # Handle reminders (e.g., join them into a single string or take the first one)
    # The current database model only supports one reminder, so we'll take the first if it exists.
    reminder_enum = None
    if task.reminders:
        try:
            # Assuming reminders are like "10m", "1h", "1d"
            reminder_map = {"10m": "ten_minutes", "1h": "one_hour", "1d": "one_day"}
            reminder_key = reminder_map.get(task.reminders[0])
            if reminder_key:
                reminder_enum = models.ReminderEnum[reminder_key]
        except (ValueError, KeyError):
            reminder_enum = None # Or handle error appropriately

    db_task = models.Task(
        title=task.title,
        description=task.description,
        due_date=due_date_obj,
        due_time=due_time_obj,
        priority=priority_enum,
        reminder=reminder_enum,
        owner_id=user_id
    )
    db.add(db_task)
    db.commit()
    db.refresh(db_task)
    return db_task

def get_tasks(db: Session, user_id: int, search: str = None, date_filter: str = "all", parent_id: Optional[int] = None):
    query = db.query(models.Task).filter(models.Task.owner_id == user_id)

    if parent_id is not None:
        query = query.filter(models.Task.parent_id == parent_id)
    else:
        query = query.filter(models.Task.parent_id == None) # Only fetch top-level tasks by default

    if search:
        query = query.filter(models.Task.title.contains(search) | models.Task.description.contains(search))

    today = date.today()

    if date_filter == "today":
        query = query.filter(models.Task.due_date == today)
    elif date_filter == "upcoming":
        query = query.filter(models.Task.due_date > today, models.Task.completed == False)
    elif date_filter == "completed":
        query = query.filter(models.Task.completed == True)

    tasks = query.order_by(models.Task.due_date).all()
    
    today = date.today()
    tomorrow = today + timedelta(days=1)
    
    today_tasks = [task for task in tasks if task.due_date == today]
    tomorrow_tasks = [task for task in tasks if task.due_date == tomorrow]

    return {"today": today_tasks, "tomorrow": tomorrow_tasks}

def get_task_by_id(db: Session, task_id: int, user_id: int):
    return db.query(models.Task).filter(and_(models.Task.id == task_id, models.Task.owner_id == user_id)).first()

def update_task(db: Session, task: models.Task, task_update: schemas_all.TaskCreate):
    task_data = task_update.model_dump(exclude_unset=True)
    for key, value in task_data.items():
        setattr(task, key, value)
    
    # Handle completed_at timestamp
    if "completed" in task_data:
        if task_data["completed"] and task.completed_at is None:
            task.completed_at = datetime.now()
        elif not task_data["completed"] and task.completed_at is not None:
            task.completed_at = None

    db.add(task)
    db.commit()
    db.refresh(task)

    # Logic to update parent task status based on sub-task completion
    if task.parent_id:
        parent_task = db.query(models.Task).filter(models.Task.id == task.parent_id).first()
        if parent_task:
            subtasks = db.query(models.Task).filter(models.Task.parent_id == parent_task.id).all()
            all_subtasks_completed = all(subtask.completed for subtask in subtasks)
            if all_subtasks_completed and not parent_task.completed:
                parent_task.completed = True
                parent_task.completed_at = datetime.now()
                db.add(parent_task)
                db.commit()
                db.refresh(parent_task)
            elif not all_subtasks_completed and parent_task.completed:
                parent_task.completed = False
                parent_task.completed_at = None
                db.add(parent_task)
                db.commit()
                db.refresh(parent_task)

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

def get_upcoming_tasks(db: Session, user_id: int, limit: int = 3):
    today = date.today()
    return db.query(models.Task).filter(
        models.Task.owner_id == user_id,
        models.Task.due_date >= today,
        models.Task.completed == False
    ).order_by(models.Task.due_date, models.Task.due_time).limit(limit).all()

def get_today_schedule(db: Session, user_id: int):
    today = date.today()
    start_of_day = datetime.combine(today, datetime.min.time())
    end_of_day = datetime.combine(today, datetime.max.time())
    return db.query(models.Event).filter(
        models.Event.owner_id == user_id,
        models.Event.start_datetime >= start_of_day,
        models.Event.start_datetime <= end_of_day
    ).order_by(models.Event.start_datetime).all()

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

def create_user_event(db: Session, event: schemas_all.EventCreate, user_id: int):
    db_event = models.Event(
        title=event.title,
        location=event.location,
        start_datetime=event.start_datetime,
        end_datetime=event.end_datetime,
        category=event.category,
        notes=event.notes,
        reminder_minutes_before=event.reminder_minutes_before,
        recurrence_rule=event.recurrence_rule,
        recurrence_end_date=event.recurrence_end_date,
        owner_id=user_id
    )
    db.add(db_event)
    db.commit()
    db.refresh(db_event)
    return db_event

def get_events(db: Session, user_id: int, start_datetime: datetime = None, end_datetime: datetime = None, category: schemas_all.CategoryEnum = None):
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

def update_event(db: Session, event: models.Event, event_update: schemas_all.EventCreate):
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

def update_user_profile(db: Session, user: models.User, profile_update: schemas_all.UserProfileUpdate):
    for key, value in profile_update.model_dump(exclude_unset=True).items():
        setattr(user, key, value)
    db.commit()
    db.refresh(user)
    return user

def update_user_password(db: Session, user: models.User, hashed_password: str):
    user.hashed_password = hashed_password
    db.commit()
    db.refresh(user)
    return user

def update_user_settings(db: Session, user: models.User, settings_update: schemas_all.AppSettingsUpdate):
    for key, value in settings_update.model_dump(exclude_unset=True).items():
        setattr(user, key, value)
    db.commit()
    db.refresh(user)
    return user

def update_app_settings(db: Session, user: models.User, settings_update: schemas_all.AppSettingsUpdate):
    for key, value in settings_update.model_dump(exclude_unset=True).items():
        setattr(user, key, value)
    db.commit()
    db.refresh(user)
    return user

def update_notification_settings(db: Session, user: models.User, notification_update: schemas_all.NotificationSettingsUpdate):
    for key, value in notification_update.model_dump(exclude_unset=True).items():
        setattr(user, key, value)
    db.commit()
    db.refresh(user)
    return user

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
from sqlalchemy.orm import Session
from sqlalchemy import and_, func
from src.auth import models as auth_models
from src.tasks import models as task_models
from src.events import models as event_models
from .schemas import schemas as schemas_all
from .auth_utils import get_password_hash
from datetime import date, timedelta, datetime
from typing import Optional

def get_user_by_email(db: Session, email: str):
    return db.query(auth_models.User).filter(auth_models.User.email == email).first()

def create_password_reset_token(db: Session, user: auth_models.User):
    # This should be implemented with a secure token generation and storage mechanism
    # For now, returning a dummy token
    return {"token": "dummy_reset_token"}

def get_user_by_reset_token(db: Session, token: str):
    # This should be implemented with a secure token verification mechanism
    # For now, returning a dummy user
    return db.query(auth_models.User).first()

def get_user_by_email_or_username(db: Session, identifier: str):
    # Try to find by email first
    user = db.query(auth_models.User).filter(auth_models.User.email == identifier).first()
    if user:
        return user
    # If not found by email, try to find by full_name (if applicable, though email is preferred for login)
    # Note: For a real application, you might want to enforce unique full_name if using it for login
    # or stick strictly to email for login.
    user = db.query(auth_models.User).filter(auth_models.User.full_name == identifier).first()
    return user

def add_token_to_blocklist(db: Session, jti: str):
    db_token = auth_models.TokenBlocklist(jti=jti)
    db.add(db_token)
    db.commit()
    db.refresh(db_token)
    return db_token

def is_token_blocklisted(db: Session, jti: str):
    return db.query(auth_models.TokenBlocklist).filter(auth_models.TokenBlocklist.jti == jti).first() is not None


def create_user(db: Session, user: schemas_all.UserCreate):
    hashed_password = get_password_hash(user.password)
    db_user = auth_models.User(full_name=user.fullName, email=user.email, hashed_password=hashed_password)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

def create_user_task(db: Session, task: schemas_all.TaskCreate, user_id: int):
    # Convert string dates and times to Python objects
    due_date_obj = datetime.strptime(task.dueDate, '%Y-%m-%d').date()
    due_time_obj = datetime.strptime(task.dueTime, '%H:%M').time()

    # Convert priority string to PriorityEnum
    priority_enum = task_models.PriorityEnum[task.priority.lower()]

    # Handle reminders (e.g., join them into a single string or take the first one)
    # The current database model only supports one reminder, so we'll take the first if it exists.
    reminder_enum = None
    if task.reminders:
        try:
            # Assuming reminders are like "10m", "1h", "1d"
            reminder_map = {"10m": "ten_minutes", "1h": "one_hour", "1d": "one_day"}
            reminder_key = reminder_map.get(task.reminders[0])
            if reminder_key:
                reminder_enum = task_models.ReminderEnum[reminder_key]
        except (ValueError, KeyError):
            reminder_enum = None # Or handle error appropriately

    db_task = task_models.Task(
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
    query = db.query(task_models.Task).filter(task_models.Task.owner_id == user_id)

    if parent_id is not None:
        query = query.filter(task_models.Task.parent_id == parent_id)
    else:
        query = query.filter(task_models.Task.parent_id == None)

    if search:
        query = query.filter(task_models.Task.title.contains(search) | task_models.Task.description.contains(search))

    today = date.today()

    if date_filter == "today":
        query = query.filter(task_models.Task.due_date == today)
    elif date_filter == "upcoming":
        query = query.filter(task_models.Task.due_date > today, task_models.Task.completed == False)
    elif date_filter == "completed":
        query = query.filter(task_models.Task.completed == True)

    tasks = query.order_by(task_models.Task.due_date).all()
    
    today = date.today()
    tomorrow = today + timedelta(days=1)

    today_tasks = [task for task in tasks if task.due_date == today]
    tomorrow_tasks = [task for task in tasks if task.due_date == tomorrow]
    upcoming_tasks = [task for task in tasks if task.due_date > tomorrow]

    return {"today": today_tasks, "tomorrow": tomorrow_tasks, "upcoming": upcoming_tasks}

def get_task_by_id(db: Session, task_id: int, user_id: int):
    return db.query(task_models.Task).filter(and_(task_models.Task.id == task_id, task_models.Task.owner_id == user_id)).first()

def update_task(db: Session, task: task_models.Task, task_update: schemas_all.TaskCreate):
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
        parent_task = db.query(task_models.Task).filter(task_models.Task.id == task.parent_id).first()
        if parent_task:
            subtasks = db.query(task_models.Task).filter(task_models.Task.parent_id == parent_task.id).all()
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

def delete_task(db: Session, task: task_models.Task):
    db.delete(task)
    db.commit()

def get_tasks_by_date_range(db: Session, user_id: int, start_date: date, end_date: date):
    return db.query(task_models.Task).filter(
        task_models.Task.owner_id == user_id,
        task_models.Task.due_date >= start_date,
        task_models.Task.due_date <= end_date
    ).all()

def get_completed_tasks_by_date_range(db: Session, user_id: int, start_date: date, end_date: date):
    return db.query(task_models.Task).filter(
        task_models.Task.owner_id == user_id,
        task_models.Task.completed == True,
        task_models.Task.completed_at >= start_date,
        task_models.Task.completed_at <= end_date
    ).all()

def get_today_progress(db: Session, user_id: int) -> float:
    today = date.today()

    # Get tasks for today
    total_tasks_today = db.query(task_models.Task).filter(
        task_models.Task.owner_id == user_id,
        task_models.Task.due_date == today
    ).count()
    completed_tasks_today = db.query(task_models.Task).filter(
        task_models.Task.owner_id == user_id,
        task_models.Task.due_date == today,
        task_models.Task.completed == True
    ).count()

    # Get events for today
    start_of_day = datetime.combine(today, datetime.min.time())
    end_of_day = datetime.combine(today, datetime.max.time())
    total_events_today = db.query(event_models.Event).filter(
        event_models.Event.owner_id == user_id,
        event_models.Event.start_datetime >= start_of_day,
        event_models.Event.start_datetime <= end_of_day
    ).count()
    # Assuming events are implicitly 'completed' once they happen, we can count all of them.
    # If events have a completion status, this would need to be adjusted.
    completed_events_today = total_events_today

    total_items = total_tasks_today + total_events_today
    completed_items = completed_tasks_today + completed_events_today

    if total_items == 0:
        return 0.0
    return (completed_items / total_items) * 100

def get_upcoming_tasks(db: Session, user_id: int, limit: int = 3):
    today = date.today()
    return db.query(task_models.Task).filter(
        task_models.Task.owner_id == user_id,
        task_models.Task.due_date >= today,
        task_models.Task.completed == False
    ).order_by(task_models.Task.due_date, task_models.Task.due_time).limit(limit).all()

def get_today_schedule(db: Session, user_id: int):
    today = date.today()
    start_of_day = datetime.combine(today, datetime.min.time())
    end_of_day = datetime.combine(today, datetime.max.time())
    return db.query(event_models.Event).filter(
        event_models.Event.owner_id == user_id,
        event_models.Event.start_datetime >= start_of_day,
        event_models.Event.start_datetime <= end_of_day
    ).order_by(event_models.Event.start_datetime).all()

def get_tasks_by_date(db: Session, user_id: int, task_date: date):
    return db.query(task_models.Task).filter(task_models.Task.owner_id == user_id, task_models.Task.due_date == task_date).all()

def get_tasks_due_in_days(db: Session, user_id: int, days: int):
    today = date.today()
    end_date = today + timedelta(days=days)
    return db.query(task_models.Task).filter(
        task_models.Task.owner_id == user_id,
        task_models.Task.due_date >= today,
        task_models.Task.due_date < end_date
    ).all()

def create_user_event(db: Session, event: schemas_all.EventCreate, user_id: int):
    db_event = event_models.Event(
        title=event.title,
        location=event.location,
        start_datetime=event.start_datetime,
        end_datetime=event.end_datetime,
        category=event.category.value,
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

def get_events(db: Session, user_id: int, start_date: date = None, end_date: date = None, category: schemas_all.CategoryEnum = None):
    query = db.query(event_models.Event).filter(event_models.Event.owner_id == user_id)
    if start_date:
        start_datetime_query = datetime.combine(start_date, datetime.min.time())
        query = query.filter(event_models.Event.end_datetime >= start_datetime_query) # Event must end after or on query start
    if end_date:
        end_datetime_query = datetime.combine(end_date, datetime.max.time())
        query = query.filter(event_models.Event.start_datetime <= end_datetime_query) # Event must start before or on query end
    if category:
        query = query.filter(event_models.Event.category == category)
    return query.all()

def get_event_by_id(db: Session, event_id: int, user_id: int):
    return db.query(event_models.Event).filter(and_(event_models.Event.id == event_id, event_models.Event.owner_id == user_id)).first()

def update_event(db: Session, event: event_models.Event, event_update: schemas_all.EventUpdate):
    event_data = event_update.model_dump(exclude_unset=True)
    for key, value in event_data.items():
        setattr(event, key, value)

    db.add(event)
    db.commit()
    db.refresh(event)
    return event

def delete_event(db: Session, event: event_models.Event):
    db.delete(event)
    db.commit()

def update_user_profile(db: Session, user: auth_models.User, profile_update: schemas_all.UserProfileUpdate):
    for key, value in profile_update.model_dump(exclude_unset=True).items():
        setattr(user, key, value)
    db.commit()
    db.refresh(user)
    return user

def update_user_password(db: Session, user: auth_models.User, hashed_password: str):
    user.hashed_password = hashed_password
    db.commit()
    db.refresh(user)
    return user

def update_user_settings(db: Session, user: auth_models.User, settings_update: schemas_all.AppSettingsUpdate):
    for key, value in settings_update.model_dump(exclude_unset=True).items():
        setattr(user, key, value)
    db.commit()
    db.refresh(user)
    return user

def update_app_settings(db: Session, user: auth_models.User, settings_update: schemas_all.AppSettingsUpdate):
    for key, value in settings_update.model_dump(exclude_unset=True).items():
        setattr(user, key, value)
    db.commit()
    db.refresh(user)
    return user

def update_notification_settings(db: Session, user: auth_models.User, notification_update: schemas_all.NotificationSettingsUpdate):
    for key, value in notification_update.model_dump(exclude_unset=True).items():
        setattr(user, key, value)
    db.commit()
    db.refresh(user)
    return user

def get_events_by_datetime_range(db: Session, user_id: int, start_datetime: datetime, end_datetime: datetime):
    return db.query(event_models.Event).filter(
        event_models.Event.owner_id == user_id,
        event_models.Event.start_datetime >= start_datetime,
        event_models.Event.start_datetime <= end_datetime
    ).all()

def get_events_by_month(db: Session, user_id: int, year: int, month: int):
    start_date = date(year, month, 1)
    if month == 12:
        end_date = date(year + 1, 1, 1)
    else:
        end_date = date(year, month + 1, 1)
    
    start_datetime = datetime.combine(start_date, datetime.min.time())
    end_datetime = datetime.combine(end_date, datetime.min.time())

    return db.query(event_models.Event).filter(
        event_models.Event.owner_id == user_id,
        event_models.Event.start_datetime >= start_datetime,
        event_models.Event.start_datetime < end_datetime
    ).all()

def get_events_by_date(db: Session, user_id: int, event_date: date):
    start_datetime = datetime.combine(event_date, datetime.min.time())
    end_datetime = datetime.combine(event_date, datetime.max.time())
    return db.query(event_models.Event).filter(
        event_models.Event.owner_id == user_id,
        event_models.Event.start_datetime >= start_datetime,
        event_models.Event.start_datetime <= end_datetime
    ).order_by(event_models.Event.start_datetime).all()

def update_user_name(db: Session, user: auth_models.User, name: str):
    user.full_name = name
    db.commit()
    db.refresh(user)
    return user
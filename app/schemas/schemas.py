from pydantic import BaseModel, ConfigDict
from datetime import date, time, datetime
from typing import Optional
from enum import Enum

class UserBase(BaseModel):
    username: str

class UserCreate(UserBase):
    password: str

class User(UserBase):
    id: int
    email: Optional[str] = None
    profile_picture_url: Optional[str] = None
    theme: Optional[str] = None
    language: Optional[str] = None
    email_notifications_enabled: Optional[bool] = None
    push_notifications_enabled: Optional[bool] = None
    model_config = ConfigDict(from_attributes=True)

class UserProfileUpdate(BaseModel):
    username: Optional[str] = None
    email: Optional[str] = None
    profile_picture_url: Optional[str] = None

class PasswordChange(BaseModel):
    current_password: str
    new_password: str

class AppSettingsUpdate(BaseModel):
    theme: Optional[str] = None
    language: Optional[str] = None

class NotificationSettingsUpdate(BaseModel):
    email_notifications_enabled: Optional[bool] = None
    push_notifications_enabled: Optional[bool] = None

class Feedback(BaseModel):
    message: str

class PriorityEnum(str, Enum):
    high = "High"
    medium = "Medium"
    low = "Low"

class ReminderEnum(str, Enum):
    ten_minutes = "10 minutes before"
    one_hour = "1 hour before"
    one_day = "1 day before"

class CategoryEnum(str, Enum):
    work = "Work"
    personal = "Personal"
    social = "Social"
    other = "Other"

class TaskBase(BaseModel):
    title: str
    description: Optional[str] = None
    due_date: date
    due_time: time
    priority: PriorityEnum
    reminder: ReminderEnum
    completed: bool = False

class TaskCreate(TaskBase):
    pass

class Task(TaskBase):
    id: int
    owner_id: int
    model_config = ConfigDict(from_attributes=True)

class EventBase(BaseModel):
    title: str
    location: Optional[str] = None
    start_datetime: datetime
    end_datetime: datetime
    category: CategoryEnum
    notes: Optional[str] = None
    reminder_minutes_before: Optional[int] = None

class EventCreate(EventBase):
    pass

class Event(EventBase):
    id: int
    owner_id: int
    model_config = ConfigDict(from_attributes=True)

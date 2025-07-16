from pydantic import BaseModel
from datetime import date, time
from typing import Optional
from enum import Enum

class UserBase(BaseModel):
    username: str

class UserCreate(UserBase):
    password: str

class User(UserBase):
    id: int

    class Config:
        from_attributes = True

class PriorityEnum(str, Enum):
    high = "High"
    medium = "Medium"
    low = "Low"

class ReminderEnum(str, Enum):
    ten_minutes = "10 minutes before"
    one_hour = "1 hour before"
    one_day = "1 day before"

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

    class Config:
        orm_mode = True

class EventBase(BaseModel):
    title: str
    description: str | None = None
    date: date
    time: time

class EventCreate(EventBase):
    pass

class Event(EventBase):
    id: int
    owner_id: int

    class Config:
        orm_mode = True

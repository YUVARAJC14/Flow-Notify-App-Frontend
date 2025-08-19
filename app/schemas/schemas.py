from pydantic import BaseModel, ConfigDict, EmailStr
from datetime import date, time, datetime
from typing import Optional, List
from enum import Enum

class UserCreate(BaseModel):
    fullName: str
    email: str
    password: str

class UserBase(BaseModel):
    email: str

class User(UserBase):
    id: int
    profile_picture_url: Optional[str] = None
    theme: Optional[str] = None
    language: Optional[str] = None
    email_notifications_enabled: Optional[bool] = None
    push_notifications_enabled: Optional[bool] = None
    model_config = ConfigDict(from_attributes=True)

class LoginRequest(BaseModel):
    email: str
    password: str

class Token(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str

class LoginResponse(BaseModel):
    access_token: str
    user: User
    token_type: str


class UserProfileUpdate(BaseModel):
    username: Optional[str] = None
    email: Optional[str] = None
    profile_picture_url: Optional[str] = None

class UserNameUpdate(BaseModel):
    name: str

class PasswordChange(BaseModel):
    current_password: str
    new_password: str

class AppSettingsUpdate(BaseModel):
    theme: Optional[str] = None
    language: Optional[str] = None

class NotificationSettingsUpdate(BaseModel):
    email_notifications_enabled: Optional[bool] = None
    push_notifications_enabled: Optional[bool] = None

class ForgotPasswordSchema(BaseModel):
    email: str

class ResendVerificationSchema(BaseModel):
    email: EmailStr

class VerifyEmailSchema(BaseModel):
    token: str

class ResetPasswordSchema(BaseModel):
    token: str
    new_password: str

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
    due_date: Optional[date] = None
    due_time: Optional[time] = None
    priority: Optional[PriorityEnum] = None
    reminder: Optional[ReminderEnum] = None
    completed: bool = False
    recurrence_rule: Optional[str] = None
    recurrence_end_date: Optional[date] = None
    goal_id: Optional[int] = None
    parent_id: Optional[int] = None

class TaskCreate(BaseModel):
    title: str
    description: Optional[str] = None
    due_date: Optional[date] = None
    due_time: Optional[time] = None
    priority: Optional[PriorityEnum] = PriorityEnum.medium
    reminder: Optional[ReminderEnum] = None
    completed: bool = False
    recurrence_rule: Optional[str] = None
    recurrence_end_date: Optional[date] = None
    goal_id: Optional[int] = None
    parent_id: Optional[int] = None

class Task(TaskBase):
    id: int
    owner_id: int
    subtasks: List['Task'] = [] # For hierarchical tasks
    model_config = ConfigDict(from_attributes=True)

class TaskUpdate(BaseModel):
    completed: bool


class TaskPartialUpdate(BaseModel):
    title: Optional[str] = None
    description: Optional[str] = None
    due_date: Optional[date] = None
    due_time: Optional[time] = None
    priority: Optional[PriorityEnum] = None
    reminder: Optional[ReminderEnum] = None
    completed: Optional[bool] = None
    recurrence_rule: Optional[str] = None
    recurrence_end_date: Optional[date] = None
    goal_id: Optional[int] = None
    parent_id: Optional[int] = None



class TaskListGrouped(BaseModel):
    today: List[Task]
    tomorrow: List[Task]



class EventBase(BaseModel):
    title: str
    location: Optional[str] = None
    start_datetime: datetime
    end_datetime: datetime
    category: CategoryEnum
    notes: Optional[str] = None
    reminder_minutes_before: Optional[int] = None
    recurrence_rule: Optional[str] = None
    recurrence_end_date: Optional[date] = None

class EventCreate(EventBase):
    pass

class Event(EventBase):
    id: int
    owner_id: int
    model_config = ConfigDict(from_attributes=True)

class GoalStatusEnum(str, Enum):
    not_started = "Not Started"
    in_progress = "In Progress"
    completed = "Completed"
    abandoned = "Abandoned"

class GoalBase(BaseModel):
    title: str
    description: Optional[str] = None
    due_date: Optional[date] = None
    status: GoalStatusEnum = GoalStatusEnum.not_started
    progress: int = 0

class GoalCreate(GoalBase):
    pass

class Goal(GoalBase):
    id: int
    owner_id: int
    model_config = ConfigDict(from_attributes=True)

class NaturalLanguageTaskCreate(BaseModel):
    text: str

class NaturalLanguageTaskResponse(BaseModel):
    status: str
    message: str
    parsed_task: Optional[Task] = None


class SchedulingRequest(BaseModel):
    task_id: int
    preferred_time_range: Optional[tuple[time, time]] = None

class SchedulingSuggestion(BaseModel):
    start_time: time
    end_time: time
    confidence_score: float

class GoogleCalendarSyncRequest(BaseModel):
    force_full_sync: bool = False

class Nudge(BaseModel):
    id: str
    message: str
    action_url: Optional[str] = None

class Notification(BaseModel):
    id: str
    title: str
    body: str
    scheduled_time: datetime
    sent: bool = False
    read: bool = False

class NotificationTimingSuggestion(BaseModel):
    suggested_time: datetime
    reason: str

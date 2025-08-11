from sqlalchemy import Column, Integer, String, Date, Time, Enum, ForeignKey, Boolean, DateTime
from sqlalchemy.orm import relationship
from ..database.base import Base
import enum
from datetime import datetime

class PriorityEnum(str, enum.Enum):
    high = "High"
    medium = "Medium"
    low = "Low"

class ReminderEnum(str, enum.Enum):
    ten_minutes = "10 minutes before"
    one_hour = "1 hour before"
    one_day = "1 day before"

class CategoryEnum(str, enum.Enum):
    work = "Work"
    personal = "Personal"
    social = "Social"
    other = "Other"

class PasswordResetToken(Base):
    __tablename__ = "password_reset_tokens"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    token = Column(String, unique=True, index=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="reset_tokens")

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    full_name = Column(String)
    hashed_password = Column(String)
    email = Column(String, unique=True, index=True, nullable=False)
    profile_picture_url = Column(String, nullable=True)
    theme = Column(String, default="light")
    language = Column(String, default="en")
    email_notifications_enabled = Column(Boolean, default=True)
    push_notifications_enabled = Column(Boolean, default=True)

    tasks = relationship("Task", back_populates="owner")
    events = relationship("Event", back_populates="owner")
    reset_tokens = relationship("PasswordResetToken", back_populates="user")
    goals = relationship("Goal", back_populates="owner")

    @staticmethod
    def get_user_by_id(db, user_id: int):
        return db.query(User).filter(User.id == user_id).first()




class Task(Base):
    __tablename__ = "tasks"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    description = Column(String, nullable=True)
    due_date = Column(Date)
    due_time = Column(Time)
    priority = Column(Enum(PriorityEnum))
    reminder = Column(Enum(ReminderEnum))
    completed = Column(Boolean, default=False)
    completed_at = Column(DateTime, nullable=True)
    recurrence_rule = Column(String, nullable=True) # Stores RRULE for recurring tasks
    recurrence_end_date = Column(Date, nullable=True) # When the recurrence ends
    owner_id = Column(Integer, ForeignKey("users.id"))
    goal_id = Column(Integer, ForeignKey("goals.id"), nullable=True) # Link to Goal
    parent_id = Column(Integer, ForeignKey("tasks.id"), nullable=True) # For sub-tasks

    owner = relationship("User", back_populates="tasks")
    goal = relationship("Goal", back_populates="tasks")
    parent_task = relationship("Task", remote_side=[id], back_populates="subtasks")
    subtasks = relationship("Task", back_populates="parent_task")


class Event(Base):
    __tablename__ = "events"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    location = Column(String, nullable=True)
    start_datetime = Column(DateTime)
    end_datetime = Column(DateTime)
    category = Column(Enum(CategoryEnum))
    notes = Column(String, nullable=True)
    reminder_minutes_before = Column(Integer, nullable=True)
    recurrence_rule = Column(String, nullable=True) # Stores RRULE for recurring events
    recurrence_end_date = Column(Date, nullable=True) # When the recurrence ends
    owner_id = Column(Integer, ForeignKey("users.id"))

    owner = relationship("User", back_populates="events")

class GoalStatusEnum(str, enum.Enum):
    not_started = "Not Started"
    in_progress = "In Progress"
    completed = "Completed"
    abandoned = "Abandoned"

class Goal(Base):
    __tablename__ = "goals"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    description = Column(String, nullable=True)
    due_date = Column(Date, nullable=True)
    status = Column(Enum(GoalStatusEnum), default=GoalStatusEnum.not_started)
    progress = Column(Integer, default=0) # Percentage of completion
    owner_id = Column(Integer, ForeignKey("users.id"))

    owner = relationship("User", back_populates="goals")
    tasks = relationship("Task", back_populates="goal")

class TokenBlocklist(Base):
    __tablename__ = "token_blocklist"

    id = Column(Integer, primary_key=True, index=True)
    jti = Column(String, unique=True, index=True, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

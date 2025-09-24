from sqlalchemy import Column, Integer, String, Date, Time, Enum, ForeignKey, Boolean, DateTime
from sqlalchemy.orm import relationship
from ..database.base import Base
import enum

class PriorityEnum(str, enum.Enum):
    high = "High"
    medium = "Medium"
    low = "Low"

class ReminderEnum(str, enum.Enum):
    ten_minutes = "10 minutes before"
    one_hour = "1 hour before"
    one_day = "1 day before"

class GoalStatusEnum(str, enum.Enum):
    not_started = "Not Started"
    in_progress = "In Progress"
    completed = "Completed"
    abandoned = "Abandoned"

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
    owner_id = Column(String, ForeignKey("users.id"))
    goal_id = Column(Integer, ForeignKey("goals.id"), nullable=True) # Link to Goal
    parent_id = Column(Integer, ForeignKey("tasks.id"), nullable=True) # For sub-tasks

    owner = relationship("User", back_populates="tasks")
    goal = relationship("Goal", back_populates="tasks")
    parent_task = relationship("Task", remote_side=[id], back_populates="subtasks")
    subtasks = relationship("Task", back_populates="parent_task")

class Goal(Base):
    __tablename__ = "goals"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    description = Column(String, nullable=True)
    due_date = Column(Date, nullable=True)
    status = Column(Enum(GoalStatusEnum), default=GoalStatusEnum.not_started)
    progress = Column(Integer, default=0) # Percentage of completion
    owner_id = Column(String, ForeignKey("users.id"))

    owner = relationship("User", back_populates="goals")
    tasks = relationship("Task", back_populates="goal")

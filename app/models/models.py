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

class CategoryEnum(str, enum.Enum):
    work = "Work"
    personal = "Personal"
    social = "Social"
    other = "Other"

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True)
    hashed_password = Column(String)

    tasks = relationship("Task", back_populates="owner")
    events = relationship("Event", back_populates="owner")


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
    owner_id = Column(Integer, ForeignKey("users.id"))

    owner = relationship("User", back_populates="tasks")


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
    owner_id = Column(Integer, ForeignKey("users.id"))

    owner = relationship("User", back_populates="events")

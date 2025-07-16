from sqlalchemy import Column, Integer, String, Date, Time, Enum, ForeignKey, Boolean
from sqlalchemy.orm import relationship
from ..database.database import Base
import enum

class PriorityEnum(str, enum.Enum):
    high = "High"
    medium = "Medium"
    low = "Low"

class ReminderEnum(str, enum.Enum):
    ten_minutes = "10 minutes before"
    one_hour = "1 hour before"
    one_day = "1 day before"

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True)
    hashed_password = Column(String)

    tasks = relationship("Task", back_populates="owner")


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

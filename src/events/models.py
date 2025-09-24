from sqlalchemy import Column, Integer, String, DateTime, Enum, ForeignKey, Date
from sqlalchemy.orm import relationship
from ..database.base import Base
import enum

class CategoryEnum(str, enum.Enum):
    work = "Work"
    personal = "Personal"
    social = "Social"
    health = "Health"
    other = "Other"

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
    owner_id = Column(String, ForeignKey("users.id"))

    owner = relationship("User", back_populates="events")

import uuid
from sqlalchemy import Column, String, Integer, ForeignKey, CheckConstraint
from sqlalchemy.orm import relationship
from ..database.base import Base

class KanbanBoard(Base):
    __tablename__ = "kanban_boards"
    id = Column(String, primary_key=True, index=True, default=lambda: str(uuid.uuid4()))
    name = Column(String, index=True)
    owner_id = Column(String, ForeignKey("users.id"))

    owner = relationship("User")
    columns = relationship("KanbanColumn", back_populates="board", cascade="all, delete-orphan")

class KanbanColumn(Base):
    __tablename__ = "kanban_columns"
    id = Column(String, primary_key=True, index=True, default=lambda: str(uuid.uuid4()))
    name = Column(String)
    position = Column(Integer)
    board_id = Column(String, ForeignKey("kanban_boards.id"))

    board = relationship("KanbanBoard", back_populates="columns")
    cards = relationship("KanbanCard", back_populates="column", cascade="all, delete-orphan")

class KanbanCard(Base):
    __tablename__ = "kanban_cards"
    id = Column(String, primary_key=True, index=True, default=lambda: str(uuid.uuid4()))
    position = Column(Integer)
    column_id = Column(String, ForeignKey("kanban_columns.id"))
    task_id = Column(String, ForeignKey("tasks.id"), nullable=True)
    event_id = Column(String, ForeignKey("events.id"), nullable=True)

    column = relationship("KanbanColumn", back_populates="cards")
    task = relationship("Task")
    event = relationship("Event")

    __table_args__ = (CheckConstraint("(task_id IS NOT NULL AND event_id IS NULL) OR (task_id IS NULL AND event_id IS NOT NULL)", name="cc_card_task_or_event"),)

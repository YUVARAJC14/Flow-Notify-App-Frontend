from pydantic import BaseModel
from typing import List, Optional
import uuid
from src.schemas.schemas import Task, Event

# Card Schemas
class KanbanCardBase(BaseModel):
    position: int
    task_id: Optional[str] = None
    event_id: Optional[str] = None

class KanbanCardCreate(KanbanCardBase):
    pass

class KanbanCard(KanbanCardBase):
    id: str
    column_id: str
    task: Optional[Task] = None
    event: Optional[Event] = None

    class Config:
        orm_mode = True

class KanbanCardMoveRequest(BaseModel):
    new_column_id: str
    position: int

# Column Schemas
class KanbanColumnBase(BaseModel):
    name: str
    position: int

class KanbanColumnCreate(KanbanColumnBase):
    pass

class KanbanColumn(KanbanColumnBase):
    id: str
    board_id: str
    cards: List[KanbanCard] = []

    class Config:
        orm_mode = True

# Board Schemas
class KanbanBoardBase(BaseModel):
    name: str

class KanbanBoardCreate(KanbanBoardBase):
    pass

class KanbanBoard(KanbanBoardBase):
    id: str
    owner_id: str
    columns: List[KanbanColumn] = []

    class Config:
        orm_mode = True

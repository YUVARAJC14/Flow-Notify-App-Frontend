from pydantic import BaseModel
from typing import List
from .schemas import Task
from .schemas import Event

class HomePage(BaseModel):
    user_name: str
    todays_flow: float
    upcoming_tasks: List[Task]
    todays_schedule: List[Event]

from pydantic import BaseModel
from datetime import datetime
from typing import Optional

class SchedulingSuggestion(BaseModel):
    start_time: datetime
    end_time: datetime

class SchedulingRequest(BaseModel):
    task_type: str
    duration_minutes: int

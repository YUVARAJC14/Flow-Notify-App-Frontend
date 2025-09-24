from pydantic import BaseModel
from datetime import datetime
from typing import Optional, Dict

class Nudge(BaseModel):
    user_id: int
    message: str
    nudge_type: str
    timestamp: datetime
    context: Optional[Dict] = None

class NudgeRequest(BaseModel):
    event_id: Optional[int] = None
    task_id: Optional[int] = None
    # Add other relevant context for nudge generation

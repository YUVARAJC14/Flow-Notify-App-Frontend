from pydantic import BaseModel
from datetime import datetime
from typing import Optional, Dict

class NotificationContent(BaseModel):
    message: str
    notification_type: str # e.g., 'urgent', 'reminder', 'informational'
    urgency: int = 5 # 1 (low) to 10 (high)
    related_event_id: Optional[int] = None
    related_task_id: Optional[int] = None

class NotificationTimingSuggestion(BaseModel):
    scheduled_time: datetime
    reason: str

class NotificationInteraction(BaseModel):
    notification_id: int
    user_id: int
    interaction_type: str # e.g., 'opened', 'dismissed', 'acted_on'
    timestamp: datetime
    details: Optional[Dict] = None

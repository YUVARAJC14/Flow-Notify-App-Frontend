from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime

class Comparison(BaseModel):
    change: int
    period: str

class FlowScore(BaseModel):
    score: int
    comparison: Comparison

class TaskCompletion(BaseModel):
    label: str
    completed: int
    total: int

class ProductiveTime(BaseModel):
    day: int
    hour: int
    intensity: float

class InsightsResponse(BaseModel):
    flowScore: FlowScore
    taskCompletion: List[TaskCompletion]
    productiveTimes: List[ProductiveTime]

class DailyTaskSummary(BaseModel):
    id: str
    title: str
    completed_at: Optional[datetime]

class DailyEventSummary(BaseModel):
    id: str
    title: str
    completed_at: Optional[datetime]

class DailyInsightsResponse(BaseModel):
    flowScore: int
    completedTasks: List[DailyTaskSummary]
    completedEvents: List[DailyEventSummary]
    deletedTasks: List[DailyTaskSummary]
    deletedEvents: List[DailyEventSummary]

class ActivitySummaryResponse(BaseModel):
    summary: str
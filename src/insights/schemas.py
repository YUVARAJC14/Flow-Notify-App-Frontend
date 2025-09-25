from pydantic import BaseModel
from typing import List, Dict

class FlowScore(BaseModel):
    score: int
    comparison: Dict[str, str]

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

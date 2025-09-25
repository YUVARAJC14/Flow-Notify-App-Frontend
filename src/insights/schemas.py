from pydantic import BaseModel
from typing import List

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

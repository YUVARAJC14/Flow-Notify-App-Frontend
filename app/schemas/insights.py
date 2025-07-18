from pydantic import BaseModel
from typing import List, Dict

class FlowScore(BaseModel):
    score: float
    comparison: str

class TaskCompletion(BaseModel):
    labels: List[str]
    data: List[int]

class ProductiveTimes(BaseModel):
    data: Dict[str, List[int]]

class Insights(BaseModel):
    flowScore: FlowScore
    taskCompletion: TaskCompletion
    productiveTimes: ProductiveTimes

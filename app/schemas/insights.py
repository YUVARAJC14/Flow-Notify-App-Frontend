from pydantic import BaseModel
from typing import List, Dict

class FlowScore(BaseModel):
    score: float
    comparisonText: str

class TaskCompletion(BaseModel):
    labels: List[str]
    data: List[int]

class ProductiveTimes(BaseModel):
    labels: List[str]
    data: List[List[int]]

class Insights(BaseModel):
    flowScore: FlowScore
    taskCompletion: TaskCompletion
    productiveTimes: ProductiveTimes

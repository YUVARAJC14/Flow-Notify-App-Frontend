from pydantic import BaseModel
from typing import List, Optional

class TodaysFlow(BaseModel):
    percentage: int
    message: str

class UpcomingTask(BaseModel):
    id: str
    title: str
    time: str
    priority: str

class TodaysSchedule(BaseModel):
    id: str
    title: str
    time: str
    location: Optional[str]

class DashboardData(BaseModel):
    greetingName: str
    todaysFlow: TodaysFlow
    upcomingTasks: List[UpcomingTask]
    todaysSchedule: List[TodaysSchedule]

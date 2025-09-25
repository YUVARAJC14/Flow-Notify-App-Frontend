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

class ScheduleEvent(BaseModel):
    id: str
    title: str
    time: str
    location: str
    category: str

class DashboardSummaryResponse(BaseModel):
    todaysFlow: TodaysFlow
    upcomingTasks: List[UpcomingTask]
    todaysSchedule: List[ScheduleEvent]
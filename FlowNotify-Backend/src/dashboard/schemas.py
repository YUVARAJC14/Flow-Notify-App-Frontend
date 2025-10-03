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
    description: str
    dueDate: str
    completed: bool

class ScheduleEvent(BaseModel):
    id: str
    title: str
    time: str
    endTime: str
    location: str
    category: str
    notes: Optional[str] = None
    date: str

class DashboardSummaryResponse(BaseModel):
    todaysFlow: TodaysFlow
    upcomingTasks: List[UpcomingTask]
    todaysSchedule: List[ScheduleEvent]
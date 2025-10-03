from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from src.auth import models as auth_models
from . import schemas as schemas_insights
from ..services import insights_service
from ..database.database import get_db
from ..security import get_current_user

from typing import List, Optional

router = APIRouter(
    prefix="/insights",
    tags=["insights"]
)

@router.get("", response_model=schemas_insights.InsightsResponse)
def get_dashboard_summary(
    period: str = Query("week"),
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    return insights_service.get_insights(db, current_user.id, period)

from datetime import date

@router.get("/activity-summary", response_model=schemas_insights.ActivitySummaryResponse)

def get_activity_summary_route(
    period: str = Query("week"),
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    summary = insights_service.get_activity_summary(db, current_user.id, period)
    return {"summary": summary}

@router.get("/{date}", response_model=schemas_insights.DailyInsightsResponse)

def get_daily_insights(
    date: date,
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    return insights_service.get_daily_insights(db, current_user.id, date)
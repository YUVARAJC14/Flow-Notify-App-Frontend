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

@router.get("/activity-summary")
def get_activity_summary(
    period: str = Query("week"),
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    summary_text = insights_service.get_activity_summary(db, current_user.id, period)
    return {"summary": summary_text}
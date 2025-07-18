from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import models
from ..schemas import insights as schemas_insights
from ..services import insights_service
from ..database.database import SessionLocal
from ..routers.users import get_current_user

router = APIRouter()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.get("/api/insights", response_model=schemas_insights.Insights)
def get_insights(
    period: str = "week",
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    return insights_service.get_insights(db=db, user_id=current_user.id, period=period)

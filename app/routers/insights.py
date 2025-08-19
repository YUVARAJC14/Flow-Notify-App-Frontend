from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from .. import models
from ..schemas import insights as schemas_insights
from ..services import insights_service
from ..database.database import get_db
from ..security import get_current_user

router = APIRouter(
    tags=["insights"],
    dependencies=[Depends(get_current_user)]
)

@router.get("/insights", response_model=schemas_insights.Insights)
def get_insights(
    period: str = "week",
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    return insights_service.get_insights(db=db, user_id=current_user.id, period=period)
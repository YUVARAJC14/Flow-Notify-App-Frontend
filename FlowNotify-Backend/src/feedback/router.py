from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session
from src.auth import models as auth_models
from ..database.database import get_db
from ..security import get_current_user
from . import schemas

router = APIRouter(
    prefix="/feedback",
    tags=["feedback"]
)

@router.post("", status_code=status.HTTP_200_OK)
def submit_feedback(
    feedback: schemas.FeedbackCreate,
    db: Session = Depends(get_db), # Keep db dependency even if not used for storage yet
    current_user: auth_models.User = Depends(get_current_user) # Get current user for context
):
    # In a real application, you would save this feedback to a database
    # or send it to a feedback collection service.
    print(f"Received feedback from {current_user.email}: {feedback.message}")
    return {"message": "Feedback submitted successfully."}

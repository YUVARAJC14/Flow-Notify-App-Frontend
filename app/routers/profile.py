from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from .. import crud, models
from ..schemas import schemas as schemas_all
from ..database.database import SessionLocal
from ..routers.users import get_current_user
from ..security import get_password_hash, verify_password

router = APIRouter()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.get("/profile", response_model=schemas_all.User)
def get_user_profile(
    current_user: models.User = Depends(get_current_user)
):
    return current_user

@router.put("/profile", response_model=schemas_all.User)
def update_user_profile(
    profile_update: schemas_all.UserProfileUpdate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    updated_user = crud.update_user_profile(db, current_user, profile_update)
    return updated_user

@router.put("/profile/password", status_code=status.HTTP_204_NO_CONTENT)
def change_password(
    password_change: schemas_all.PasswordChange,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not verify_password(password_change.current_password, current_user.hashed_password):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid current password")
    
    hashed_password = get_password_hash(password_change.new_password)
    crud.update_user_password(db, current_user, hashed_password)
    return

@router.put("/profile/settings", response_model=schemas_all.User)
def update_app_settings(
    settings_update: schemas_all.AppSettingsUpdate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    updated_user = crud.update_app_settings(db, current_user, settings_update)
    return updated_user

@router.put("/profile/notifications", response_model=schemas_all.User)
def update_notification_settings(
    notification_update: schemas_all.NotificationSettingsUpdate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    updated_user = crud.update_notification_settings(db, current_user, notification_update)
    return updated_user

@router.get("/app-info")
def get_app_info():
    return {
        "app_version": "1.0.0",
        "privacy_policy_url": "https://example.com/privacy",
        "terms_of_service_url": "https://example.com/terms",
        "help_center_url": "https://example.com/help"
    }

@router.post("/feedback", status_code=status.HTTP_202_ACCEPTED)
def submit_feedback(
    feedback: schemas_all.Feedback,
    current_user: models.User = Depends(get_current_user)
):
    # In a real application, this would send feedback to an email or a dedicated feedback system.
    print(f"Feedback from {current_user.username}: {feedback.message}")
    return {"message": "Feedback submitted successfully"}

@router.post("/logout", status_code=status.HTTP_200_OK)
def logout():
    # In a real application, this would invalidate the user's session or token.
    return {"message": "Logged out successfully"}

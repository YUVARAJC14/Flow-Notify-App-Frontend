from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from .. import crud
from src.auth import models as auth_models
from ..schemas import schemas as schemas_all
from ..database.database import get_db
from ..security import get_current_user
from ..auth_utils import get_password_hash, verify_password

router = APIRouter(
    prefix="/users",
    tags=["users"]
)

@router.get("/me", response_model=schemas_all.UserResponse)
def get_user_profile(current_user: auth_models.User = Depends(get_current_user)):
    return current_user

@router.put("/me", response_model=schemas_all.UserResponse)
def update_user_profile(
    profile_update: schemas_all.UserProfileUpdate,
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    return crud.update_user_profile(db, current_user, profile_update)

@router.put("/me/password", status_code=status.HTTP_200_OK)
def change_password(
    password_change: schemas_all.PasswordChange,
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    if not verify_password(password_change.current_password, current_user.hashed_password):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid current password")
    
    hashed_password = get_password_hash(password_change.new_password)
    crud.update_user_password(db, current_user, hashed_password)
    return {"message": "Password updated successfully."}


@router.patch("/me/profile", response_model=schemas_all.UserResponse)
def update_user_profile(
    profile_update: schemas_all.UserProfileUpdate,
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    return crud.update_user_profile(db, current_user, profile_update)

@router.patch("/me/settings", response_model=schemas_all.UserResponse)
def update_user_settings(
    settings_update: schemas_all.AppSettingsUpdate,
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    return crud.update_user_settings(db, current_user, settings_update)

@router.patch("/me/notification-settings", response_model=schemas_all.UserResponse)
def update_notification_settings(
    notification_update: schemas_all.NotificationSettingsUpdate,
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    return crud.update_notification_settings(db, current_user, notification_update)

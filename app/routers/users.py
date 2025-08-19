from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from .. import crud, models
from ..schemas import schemas as schemas_all
from ..database.database import get_db
from ..security import get_current_user
from ..auth_utils import get_password_hash, verify_password

router = APIRouter(
    prefix="/users",
    tags=["users"],
    dependencies=[Depends(get_current_user)]
)

@router.get("/me", response_model=schemas_all.User)
def get_user_profile(current_user: models.User = Depends(get_current_user)):
    return current_user

@router.put("/me", response_model=schemas_all.User)
def update_user_profile(
    profile_update: schemas_all.UserProfileUpdate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    return crud.update_user_profile(db, current_user, profile_update)

@router.post("/me/change-password", status_code=status.HTTP_204_NO_CONTENT)
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


@router.patch("/me/profile", response_model=schemas_all.User)
def update_user_name(
    name_update: schemas_all.UserNameUpdate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    return crud.update_user_name(db, current_user, name_update.name)

@router.patch("/me/settings", response_model=schemas_all.User)
def update_user_settings(
    settings_update: schemas_all.AppSettingsUpdate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    return crud.update_user_settings(db, current_user, settings_update)

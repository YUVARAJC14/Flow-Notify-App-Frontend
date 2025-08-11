from fastapi import APIRouter, Depends
from .. import models, schemas
from ..security import get_current_user

router = APIRouter()

@router.get("/users/me", response_model=schemas.User)
async def read_users_me(current_user: models.User = Depends(get_current_user)):
    return current_user
from fastapi import APIRouter, Depends, HTTPException, Response, Cookie
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from typing import Optional
from .. import crud, models, schemas
from ..database.database import SessionLocal, engine, Base
from ..security import verify_password, create_session_token, verify_session_token



router = APIRouter()

# Dependency
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

def get_current_user(session: Optional[str] = Cookie(None), db: Session = Depends(get_db)):
    if not session:
        return None
    user_data = verify_session_token(session)
    if not user_data:
        return None
    user = crud.get_user_by_username(db, username=user_data.get("sub"))
    if not user:
        return None
    return user

@router.post("/users/", response_model=schemas.User)
def create_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = crud.get_user_by_username(db, username=user.username)
    if db_user:
        raise HTTPException(status_code=400, detail="Username already registered")
    return crud.create_user(db=db, user=user)

@router.post("/login")
def login(response: Response, form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    user = crud.get_user_by_username(db, username=form_data.username)
    print(f"Login attempt for user: {form_data.username}")
    if not user:
        print("User not found.")
        raise HTTPException(
            status_code=401,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    print(f"User found: {user.username}, Hashed Password from DB: {user.hashed_password}")
    if not verify_password(form_data.password, user.hashed_password):
        print("Password verification failed.")
        raise HTTPException(
            status_code=401,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    print("Password verification successful.")
    session_token = create_session_token(data={"sub": user.username})
    response.set_cookie(key="session", value=session_token, httponly=True)
    return {"message": "Login successful"}

@router.post("/logout")
def logout(response: Response):
    response.delete_cookie(key="session")
    return {"message": "Logout successful"}

@router.get("/users/me", response_model=schemas.User)
def read_users_me(current_user: models.User = Depends(get_current_user)):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    return current_user

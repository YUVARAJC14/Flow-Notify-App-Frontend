from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from .. import crud
from ..schemas import schemas
from ..database.database import get_db
from ..security import create_access_token, create_refresh_token, get_current_user, get_jti_from_token
from ..auth_utils import verify_password

router = APIRouter(
    prefix="/auth",
    tags=["auth"],
)

@router.post("/register", status_code=status.HTTP_201_CREATED)
def register(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = crud.get_user_by_email(db, email=user.email)
    if db_user:
        raise HTTPException(status_code=400, detail="Email already registered")
    
    crud.create_user(db=db, user=user)
    return {"message": "Registration successful. Please check your email to verify your account."}


@router.post("/login", response_model=schemas.LoginResponse)
def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    user = crud.get_user_by_email(db, email=form_data.username)
    if not user or not verify_password(form_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    access_token = create_access_token(data={"sub": user.email})
    refresh_token = create_refresh_token(data={"sub": user.email})

    user_data = schemas.UserLoginResponse(
        id=str(user.id),
        name=user.full_name,
        email=user.email,
    )

    return schemas.LoginResponse(
        accessToken=access_token,
        refreshToken=refresh_token,
        user=user_data
    )

@router.post("/forgot-password", status_code=status.HTTP_200_OK)
def forgot_password(data: schemas.ForgotPasswordSchema, db: Session = Depends(get_db)):
    user = crud.get_user_by_email(db, email=data.email)
    if user:
        # In a real app, you'd send an email with the reset token.
        crud.create_password_reset_token(db, user)
    return {"message": "If an account with that email exists, a password reset link has been sent."}

@router.post("/resend-verification", status_code=status.HTTP_200_OK)
def resend_verification(data: schemas.ResendVerificationSchema, db: Session = Depends(get_db)):
    # In a real app, you'd resend the verification email.
    return {"message": "Verification email resent."}

@router.post("/verify-email", status_code=status.HTTP_200_OK)
def verify_email(data: schemas.VerifyEmailSchema, db: Session = Depends(get_db)):
    # In a real app, you'd verify the token and activate the user's account.
    # For now, we'll just return a success message.
    return {"message": "Email verified successfully."}

@router.post("/reset-password", status_code=status.HTTP_200_OK)
def reset_password(data: schemas.ResetPasswordSchema, db: Session = Depends(get_db)):
    # In a real app, you'd verify the token and update the user's password.
    # For now, we'll just return a success message.
    return {"message": "Password has been reset successfully."}

@router.post("/logout", status_code=status.HTTP_200_OK)
def logout(
    refresh_token_request: schemas.RefreshTokenRequest,
    db: Session = Depends(get_db)
):
    jti = get_jti_from_token(refresh_token_request.refreshToken)
    crud.add_token_to_blocklist(db, jti)
    return {"message": "Logged out successfully."}

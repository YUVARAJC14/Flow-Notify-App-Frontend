from passlib.context import CryptContext
from itsdangerous import URLSafeTimedSerializer
from .config import SECRET_KEY

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password):
    return pwd_context.hash(password)

def create_session_token(data: dict):
    serializer = URLSafeTimedSerializer(SECRET_KEY)
    return serializer.dumps(data)

def verify_session_token(token: str):
    serializer = URLSafeTimedSerializer(SECRET_KEY)
    try:
        return serializer.loads(token, max_age=3600)  # 1 hour expiry
    except Exception:
        return None

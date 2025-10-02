from sqlalchemy import Column, Integer, String, Boolean, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from ..database.base import Base
import uuid
from datetime import datetime

class PasswordResetToken(Base):
    __tablename__ = "password_reset_tokens"

    id = Column(String, primary_key=True, index=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String, ForeignKey("users.id"))
    token = Column(String, unique=True, index=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    user = relationship("User", back_populates="reset_tokens")

class User(Base):
    __tablename__ = "users"

    id = Column(String, primary_key=True, index=True, default=lambda: str(uuid.uuid4()))
    full_name = Column(String)
    hashed_password = Column(String)
    email = Column(String, unique=True, index=True, nullable=False)
    profile_picture_url = Column(String, nullable=True)
    bio = Column(String, nullable=True)
    theme = Column(String, default="light")
    language = Column(String, default="en")
    email_notifications_enabled = Column(Boolean, default=True)
    push_notifications_enabled = Column(Boolean, default=True)

    tasks = relationship("Task", back_populates="owner")
    events = relationship("Event", back_populates="owner")
    reset_tokens = relationship("PasswordResetToken", back_populates="user")
    goals = relationship("Goal", back_populates="owner")

    @property
    def name(self):
        return self.full_name

    @staticmethod
    def get_user_by_id(db, user_id: str):
        return db.query(User).filter(User.id == user_id).first()

class TokenBlocklist(Base):
    __tablename__ = "token_blocklist"

    id = Column(Integer, primary_key=True, index=True)
    jti = Column(String, unique=True, index=True, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

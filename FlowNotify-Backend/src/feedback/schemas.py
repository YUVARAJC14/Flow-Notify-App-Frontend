from pydantic import BaseModel

class FeedbackCreate(BaseModel):
    message: str
    email: str # Optional, if user is logged in, can get from token

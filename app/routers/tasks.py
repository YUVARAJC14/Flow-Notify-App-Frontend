from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import Optional
from .. import crud, schemas, models
from ..database.database import SessionLocal
from .users import get_current_user

router = APIRouter(
    prefix="/tasks",
    tags=["tasks"],
)

# Dependency
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.post("/", response_model=schemas.Task)
def create_task_for_user(
    task: schemas.TaskCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    return crud.create_user_task(db=db, task=task, user_id=current_user.id)

@router.get("/", response_model=list[schemas.Task])
def read_tasks(
    search: Optional[str] = Query(None, description="Search by task title or description"),
    date_filter: Optional[str] = Query("all", description="Filter tasks by date (all, today, upcoming, completed)"),
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    tasks = crud.get_tasks(db=db, user_id=current_user.id, search=search, date_filter=date_filter)
    return tasks

@router.put("/{task_id}", response_model=schemas.Task)
def update_task(
    task_id: int,
    task: schemas.TaskCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    db_task = crud.get_task_by_id(db=db, task_id=task_id, user_id=current_user.id)
    if not db_task:
        raise HTTPException(status_code=404, detail="Task not found")
    return crud.update_task(db=db, task=db_task, task_update=task)

@router.delete("/{task_id}", status_code=204)
def delete_task(
    task_id: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    if not current_user:
        raise HTTPException(status_code=401, detail="Not authenticated")
    db_task = crud.get_task_by_id(db=db, task_id=task_id, user_id=current_user.id)
    if not db_task:
        raise HTTPException(status_code=404, detail="Task not found")
    crud.delete_task(db=db, task=db_task)
    return {"ok": True}

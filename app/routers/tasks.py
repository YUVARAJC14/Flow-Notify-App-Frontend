from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session
from typing import Optional, List
from .. import crud, models
from ..schemas import schemas
from ..database.database import get_db
from ..security import get_current_user

router = APIRouter(
    prefix="/tasks",
    tags=["tasks"]
)

@router.post("/", response_model=schemas.Task, status_code=status.HTTP_201_CREATED)
def create_task(
    task: schemas.TaskCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    return crud.create_user_task(db=db, task=task, user_id=current_user.id)

@router.get("/", response_model=schemas.TaskListGrouped)
def read_tasks(
    filter: Optional[str] = None,
    search: Optional[str] = None,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    return crud.get_tasks(db=db, user_id=current_user.id, search=search, date_filter=filter)


@router.patch("/{task_id}", response_model=schemas.Task)
def update_task(
    task_id: int,
    task_update: schemas.TaskPartialUpdate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    db_task = crud.get_task_by_id(db=db, task_id=task_id, user_id=current_user.id)
    if not db_task:
        raise HTTPException(status_code=404, detail="Task not found")
    return crud.update_task(db=db, task=db_task, task_update=task_update)

@router.delete("/{task_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_task(
    task_id: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    db_task = crud.get_task_by_id(db=db, task_id=task_id, user_id=current_user.id)
    if not db_task:
        raise HTTPException(status_code=4.04, detail="Task not found")
    crud.delete_task(db=db, task=db_task)
    return

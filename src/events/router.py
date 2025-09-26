from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session
from .. import crud
from src.auth import models as auth_models
from src.events import models as event_models
from ..schemas import schemas
from ..database.database import get_db
from ..security import get_current_user
from typing import List, Optional
from datetime import date, datetime
from pydantic import ValidationError

router = APIRouter(
    prefix="/events",
    tags=["events"]
)

@router.post("", response_model=schemas.EventResponse, status_code=status.HTTP_201_CREATED)
def create_event(
    event_request: schemas.EventCreateRequest,
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    try:
        start_datetime_str = f"{event_request.date}T{event_request.startTime}"
        end_datetime_str = f"{event_request.date}T{event_request.endTime}"
        start_datetime = datetime.fromisoformat(start_datetime_str)
        end_datetime = datetime.fromisoformat(end_datetime_str)

        event_data = schemas.EventCreate(
            title=event_request.title,
            location=event_request.location,
            start_datetime=start_datetime,
            end_datetime=end_datetime,
            category=schemas.CategoryEnum(event_request.category),
            notes=event_request.notes,
            reminder_minutes_before=event_request.reminder
        )
    except ValueError as e:
        raise HTTPException(status_code=422, detail=f"Invalid event data: {e}")
    except ValidationError as e:
        raise HTTPException(status_code=422, detail=str(e))

    created_event = crud.create_user_event(db=db, event=event_data, user_id=current_user.id)

    return schemas.EventResponse(
        id=str(created_event.id),
        title=created_event.title,
        date=created_event.start_datetime.strftime('%Y-%m-%d'),
        startTime=created_event.start_datetime.strftime('%I:%M %p'),
        endTime=created_event.end_datetime.strftime('%I:%M %p'),
        location=created_event.location,
        category=created_event.category.value,
        notes=created_event.notes
    )

@router.get("", response_model=List[schemas.EventResponse])
def read_events(
    start_date: Optional[date] = Query(None, alias="startDate"),
    end_date: Optional[date] = Query(None, alias="endDate"),
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    events_db = crud.get_events(db=db, user_id=current_user.id, start_date=start_date, end_date=end_date)
    response_events = []
    for event in events_db:
        response_events.append(
            schemas.EventResponse(
                id=str(event.id),
                title=event.title,
                date=event.start_datetime.strftime('%Y-%m-%d'),
                startTime=event.start_datetime.strftime('%I:%M %p'),
                endTime=event.end_datetime.strftime('%I:%M %p'),
                location=event.location,
                category=event.category.value,
                notes=event.notes
            )
        )
    return response_events

@router.put("/{event_id}", response_model=schemas.Event)
def update_event(
    event_id: int,
    event_update: schemas.EventUpdate,
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    db_event = crud.get_event_by_id(db=db, event_id=event_id, user_id=current_user.id)
    if db_event is None:
        raise HTTPException(status_code=404, detail="Event not found")
    
    # Create a new object for the update to avoid directly modifying the ORM model with a Pydantic model
    update_data = event_update.model_dump(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_event, key, value)
        
    return crud.update_event(db=db, event=db_event, event_update=event_update)

@router.delete("/{event_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_event(
    event_id: int,
    db: Session = Depends(get_db),
    current_user: auth_models.User = Depends(get_current_user)
):
    db_event = crud.get_event_by_id(db=db, event_id=event_id, user_id=current_user.id)
    if db_event is None:
        raise HTTPException(status_code=404, detail="Event not found")
    crud.delete_event(db=db, event=db_event)
    return
import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session
from datetime import date, time, timedelta
from app.main import app
from app.models import models
from app.schemas import schemas

@pytest.fixture(scope="function")
def test_data(db_session, test_user):
    today = date.today()
    tasks = [
        models.Task(title="Today's Task 1", due_date=today, completed=True, owner_id=test_user.id),
        models.Task(title="Today's Task 2", due_date=today, completed=False, owner_id=test_user.id),
        models.Task(title="Upcoming Task", due_date=today + timedelta(days=3), owner_id=test_user.id)
    ]
    events = [
        models.Event(title="Today's Event", date=today, time=time(10, 0), owner_id=test_user.id)
    ]
    db_session.add_all(tasks)
    db_session.add_all(events)
    db_session.commit()
    return {"tasks": tasks, "events": events}

def test_get_home_page(authenticated_client, test_data):
    response = authenticated_client.get("/home")
    assert response.status_code == 200
    data = response.json()
    assert data["user_name"] == "testuser"
    assert data["todays_flow"] == 50.0
    assert len(data["upcoming_tasks"]) == 1
    assert data["upcoming_tasks"][0]["title"] == "Upcoming Task"
    assert len(data["todays_schedule"]) == 1
    assert data["todays_schedule"][0]["title"] == "Today's Event"

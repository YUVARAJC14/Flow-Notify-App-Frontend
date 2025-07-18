from fastapi.testclient import TestClient
from sqlalchemy.orm import Session
from app import crud, schemas
from datetime import date, timedelta, datetime

def test_create_user(client: TestClient, db_session: Session):
    response = client.post("/users/", json={"username": "newuser", "password": "newpassword"})
    assert response.status_code == 200, response.text
    data = response.json()
    assert data["username"] == "newuser"
    assert "id" in data

def test_login(client: TestClient, test_user):
    response = client.post("/login", data={"username": "testuser", "password": "testpassword"})
    assert response.status_code == 200, response.text
    data = response.json()
    assert data["message"] == "Login successful"

def test_read_users_me(authenticated_client: TestClient):
    response = authenticated_client.get("/users/me")
    assert response.status_code == 200, response.text
    data = response.json()
    assert data["username"] == "testuser"

def test_create_task(authenticated_client: TestClient):
    response = authenticated_client.post("/tasks/", json={
        "title": "Test Task",
        "description": "Test Description",
        "due_date": "2025-12-31",
        "due_time": "23:59:59",
        "priority": "High",
        "reminder": "1 hour before"
    })
    assert response.status_code == 200, response.text
    data = response.json()
    assert data["title"] == "Test Task"
    assert data["owner_id"] is not None

def test_read_tasks(authenticated_client: TestClient):
    response = authenticated_client.get("/tasks/")
    assert response.status_code == 200, response.text
    data = response.json()
    assert isinstance(data, list)

def test_create_event(authenticated_client: TestClient):
    response = authenticated_client.post("/events/", json={
        "title": "Test Event",
        "location": "Test Location",
        "start_datetime": "2025-12-31T10:00:00",
        "end_datetime": "2025-12-31T11:00:00",
        "category": "Work",
        "notes": "Test Notes",
        "reminder_minutes_before": 60
    })
    assert response.status_code == 200, response.text
    data = response.json()
    assert data["title"] == "Test Event"
    assert data["owner_id"] is not None

def test_read_events(authenticated_client: TestClient):
    # Create an event first
    authenticated_client.post("/events/", json={
        "title": "Test Event for Read",
        "location": "Test Location",
        "start_datetime": "2025-12-31T10:00:00",
        "end_datetime": "2025-12-31T11:00:00",
        "category": "Personal",
        "notes": "Test Notes",
        "reminder_minutes_before": 60
    })
    response = authenticated_client.get("/events/")
    assert response.status_code == 200, response.text
    data = response.json()
    assert isinstance(data, list)
    # Check if the created event is in the list of events
    assert any(event["title"] == "Test Event for Read" for event in data)

def test_read_single_event(authenticated_client: TestClient):
    # Create an event first
    create_response = authenticated_client.post("/events/", json={
        "title": "Single Event",
        "location": "Single Location",
        "start_datetime": "2025-12-31T12:00:00",
        "end_datetime": "2025-12-31T13:00:00",
        "category": "Social",
        "notes": "Single Notes",
        "reminder_minutes_before": 30
    })
    event_id = create_response.json()["id"]

    response = authenticated_client.get(f"/events/{event_id}")
    assert response.status_code == 200, response.text
    data = response.json()
    assert data["title"] == "Single Event"
    assert data["id"] == event_id

def test_update_event(authenticated_client: TestClient):
    # Create an event first
    create_response = authenticated_client.post("/events/", json={
        "title": "Event to Update",
        "location": "Old Location",
        "start_datetime": "2025-12-31T14:00:00",
        "end_datetime": "2025-12-31T15:00:00",
        "category": "Work",
        "notes": "Old Notes",
        "reminder_minutes_before": 15
    })
    event_id = create_response.json()["id"]

    update_response = authenticated_client.put(f"/events/{event_id}", json={
        "title": "Updated Event",
        "location": "New Location",
        "start_datetime": "2025-12-31T14:30:00",
        "end_datetime": "2025-12-31T15:30:00",
        "category": "Personal",
        "notes": "New Notes",
        "reminder_minutes_before": 10
    })
    assert update_response.status_code == 200, update_response.text
    data = update_response.json()
    assert data["title"] == "Updated Event"
    assert data["location"] == "New Location"

def test_delete_event(authenticated_client: TestClient):
    # Create an event first
    create_response = authenticated_client.post("/events/", json={
        "title": "Event to Delete",
        "location": "Delete Location",
        "start_datetime": "2025-12-31T16:00:00",
        "end_datetime": "2025-12-31T17:00:00",
        "category": "Other",
        "notes": "Delete Notes",
        "reminder_minutes_before": 5
    })
    event_id = create_response.json()["id"]

    delete_response = authenticated_client.delete(f"/events/{event_id}")
    assert delete_response.status_code == 204

    # Verify it's deleted
    get_response = authenticated_client.get(f"/events/{event_id}")
    assert get_response.status_code == 404

def test_get_home_page(authenticated_client: TestClient, test_user, db_session):
    # Create a task for today
    authenticated_client.post("/tasks/", json={
        "title": "Test Task Today",
        "description": "Test Description",
        "due_date": str(date.today()),
        "due_time": "23:59:59",
        "priority": "High",
        "reminder": "1 hour before"
    })
    # Create a task for tomorrow
    authenticated_client.post("/tasks/", json={
        "title": "Test Task Tomorrow",
        "description": "Test Description",
        "due_date": str(date.today() + timedelta(days=1)),
        "due_time": "23:59:59",
        "priority": "High",
        "reminder": "1 hour before"
    })
    # Create an event for today
    authenticated_client.post("/events/", json={
        "title": "Test Event Today",
        "location": "Home",
        "start_datetime": f"{date.today()}T10:00:00",
        "end_datetime": f"{date.today()}T11:00:00",
        "category": "Work",
        "notes": "Meeting",
        "reminder_minutes_before": 30
    })

    response = authenticated_client.get("/home")
    assert response.status_code == 200, response.text
    data = response.json()
    assert data["user_name"] == "testuser"
    assert data["todays_flow"] == 0  # One task, not completed
    assert len(data["upcoming_tasks"]) == 2
    assert len(data["todays_schedule"]) >= 1

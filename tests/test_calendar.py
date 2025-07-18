from fastapi.testclient import TestClient
from datetime import date, datetime

def test_get_calendar_month(authenticated_client: TestClient):
    # Create an event for the month
    authenticated_client.post("/events/", json={
        "title": "Test Event",
        "location": "Test Location",
        "start_datetime": "2025-07-15T10:00:00",
        "end_datetime": "2025-07-15T11:00:00",
        "category": "Work",
        "notes": "Test Notes",
        "reminder_minutes_before": 60
    })

    response = authenticated_client.get("/calendar/2025/7")
    assert response.status_code == 200, response.text
    data = response.json()
    assert isinstance(data, list)
    assert len(data) > 0
    assert data[0]["title"] == "Test Event"

def test_get_todays_schedule(authenticated_client: TestClient):
    # Create an event for today
    today_str = date.today().isoformat()
    authenticated_client.post("/events/", json={
        "title": "Today's Event",
        "location": "Test Location",
        "start_datetime": f"{today_str}T14:00:00",
        "end_datetime": f"{today_str}T15:00:00",
        "category": "Personal",
        "notes": "Test Notes",
        "reminder_minutes_before": 30
    })

    response = authenticated_client.get("/calendar/today")
    assert response.status_code == 200, response.text
    data = response.json()
    assert isinstance(data, list)
    assert len(data) > 0
    assert data[0]["title"] == "Today's Event"

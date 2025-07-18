

from fastapi.testclient import TestClient

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
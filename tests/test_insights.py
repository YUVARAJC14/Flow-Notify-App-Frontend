from fastapi.testclient import TestClient
from datetime import date, timedelta, datetime

def test_get_insights(authenticated_client: TestClient):
    # Create some completed tasks for testing insights
    today = date.today()
    yesterday = today - timedelta(days=1)

    authenticated_client.post("/tasks/", json={
        "title": "Completed Task Today",
        "description": "",
        "due_date": str(today),
        "due_time": "10:00:00",
        "priority": "High",
        "reminder": "1 hour before"
    })
    authenticated_client.post("/tasks/", json={
        "title": "Completed Task Yesterday",
        "description": "",
        "due_date": str(yesterday),
        "due_time": "10:00:00",
        "priority": "High",
        "reminder": "1 hour before"
    })

    # Mark tasks as completed
    tasks_response = authenticated_client.get("/tasks/")
    tasks = tasks_response.json()
    for task in tasks:
        if task["title"] == "Completed Task Today":
            authenticated_client.put(f"/tasks/{task['id']}", json={
                "title": "Completed Task Today",
                "description": "",
                "due_date": str(today),
                "due_time": "10:00:00",
                "priority": "High",
                "reminder": "1 hour before",
                "completed": True
            })
        elif task["title"] == "Completed Task Yesterday":
            authenticated_client.put(f"/tasks/{task['id']}", json={
                "title": "Completed Task Yesterday",
                "description": "",
                "due_date": str(yesterday),
                "due_time": "10:00:00",
                "priority": "High",
                "reminder": "1 hour before",
                "completed": True
            })

    response = authenticated_client.get("/api/insights?period=week")
    assert response.status_code == 200, response.text
    data = response.json()

    assert "flowScore" in data
    assert "taskCompletion" in data
    assert "productiveTimes" in data

    assert isinstance(data["flowScore"]["score"], float)
    assert isinstance(data["flowScore"]["comparison"], str)

    assert isinstance(data["taskCompletion"]["labels"], list)
    assert isinstance(data["taskCompletion"]["data"], list)

    assert isinstance(data["productiveTimes"]["data"], dict)

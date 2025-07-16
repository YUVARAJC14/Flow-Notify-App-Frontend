import pytest
from datetime import date, time, timedelta
from app.models.models import Task

@pytest.fixture(name="test_tasks")
def test_tasks_fixture(db_session, test_user):
    tasks = [
        Task(
            title="Task Today",
            description="Description for today's task",
            due_date=date.today(),
            due_time=time(10, 0, 0),
            priority="High",
            reminder="10 minutes before",
            owner_id=test_user.id,
        ),
        Task(
            title="Upcoming Task",
            description="Description for an upcoming task",
            due_date=date.today() + timedelta(days=7),
            due_time=time(14, 0, 0),
            priority="Medium",
            reminder="1 day before",
            owner_id=test_user.id,
        ),
        Task(
            title="Completed Task",
            description="Description for a completed task",
            due_date=date.today() - timedelta(days=1),
            due_time=time(9, 0, 0),
            priority="Low",
            reminder="1 hour before",
            completed=True,
            owner_id=test_user.id,
        ),
        Task(
            title="Another Task",
            description="Another description",
            due_date=date.today() + timedelta(days=1),
            due_time=time(11, 0, 0),
            priority="High",
            reminder="10 minutes before",
            owner_id=test_user.id,
        ),
    ]
    db_session.add_all(tasks)
    db_session.commit()
    for task in tasks:
        db_session.refresh(task)
    return tasks

def test_create_task(authenticated_client):
    task_data = {
        "title": "Test Task",
        "description": "This is a test description.",
        "due_date": str(date.today()),
        "due_time": "23:59:59",
        "priority": "High",
        "reminder": "10 minutes before",
    }
    response = authenticated_client.post("/tasks/", json=task_data)
    assert response.status_code == 200
    data = response.json()
    assert data["title"] == "Test Task"
    assert "id" in data
    assert "owner_id" in data

def test_create_task_unauthenticated(client):
    task_data = {
        "title": "Test Task",
        "description": "This is a test description.",
        "due_date": str(date.today()),
        "due_time": "23:59:59",
        "priority": "High",
        "reminder": "10 minutes before",
    }
    response = client.post("/tasks/", json=task_data)
    assert response.status_code == 401
    assert response.json() == {"detail": "Not authenticated"}

def test_create_task_invalid_data(authenticated_client):
    task_data = {
        "title": "Test Task",
        "due_date": "invalid-date",
        "due_time": "23:59:59",
        "priority": "High",
        "reminder": "10 minutes before",
    }
    response = authenticated_client.post("/tasks/", json=task_data)
    assert response.status_code == 422

def test_read_tasks_all(authenticated_client, test_tasks):
    response = authenticated_client.get("/tasks/")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == len(test_tasks)

def test_read_tasks_search(authenticated_client, test_tasks):
    response = authenticated_client.get("/tasks/?search=Upcoming")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 1
    assert data[0]["title"] == "Upcoming Task"

def test_read_tasks_today(authenticated_client, test_tasks):
    response = authenticated_client.get("/tasks/?date_filter=today")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 1
    assert data[0]["title"] == "Task Today"

def test_read_tasks_upcoming(authenticated_client, test_tasks):
    response = authenticated_client.get("/tasks/?date_filter=upcoming")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2
    assert data[0]["title"] == "Another Task"
    assert data[1]["title"] == "Upcoming Task"

def test_read_tasks_completed(authenticated_client, test_tasks):
    response = authenticated_client.get("/tasks/?date_filter=completed")
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 1
    assert data[0]["title"] == "Completed Task"

def test_update_task(authenticated_client, test_tasks):
    task_to_update = test_tasks[0]
    updated_data = {
        "title": "Updated Task Title",
        "description": "Updated description",
        "due_date": str(date.today()),
        "due_time": "12:00:00",
        "priority": "Medium",
        "reminder": "1 hour before",
        "completed": True,
    }
    response = authenticated_client.put(f"/tasks/{task_to_update.id}", json=updated_data)
    assert response.status_code == 200
    data = response.json()
    assert data["title"] == "Updated Task Title"
    assert data["completed"] is True

def test_update_task_not_found(authenticated_client):
    updated_data = {
        "title": "Non Existent Task",
        "description": "Description",
        "due_date": str(date.today()),
        "due_time": "12:00:00",
        "priority": "Medium",
        "reminder": "1 hour before",
    }
    response = authenticated_client.put("/tasks/999", json=updated_data)
    assert response.status_code == 404

def test_delete_task(authenticated_client, test_tasks):
    task_to_delete = test_tasks[0]
    response = authenticated_client.delete(f"/tasks/{task_to_delete.id}")
    assert response.status_code == 204

    response = authenticated_client.get("/tasks/")
    data = response.json()
    assert len(data) == len(test_tasks) - 1
    assert not any(task["id"] == task_to_delete.id for task in data)

def test_delete_task_not_found(authenticated_client):
    response = authenticated_client.delete("/tasks/999")
    assert response.status_code == 404

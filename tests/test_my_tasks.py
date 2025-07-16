
import pytest

from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.main import app
from app.database.database import Base
from app.routers.tasks import get_db
from app.models.models import User, Task
from app.schemas.schemas import UserCreate, TaskCreate
from datetime import datetime, time, timedelta

SQLALCHEMY_DATABASE_URL = "sqlite:///./test.db"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base.metadata.create_all(bind=engine)

def override_get_db():
    try:
        db = TestingSessionLocal()
        yield db
    finally:
        db.close()

app.dependency_overrides[get_db] = override_get_db

client = TestClient(app)

@pytest.fixture(scope="function")
def db_session():
    Base.metadata.create_all(bind=engine)
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()
        Base.metadata.drop_all(bind=engine)

@pytest.fixture(scope="function")
def test_user(db_session):
    user = User(username="testuser", hashed_password="testpassword")
    db_session.add(user)
    db_session.commit()
    db_session.refresh(user)
    return user

@pytest.fixture(scope="function")
def auth_cookies(test_user):
    response = client.post("/login", data={"username": test_user.username, "password": "testpassword"})
    
    return {"session": response.cookies["session"]}

def test_create_task(db_session, auth_cookies):
    response = client.post(
        "/tasks/",
        cookies=auth_cookies,
        json={
            "title": "Test Task",
            "description": "Test Description",
            "due_date": "2025-12-31",
            "due_time": "12:00:00",
            "priority": "High",
            "reminder": "10 minutes before",
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert data["title"] == "Test Task"
    assert data["description"] == "Test Description"
    assert "id" in data
    assert "owner_id" in data

def test_read_tasks(db_session, test_user, auth_cookies):
    task1 = Task(
        title="Test Task 1",
        description="Test Description 1",
        due_date=datetime.strptime("2025-12-31", "%Y-%m-%d").date(),
        due_time=time(12, 0),
        priority="High",
        reminder="10 minutes before",
        owner_id=test_user.id,
    )
    task2 = Task(
        title="Test Task 2",
        description="Test Description 2",
        due_date=datetime.strptime("2025-12-31", "%Y-%m-%d").date(),
        due_time=time(13, 0),
        priority="Medium",
        reminder="1 hour before",
        owner_id=test_user.id,
    )
    db_session.add_all([task1, task2])
    db_session.commit()

    response = client.get("/tasks/", cookies=auth_cookies)
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2
    assert data[0]["title"] == "Test Task 1"
    assert data[1]["title"] == "Test Task 2"

def test_update_task(db_session, test_user, auth_cookies):
    task = Task(
        title="Test Task",
        description="Test Description",
        due_date=datetime.strptime("2025-12-31", "%Y-%m-%d").date(),
        due_time=time(12, 0),
        priority="High",
        reminder="10 minutes before",
        owner_id=test_user.id,
    )
    db_session.add(task)
    db_session.commit()
    db_session.refresh(task)

    response = client.put(
        f"/tasks/{task.id}",
        cookies=auth_cookies,
        json={
            "title": "Updated Task",
            "description": "Updated Description",
            "due_date": "2026-01-01",
            "due_time": "13:00:00",
            "priority": "Low",
            "reminder": "1 day before",
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert data["title"] == "Updated Task"
    assert data["description"] == "Updated Description"
    assert data["due_date"] == "2026-01-01"

def test_delete_task(db_session, test_user, auth_cookies):
    task = Task(
        title="Test Task",
        description="Test Description",
        due_date=datetime.strptime("2025-12-31", "%Y-%m-%d").date(),
        due_time=time(12, 0),
        priority="High",
        reminder="10 minutes before",
        owner_id=test_user.id,
    )
    db_session.add(task)
    db_session.commit()
    db_session.refresh(task)

    response = client.delete(f"/tasks/{task.id}", cookies=auth_cookies)
    assert response.status_code == 204

    db_task = db_session.query(Task).filter(Task.id == task.id).first()
    assert db_task is None


def test_read_tasks_with_search_and_filters(db_session, test_user, auth_cookies):
    today = datetime.today().date()
    tomorrow = today + timedelta(days=1)
    yesterday = today - timedelta(days=1)

    task1 = Task(title="Today Task", description="A task for today", due_date=today, owner_id=test_user.id, completed=False)
    task2 = Task(title="Upcoming Task", description="A task for tomorrow", due_date=tomorrow, owner_id=test_user.id, completed=False)
    task3 = Task(title="Past Task", description="A task from yesterday", due_date=yesterday, owner_id=test_user.id, completed=True)
    task4 = Task(title="Another Today Task", description="Another task for today", due_date=today, owner_id=test_user.id, completed=False)

    db_session.add_all([task1, task2, task3, task4])
    db_session.commit()

    # Test search
    response = client.get("/tasks/?search=Today", cookies=auth_cookies)
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2
    assert data[0]["title"] == "Today Task"
    assert data[1]["title"] == "Another Today Task"

    # Test date_filter = "today"
    response = client.get("/tasks/?date_filter=today", cookies=auth_cookies)
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 2
    assert data[0]["title"] == "Today Task"
    assert data[1]["title"] == "Another Today Task"

    # Test date_filter = "upcoming"
    response = client.get("/tasks/?date_filter=upcoming", cookies=auth_cookies)
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 1
    assert data[0]["title"] == "Upcoming Task"

    # Test date_filter = "completed"
    response = client.get("/tasks/?date_filter=completed", cookies=auth_cookies)
    assert response.status_code == 200
    data = response.json()
    assert len(data) == 1
    assert data[0]["title"] == "Past Task"

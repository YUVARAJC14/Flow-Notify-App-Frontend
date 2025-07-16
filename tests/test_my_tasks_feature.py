import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.main import app
from app.database.database import Base
from app.models.models import User, Task, PriorityEnum, ReminderEnum
from app.security import get_password_hash
from app.routers.tasks import get_db # Import the actual get_db dependency
from app.routers.users import get_current_user # Import get_current_user for mocking

from datetime import date, time, timedelta



# Setup a test database
SQLALCHEMY_DATABASE_URL = "sqlite:///./test.db"
engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

@pytest.fixture(name="session")
def session_fixture():
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
    with TestingSessionLocal() as session:
        yield session
        Base.metadata.drop_all(bind=engine)

@pytest.fixture(name="client")
def client_fixture(session):
    def override_get_db():
        yield session

    app.dependency_overrides[get_db] = override_get_db
    with TestClient(app=app) as client:
        yield client
    app.dependency_overrides.clear()

@pytest.fixture
def test_user(session):
    hashed_password = get_password_hash("testpassword")
    user = User(username="testuser", hashed_password=hashed_password)
    session.add(user)
    session.commit()
    session.refresh(user)
    return user

@pytest.fixture
def authenticated_client(client, test_user):
    app.dependency_overrides[get_current_user] = lambda: test_user
    return client

def create_task(session, user_id, title, description, due_date, completed=False):
    task = Task(
        title=title,
        description=description,
        due_date=due_date,
        due_time=time(10, 0),
        priority=PriorityEnum.medium,
        reminder=ReminderEnum.one_hour,
        completed=completed,
        owner_id=user_id,
    )
    session.add(task)
    session.commit()
    session.refresh(task)
    return task

def test_search_tasks(authenticated_client, session, test_user):
    create_task(session, test_user.id, "Buy groceries", "Milk, eggs, bread", date.today())
    create_task(session, test_user.id, "Clean house", "Vacuum and dust", date.today())
    create_task(session, test_user.id, "Pay bills", "Electricity and internet", date.today())

    response = authenticated_client.get("/tasks/", params={"search": "groceries"})
    assert response.status_code == 200
    tasks = response.json()
    assert len(tasks) == 1
    assert tasks[0]["title"] == "Buy groceries"

    response = authenticated_client.get("/tasks/", params={"search": "dust"})
    assert response.status_code == 200
    tasks = response.json()
    assert len(tasks) == 1
    assert tasks[0]["title"] == "Clean house"

    response = authenticated_client.get("/tasks/", params={"search": "nonexistent"})
    assert response.status_code == 200
    tasks = response.json()
    assert len(tasks) == 0

def test_filter_tasks_today(authenticated_client, session, test_user):
    today = date.today()
    tomorrow = today + timedelta(days=1)
    yesterday = today - timedelta(days=1)

    create_task(session, test_user.id, "Today's task 1", "Description 1", today)
    create_task(session, test_user.id, "Today's task 2", "Description 2", today)
    create_task(session, test_user.id, "Tomorrow's task", "Description 3", tomorrow)
    create_task(session, test_user.id, "Yesterday's task", "Description 4", yesterday)

    response = authenticated_client.get("/tasks/", params={"date_filter": "today"})
    assert response.status_code == 200
    tasks = response.json()
    assert len(tasks) == 2
    assert all(task["due_date"] == today.isoformat() for task in tasks)
    assert tasks[0]["title"] == "Today's task 1" # Ordered by due_date, then by creation order (implicit)

def test_filter_tasks_upcoming(authenticated_client, session, test_user):
    today = date.today()
    tomorrow = today + timedelta(days=1)
    next_week = today + timedelta(days=7)
    yesterday = today - timedelta(days=1)

    create_task(session, test_user.id, "Today's task", "Description 1", today)
    create_task(session, test_user.id, "Upcoming task 1", "Description 2", tomorrow)
    create_task(session, test_user.id, "Upcoming task 2", "Description 3", next_week)
    create_task(session, test_user.id, "Completed upcoming task", "Description 4", tomorrow, completed=True)
    create_task(session, test_user.id, "Past task", "Description 5", yesterday)

    response = authenticated_client.get("/tasks/", params={"date_filter": "upcoming"})
    assert response.status_code == 200
    tasks = response.json()
    assert len(tasks) == 2
    assert all(task["due_date"] > today.isoformat() and not task["completed"] for task in tasks)
    assert tasks[0]["title"] == "Upcoming task 1" # Ordered by due_date
    assert tasks[1]["title"] == "Upcoming task 2"

def test_filter_tasks_completed(authenticated_client, session, test_user):
    today = date.today()
    create_task(session, test_user.id, "Completed task 1", "Description 1", today, completed=True)
    create_task(session, test_user.id, "Completed task 2", "Description 2", today - timedelta(days=1), completed=True)
    create_task(session, test_user.id, "Incomplete task", "Description 3", today, completed=False)

    response = authenticated_client.get("/tasks/", params={"date_filter": "completed"})
    assert response.status_code == 200
    tasks = response.json()
    assert len(tasks) == 2
    assert all(task["completed"] for task in tasks)
    assert tasks[0]["title"] == "Completed task 2" # Ordered by due_date
    assert tasks[1]["title"] == "Completed task 1"

def test_filter_tasks_all(authenticated_client, session, test_user):
    today = date.today()
    create_task(session, test_user.id, "Task A", "Desc A", today)
    create_task(session, test_user.id, "Task B", "Desc B", today + timedelta(days=1))
    create_task(session, test_user.id, "Task C", "Desc C", today - timedelta(days=1))

    response = authenticated_client.get("/tasks/", params={"date_filter": "all"})
    assert response.status_code == 200
    tasks = response.json()
    assert len(tasks) == 3
    # Verify ordering by due_date
    assert tasks[0]["title"] == "Task C"
    assert tasks[1]["title"] == "Task A"
    assert tasks[2]["title"] == "Task B"

def test_tasks_ordered_by_date(authenticated_client, session, test_user):
    today = date.today()
    tomorrow = today + timedelta(days=1)
    yesterday = today - timedelta(days=1)

    create_task(session, test_user.id, "Task for tomorrow", "Desc", tomorrow)
    create_task(session, test_user.id, "Task for today", "Desc", today)
    create_task(session, test_user.id, "Task for yesterday", "Desc", yesterday)

    response = authenticated_client.get("/tasks/") # Default filter is "all"
    assert response.status_code == 200
    tasks = response.json()
    assert len(tasks) == 3
    assert tasks[0]["due_date"] == yesterday.isoformat()
    assert tasks[1]["due_date"] == today.isoformat()
    assert tasks[2]["due_date"] == tomorrow.isoformat()
    assert tasks[0]["title"] == "Task for yesterday"
    assert tasks[1]["title"] == "Task for today"
    assert tasks[2]["title"] == "Task for tomorrow"

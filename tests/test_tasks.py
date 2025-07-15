import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.main import app
from app.database.database import Base
from app.models.models import User, Task
from app.security import get_password_hash
from app.routers.users import get_db

# Setup a test database
SQLALCHEMY_DATABASE_URL = "sqlite:///./test.db"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


@pytest.fixture(name="db_session")
def db_session_fixture():
    Base.metadata.create_all(bind=engine)
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()
        Base.metadata.drop_all(bind=engine)


@pytest.fixture(name="client")
def client_fixture(db_session):
    def override_get_db():
        try:
            yield db_session
        finally:
            db_session.close()

    app.dependency_overrides[get_db] = override_get_db
    yield TestClient(app)
    app.dependency_overrides.clear()


@pytest.fixture(name="test_user")
def test_user_fixture(db_session):
    hashed_password = get_password_hash("testpassword")
    user = User(username="testuser", hashed_password=hashed_password)
    db_session.add(user)
    db_session.commit()
    db_session.refresh(user)
    return user


@pytest.fixture(name="authenticated_client")
def authenticated_client_fixture(client, test_user):
    # Simulate login by setting a session cookie
    response = client.post(
        "/login", data={"username": "testuser", "password": "testpassword"}
    )
    assert response.status_code == 200
    session_cookie = response.cookies.get("session")
    client.cookies["session"] = session_cookie
    return client


def test_create_task(authenticated_client):
    task_data = {
        "title": "Test Task",
        "description": "This is a test description.",
        "due_date": "2025-12-31",
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
        "due_date": "2025-12-31",
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
        "due_date": "invalid-date",  # Invalid date format
        "due_time": "23:59:59",
        "priority": "High",
        "reminder": "10 minutes before",
    }
    response = authenticated_client.post("/tasks/", json=task_data)
    assert response.status_code == 422  # Unprocessable Entity

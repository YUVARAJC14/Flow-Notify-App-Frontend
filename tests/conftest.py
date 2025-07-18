import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from fastapi.testclient import TestClient
from datetime import date, time, timedelta
import importlib

from app.main import create_app
from app.database.base import Base
from app.routers import users, tasks, events, home
from app import crud, schemas
from app.security import get_password_hash

SQLALCHEMY_DATABASE_URL = "sqlite:///:memory:"

@pytest.fixture(scope="session")
def engine():
    return create_engine(SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False})

@pytest.fixture(scope="function")
def db_session(engine):
    """Returns an sqlalchemy session, and after the test tears down everything properly."""
    # Ensure models are loaded and metadata is fresh
    from app.database import base
    importlib.reload(base)
    from app.models import models
    importlib.reload(models)

    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
    connection = engine.connect()
    # begin the nested transaction
    transaction = connection.begin()
    # use the connection with the already started transaction
    session = sessionmaker(autocommit=False, autoflush=False, bind=connection)()

    yield session

    session.close()
    # roll back the broader transaction
    transaction.rollback()
    # put back the connection to the connection pool
    connection.close()


@pytest.fixture(scope="function")
def client(db_session):
    app = create_app()
    app.dependency_overrides[users.get_db] = lambda: db_session
    app.dependency_overrides[tasks.get_db] = lambda: db_session
    app.dependency_overrides[events.get_db] = lambda: db_session
    app.dependency_overrides[home.get_db] = lambda: db_session
    with TestClient(app) as c:
        yield c

@pytest.fixture(scope="function")
def test_user(db_session):
    user_data = schemas.UserCreate(username="testuser", password="testpassword")
    user = crud.create_user(db=db_session, user=user_data)
    return user

@pytest.fixture(scope="function")
def authenticated_client(client, test_user):
    client.post("/login", data={"username": "testuser", "password": "testpassword"})
    return client
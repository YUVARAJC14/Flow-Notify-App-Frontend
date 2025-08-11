import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.main import create_app
from app.models import models # Ensure models are imported to register with Base.metadata
from app.database.base import Base
from app.database.database import get_db

@pytest.fixture(scope="function")
def fastapi_app():
    # Create a new in-memory SQLite engine for each test function
    engine = create_engine("sqlite:///:memory:", connect_args={"check_same_thread": False})
    TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

    app = create_app()
    # The override_get_db needs to be a closure to access TestingSessionLocal
    def override_get_db():
        db = TestingSessionLocal()
        try:
            yield db
        finally:
            db.close()

    app.dependency_overrides[get_db] = override_get_db
    
    # Create tables after app and models are loaded
    Base.metadata.create_all(bind=engine)
    
    yield app
    
    # Drop tables after tests are done
    Base.metadata.drop_all(bind=engine)

@pytest.fixture(scope="function")
def client(fastapi_app):
    # The client now implicitly uses the db_session through the app's dependency override
    with TestClient(fastapi_app) as c:
        yield c

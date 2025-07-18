from fastapi.testclient import TestClient
from sqlalchemy.orm import Session
from app import crud, schemas
from app.security import verify_password

def test_get_user_profile(authenticated_client: TestClient):
    response = authenticated_client.get("/profile")
    assert response.status_code == 200, response.text
    data = response.json()
    assert data["username"] == "testuser"
    assert "email" in data

def test_update_user_profile(authenticated_client: TestClient):
    response = authenticated_client.put("/profile", json={
        "email": "new_email@example.com",
        "profile_picture_url": "http://example.com/pic.jpg"
    })
    assert response.status_code == 200, response.text
    data = response.json()
    assert data["email"] == "new_email@example.com"
    assert data["profile_picture_url"] == "http://example.com/pic.jpg"

def test_change_password(authenticated_client: TestClient, db_session: Session):
    # First, get the current user to verify password later
    user_response = authenticated_client.get("/users/me")
    user_data = user_response.json()
    user_id = user_data["id"]

    response = authenticated_client.put("/profile/password", json={
        "current_password": "testpassword",
        "new_password": "new_testpassword"
    })
    assert response.status_code == 204, response.text

    # Verify password change by trying to log in with new password
    login_response = authenticated_client.post("/login", data={
        "username": "testuser",
        "password": "new_testpassword"
    })
    assert login_response.status_code == 200, login_response.text
    assert login_response.json()["message"] == "Login successful"

def test_update_app_settings(authenticated_client: TestClient):
    response = authenticated_client.put("/profile/settings", json={
        "theme": "dark",
        "language": "es"
    })
    assert response.status_code == 200, response.text
    data = response.json()
    assert data["theme"] == "dark"
    assert data["language"] == "es"

def test_update_notification_settings(authenticated_client: TestClient):
    response = authenticated_client.put("/profile/notifications", json={
        "email_notifications_enabled": False,
        "push_notifications_enabled": False
    })
    assert response.status_code == 200, response.text
    data = response.json()
    assert data["email_notifications_enabled"] == False
    assert data["push_notifications_enabled"] == False

def test_get_app_info(client: TestClient):
    response = client.get("/app-info")
    assert response.status_code == 200, response.text
    data = response.json()
    assert "app_version" in data
    assert "privacy_policy_url" in data
    assert "terms_of_service_url" in data
    assert "help_center_url" in data

def test_submit_feedback(authenticated_client: TestClient):
    response = authenticated_client.post("/feedback", json={"message": "This is a test feedback."})
    assert response.status_code == 202, response.text
    data = response.json()
    assert data["message"] == "Feedback submitted successfully"

def test_logout(authenticated_client: TestClient):
    response = authenticated_client.post("/logout")
    assert response.status_code == 200, response.text
    data = response.json()
    assert data["message"] == "Logout successful"

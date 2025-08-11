from fastapi.testclient import TestClient

def test_register(client: TestClient):
    response = client.post("/api/v1/auth/register", json={"fullName": "Test User", "email": "test@example.com", "password": "testpassword"})
    assert response.status_code == 201
    assert response.json() == {"message": "Registration successful. Please check your email to verify your account."}

def test_register_duplicate_email(client: TestClient):
    client.post("/api/v1/auth/register", json={"fullName": "Test User", "email": "test@example.com", "password": "testpassword"})
    response = client.post("/api/v1/auth/register", json={"fullName": "Test User", "email": "test@example.com", "password": "testpassword"})
    assert response.status_code == 400
    assert response.json() == {"detail": "Email already registered"}

def test_login(client: TestClient):
    client.post("/api/v1/auth/register", json={"fullName": "Test User", "email": "test@example.com", "password": "testpassword"})
    response = client.post("/api/v1/auth/login", json={"emailOrUsername": "test@example.com", "password": "testpassword"})
    assert response.status_code == 200
    data = response.json()
    assert "accessToken" in data
    assert "refreshToken" in data
    assert data["user"]["email"] == "test@example.com"

def test_login_incorrect_password(client: TestClient):
    client.post("/api/v1/auth/register", json={"fullName": "Test User", "email": "test@example.com", "password": "testpassword"})
    response = client.post("/api/v1/auth/login", json={"emailOrUsername": "test@example.com", "password": "wrongpassword"})
    assert response.status_code == 401
    assert response.json() == {"detail": "Incorrect email or password"}

def test_forgot_password(client: TestClient):
    client.post("/api/v1/auth/register", json={"fullName": "Test User", "email": "test@example.com", "password": "testpassword"})
    response = client.post("/api/v1/auth/forgot-password", json={"email": "test@example.com"})
    assert response.status_code == 200
    assert response.json() == {"message": "If an account with that email exists, a password reset link has been sent."}
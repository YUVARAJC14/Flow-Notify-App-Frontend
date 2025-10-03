This document provides a detailed breakdown of the `app` directory, which constitutes the core of the Flow Notify FastAPI application.

## Directory Structure

```
app/
├── __init__.py
├── main.py
├── config.py
├── crud.py
├── auth_utils.py
├── security.py
├── database/
│   ├── __init__.py
│   ├── database.py
│   └── base.py
├── models/
│   ├── __init__.py
│   └── models.py
├── routers/
│   ├── __init__.py
│   ├── auth.py
│   ├── dashboard.py
│   ├── events.py
│   ├── insights.py
│   ├── profile.py
│   ├── tasks.py
│   └── users.py
├── schemas/
│   ├── __init__.py
│   ├── schemas.py
│   ├── dashboard.py
│   ├── home.py
│   ├── insights.py
│   ├── notification.py
│   ├── nudge.py
│   └── scheduling.py
└── services/
    ├── __init__.py
    ├── goal_service.py
    ├── insights_service.py
    ├── integration_service.py
    ├── nlp_service.py
    ├── notification_timing_service.py
    ├── nudge_service.py
    ├── recurrence_service.py
    └── scheduling_service.py
```

---

### Core Application Files

These files are at the root of the `app` package and control the application's setup and core functionalities.

-   **`main.py`**: This is the main entry point for the FastAPI application. It initializes the `FastAPI` app, sets up metadata (title, version), and includes all the API routers from the `app.routers` module. It also defines a `lifespan` context manager to initialize the database when the application starts.

-   **`config.py`**: This file manages application configuration. It loads sensitive data and settings, such as the `SECRET_KEY` and `ALGORITHM` for JWT encoding, from environment variables, providing default values for development.

-   **`crud.py`**: This module contains all the **C**reate, **R**ead, **U**pdate, **D**elete (CRUD) functions that interact directly with the database. It uses SQLAlchemy sessions and Pydantic schemas to perform operations on the data models. This abstracts the database logic from the API endpoints.

-   **`auth_utils.py`**: A utility module for authentication-related tasks. It contains functions for hashing new passwords (`get_password_hash`) and verifying existing ones (`verify_password`) using `passlib`.

-   **`security.py`**: This module handles the security aspects of the application. It is responsible for:
    -   Creating JWT access and refresh tokens (`create_access_token`, `create_refresh_token`).
    -   Verifying JWTs to authenticate users (`verify_token`).
    -   Providing a `get_current_user` dependency that API endpoints can use to require authentication and retrieve the currently logged-in user's data.

---

### `database/`

This sub-package is responsible for all database connections and session management.

-   **`database.py`**: Sets up the database connection using SQLAlchemy. It defines the `SQLALCHEMY_DATABASE_URL`, creates the `engine`, and provides a `SessionLocal` class to create database sessions. The `get_db` function is a FastAPI dependency that provides a database session to API routes and ensures the session is closed after the request is complete. `init_db` creates the database tables based on the defined models.

-   **`base.py`**: Contains the `declarative_base()` which is used to create the base class for all SQLAlchemy ORM models.

---

### `models/`

This sub-package defines the structure of the database tables using SQLAlchemy's ORM.

-   **`models.py`**: Contains all the SQLAlchemy models, which are Python classes that map to database tables. Key models include:
    -   `User`: Represents user accounts and their settings.
    -   `Task`: Represents user tasks, including properties like due date, priority, and completion status. Supports sub-tasks and recurrence.
    -   `Event`: Represents calendar events with start/end times, location, and category.
    -   `Goal`: Represents user goals, which can be linked to tasks.
    -   `TokenBlocklist`: Stores revoked JWTs to prevent their reuse after logout.
    -   `PasswordResetToken`: Stores tokens for the password reset functionality.

---

### `routers/`

This sub-package contains the API endpoints, organized into logical groups using FastAPI's `APIRouter`.

-   **`auth.py`**: Defines authentication-related endpoints:
    -   `POST /api/v1/auth/register`: Creates a new user.
    -   `POST /api/v1/auth/login`: Authenticates a user and returns JWTs.
    -   `POST /api/v1/auth/forgot-password`: Initiates the password reset process.

-   **`dashboard.py`**: Provides the endpoint for the main dashboard screen.
    -   `GET /api/v1/dashboard`: Fetches aggregated data like today's progress, upcoming tasks, and today's schedule.

-   **`events.py`**: Defines CRUD endpoints for managing calendar events (`/api/v1/events`).

-   **`insights.py`**: Provides the endpoint for user analytics.
    -   `GET /api/v1/insights`: Returns calculated data like flow score, task completion rates, and productive times.

-   **`profile.py`**: Defines endpoints for managing the user's profile (`/api/v1/profile`). This includes fetching, updating, and changing the password.

-   **`tasks.py`**: Defines CRUD endpoints for managing tasks (`/api/v1/tasks`), including filtering and status updates.

-   **`users.py`**: Contains endpoints for user-specific information, such as `/users/me` to get the current user's details.

---

### `schemas/`

This sub-package contains Pydantic models, which are used for data validation, serialization, and documentation.

-   **`schemas.py`**: The main schema file, defining the primary data shapes for API requests and responses. It includes models like `UserCreate`, `TaskCreate`, `EventCreate`, and their corresponding response models. It also defines shared `Enum` types.

-   **`dashboard.py`**: Contains Pydantic models specifically for the complex data structure returned by the `/dashboard` endpoint.

-   **`home.py`**: Defines Pydantic models for the home page.

-   **`insights.py`**: Defines the data structures for the `/insights` endpoint, such as `FlowScore` and `TaskCompletion`.

-   **`notification.py`**: Contains Pydantic models related to notification content and timing suggestions.

-   **`nudge.py`**: Defines the data structures for proactive "nudges" sent to the user.

-   **`scheduling.py`**: Contains Pydantic models for scheduling suggestions and requests.

---

### `services/`

This sub-package holds the business logic that is too complex to be placed directly in the `crud.py` or `routers/` modules. It often involves coordinating multiple data sources, interacting with external APIs, or running machine learning models.

-   **`goal_service.py`**: Encapsulates logic for managing goals, including updating progress based on completed tasks and suggesting new tasks for a goal (with a placeholder for an ML model).

-   **`insights_service.py`**: Responsible for calculating and generating the analytics data served by the `/insights` endpoint. It uses placeholder logic to simulate ML model predictions.

-   **`integration_service.py`**: Manages connections to external services. The current implementation simulates connecting to and syncing with Google Calendar.

-   **`nlp_service.py`**: Provides Natural Language Processing capabilities. The `parse_natural_language_task` function can extract task details (description, due date, time) from a plain text string.

-   **`notification_timing_service.py`**: A service designed to determine the best time to send a notification to a user, based on their habits, calendar, and focus hours (simulated).

-   **`nudge_service.py`**: Generates proactive "nudges" or suggestions for the user, such as reminding them to prepare for an upcoming meeting.

-   **`recurrence_service.py`**: Handles the logic for recurring tasks and events, generating future instances based on an RRULE string.

-   **`scheduling_service.py`**: Suggests optimal time slots for new tasks by analyzing the user's calendar availability and work habits (simulated).
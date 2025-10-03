Flow Notify" app, here is a detailed breakdown of the necessary API routes. These routes follow RESTful principles and are designed to support the functionality shown.
Flow Notify is a FastAPI sqlite sqlalchemy project. 


## Project Structure
There are many ways to structure a project, but the best structure is one that is consistent, straightforward, and free of surprises.

Many example projects and tutorials divide the project by file type (e.g., crud, routers, models), which works well for microservices or projects with fewer scopes. However, this approach didn't fit our monolith with many domains and modules.

The structure I found more scalable and evolvable for these cases is inspired by Netflix's [Dispatch](https://github.com/Netflix/dispatch), with some minor modifications.
```
fastapi-project
├── alembic/
├── src
│   ├── auth
│   │   ├── router.py
│   │   ├── schemas.py  # pydantic models
│   │   ├── models.py  # db models
│   │   ├── dependencies.py
│   │   ├── config.py  # local configs
│   │   ├── constants.py
│   │   ├── exceptions.py
│   │   ├── service.py
│   │   └── utils.py
│   ├── aws
│   │   ├── client.py  # client model for external service communication
│   │   ├── schemas.py
│   │   ├── config.py
│   │   ├── constants.py
│   │   ├── exceptions.py
│   │   └── utils.py
│   └── posts
│   │   ├── router.py
│   │   ├── schemas.py
│   │   ├── models.py
│   │   ├── dependencies.py
│   │   ├── constants.py
│   │   ├── exceptions.py
│   │   ├── service.py
│   │   └── utils.py
│   ├── config.py  # global configs
│   ├── models.py  # global models
│   ├── exceptions.py  # global exceptions
│   ├── pagination.py  # global module e.g. pagination
│   ├── database.py  # db connection related stuff
│   └── main.py
├── tests/
│   ├── auth
│   ├── aws
│   └── posts
├── templates/
│   └── index.html
├── requirements
│   ├── base.txt
│   ├── dev.txt
│   └── prod.txt
├── .env
├── .gitignore
├── logging.ini
└── alembic.ini
```
1. Store all domain directories inside `src` folder
   1. `src/` - highest level of an app, contains common models, configs, and constants, etc.
   2. `src/main.py` - root of the project, which inits the FastAPI app
2. Each package has its own router, schemas, models, etc.
   1. `router.py` - is a core of each module with all the endpoints
   2. `schemas.py` - for pydantic models
   3. `models.py` - for db models
   4. `service.py` - module specific business logic  
   5. `dependencies.py` - router dependencies
   6. `constants.py` - module specific constants and error codes
   7. `config.py` - e.g. env vars
   8. `utils.py` - non-business logic functions, e.g. response normalization, data enrichment, etc.
   9. `exceptions.py` - module specific exceptions, e.g. `PostNotFound`, `InvalidUserData`
3. When package requires services or dependencies or constants from other packages - import them with an explicit module name
```python
from src.auth import constants as auth_constants
from src.notifications import service as notification_service
from src.posts.constants import ErrorCode as PostsErrorCode  # in case we have Standard ErrorCode in constants module of each package
```
## Pydantic
### Excessively use Pydantic
Pydantic has a rich set of features to validate and transform data. 

In addition to regular features like required & non-required fields with default values, 
Pydantic has built-in comprehensive data processing tools like regex, enums, strings manipulation, emails validation, etc.
```python
from enum import Enum
from pydantic import AnyUrl, BaseModel, EmailStr, Field


class MusicBand(str, Enum):
   AEROSMITH = "AEROSMITH"
   QUEEN = "QUEEN"
   ACDC = "AC/DC"


class UserBase(BaseModel):
    first_name: str = Field(min_length=1, max_length=128)
    username: str = Field(min_length=1, max_length=128, pattern="^[A-Za-z0-9-_]+$")
    email: EmailStr
    age: int = Field(ge=18, default=None)  # must be greater or equal to 18
    favorite_band: MusicBand | None = None  # only "AEROSMITH", "QUEEN", "AC/DC" values are allowed to be inputted
    website: AnyUrl | None = None
```
### Custom Base Model
Having a controllable global base model allows us to customize all the models within the app. For instance, we can enforce a standard datetime format or introduce a common method for all subclasses of the base model.
```python
from datetime import datetime
from zoneinfo import ZoneInfo

from fastapi.encoders import jsonable_encoder
from pydantic import BaseModel, ConfigDict


def datetime_to_gmt_str(dt: datetime) -> str:
    if not dt.tzinfo:
        dt = dt.replace(tzinfo=ZoneInfo("UTC"))

    return dt.strftime("%Y-%m-%dT%H:%M:%S%z")


class CustomModel(BaseModel):
    model_config = ConfigDict(
        json_encoders={datetime: datetime_to_gmt_str},
        populate_by_name=True,
    )

    def serializable_dict(self, **kwargs):
        """Return a dict which contains only serializable fields."""
        default_dict = self.model_dump()

        return jsonable_encoder(default_dict)


```
In the example above, we have decided to create a global base model that:
- Serializes all datetime fields to a standard format with an explicit timezone
- Provides a method to return a dict with only serializable fields
### Decouple Pydantic BaseSettings
BaseSettings was a great innovation for reading environment variables, but having a single BaseSettings for the whole app can become messy over time. To improve maintainability and organization, we have split the BaseSettings across different modules and domains.
```python
# src.auth.config
from datetime import timedelta

from pydantic_settings import BaseSettings


class AuthConfig(BaseSettings):
    JWT_ALG: str
    JWT_SECRET: str
    JWT_EXP: int = 5  # minutes

    REFRESH_TOKEN_KEY: str
    REFRESH_TOKEN_EXP: timedelta = timedelta(days=30)

    SECURE_COOKIES: bool = True


auth_settings = AuthConfig()


# src.config
from pydantic import PostgresDsn, RedisDsn, model_validator
from pydantic_settings import BaseSettings

from src.constants import Environment


class Config(BaseSettings):
    DATABASE_URL: PostgresDsn
    REDIS_URL: RedisDsn

    SITE_DOMAIN: str = "myapp.com"

    ENVIRONMENT: Environment = Environment.PRODUCTION

    SENTRY_DSN: str | None = None

    CORS_ORIGINS: list[str]
    CORS_ORIGINS_REGEX: str | None = None
    CORS_HEADERS: list[str]

    APP_VERSION: str = "1.0"


settings = Config()

```
### Docs
```
2. Help FastAPI to generate an easy-to-understand docs
   1. Set `response_model`, `status_code`, `description`, etc.
   2. If models and statuses vary, use `responses` route attribute to add docs for different responses
```python
from fastapi import APIRouter, status

router = APIRouter()

@router.post(
    "/endpoints",
    response_model=DefaultResponseModel,  # default response pydantic model 
    status_code=status.HTTP_201_CREATED,  # default status code
    description="Description of the well documented endpoint",
    tags=["Endpoint Category"],
    summary="Summary of the Endpoint",
    responses={
        status.HTTP_200_OK: {
            "model": OkResponse, # custom pydantic model for 200 response
            "description": "Ok Response",
        },
        status.HTTP_201_CREATED: {
            "model": CreatedResponse,  # custom pydantic model for 201 response
            "description": "Creates something from user request",
        },
        status.HTTP_202_ACCEPTED: {
            "model": AcceptedResponse,  # custom pydantic model for 202 response
            "description": "Accepts request and handles it later",
        },
    },
)
async def documented_route():
    pass
```
Will generate docs like this:
![FastAPI Generated Custom Response Docs](images/custom_responses.png "Custom Response Docs")

### Set DB keys naming conventions
Explicitly setting the indexes' namings according to your database's convention is preferable over sqlalchemy's. 
```python
from sqlalchemy import MetaData

POSTGRES_INDEXES_NAMING_CONVENTION = {
    "ix": "%(column_0_label)s_idx",
    "uq": "%(table_name)s_%(column_0_name)s_key",
    "ck": "%(table_name)s_%(constraint_name)s_check",
    "fk": "%(table_name)s_%(column_0_name)s_fkey",
    "pk": "%(table_name)s_pkey",
}
metadata = MetaData(naming_convention=POSTGRES_INDEXES_NAMING_CONVENTION)
```

### 1. Authentication & Onboarding (Screens 1-5)

These routes handle user registration, login, password reset, and initial setup.

#### `POST /api/auth/register` (for Screen 4: Create Account)
*   **Description:** Creates a new user account.
*   **Request Body:**
    ```json
    {
      "fullName": "John Smith",
      "email": "john.smith@example.com",
      "password": "Password123!"
    }
    ```
*   **Success Response (201 Created):**
    ```json
    {
      "message": "Registration successful. Please check your email to verify your account.",
      "user": {
        "id": "user-uuid-123",
        "email": "john.smith@example.com"
      }
    }
    ```

#### `POST /api/auth/login` (for Screen 1: Sign In)
*   **Description:** Authenticates a user and returns access tokens.
*   **Request Body(FORM DATA):**
    ```json
    {
      "username": "john.smith@example.com",
      "password": "Password123!"
    }
    ```
*   **Success Response (200 OK):**
    ```json
    {
      "accessToken": "ey...",
      "refreshToken": "ey...",
      "user": {
        "id": "user-uuid-123",
        "name": "John Smith",
        "email": "john.smith@example.com"
      }
    }
    ```

#### `POST /api/auth/forgot-password` (for Screen 2: Reset Password)
*   **Description:** Sends a password reset link to the user's email.
*   **Request Body:**
    ```json
    {
      "email": "john.smith@example.com"
    }
    ```
*   **Success Response (200 OK):**
    ```json
    { "message": "If an account with that email exists, a password reset link has been sent." }
    ```

#### `POST /api/auth/reset-password` (Used by the link sent to email)
*   **Description:** Resets the user's password using a token from the reset link.
*   **Request Body:**
    ```json
    {
      "token": "reset-token-from-email-link",
      "newPassword": "NewSecurePassword123!"
    }
    ```
*   **Success Response (200 OK):**
    ```json
    { "message": "Password has been reset successfully." }
    ```

#### `POST /api/auth/resend-verification` (for Screen 3: Check Your Email)
*   **Description:** Resends the account verification email.
*   **Request Body:**
    ```json
    {
      "email": "j**@gmail.com" // Client sends the email it has stored
    }
    ```
*   **Success Response (200 OK):**
    ```json
    { "message": "Verification email resent successfully." }
    ```

#### `PATCH /api/users/me/profile` (for Screen 5: What's your name?)
*   **Description:** An onboarding step to set the user's name after registration. This is an update to the newly created user profile.
*   **Authentication:** Required.
*   **Request Body:**
    ```json
    {
      "name": "Sarah"
    }
    ```
*   **Success Response (200 OK):**
    ```json
    {
      "id": "user-uuid-123",
      "name": "Sarah",
      "email": "sarah.davis@example.com"
    }
    ```

---

### 2. Home Dashboard (Screen 6)

#### `GET /api/dashboard/summary`
*   **Description:** Fetches all aggregated data needed for the main dashboard screen to minimize network requests.
*   **Authentication:** Required.
*   **Success Response (200 OK):**
    ```json
    {
      "todaysFlow": {
        "percentage": 82
      },
      "upcomingTasks": [
        { "id": "task-1", "title": "Team Meeting", "time": "10:00 AM", "priority": "high" },
        { "id": "task-2", "title": "Project Review", "time": "2:30 PM", "priority": "medium" },
        { "id": "task-3", "title": "Daily Report", "time": "4:00 PM", "priority": "low" }
      ],
      "todaysSchedule": [
        { "id": "event-1", "title": "Morning Standup", "time": "09:00 AM", "location": "Meeting Room 1" }
      ]
    }
    ```

---

### 3. Task Management (Screens 7-8)

#### `GET /api/tasks`
*   **Description:** Fetches a list of tasks, with optional filtering.
*   **Authentication:** Required.
*   **Query Parameters:**
    *   `filter` (optional): `all`, `today`, `upcoming`, `completed`.
    *   `search` (optional): `string` to search task titles.
*   **Success Response (200 OK):** The response is structured to make it easy for the UI to render sections.
    ```json
    {
      "today": [
        { "id": "task-4", "title": "Team meeting preparation", "time": "10:00 AM", "priority": "high", "isCompleted": false },
        { "id": "task-5", "title": "Review project proposal", "time": "2:00 PM", "priority": "medium", "isCompleted": true }
      ],
      "tomorrow": [
         { "id": "task-6", "title": "Client presentation", "time": "9:30 AM", "priority": "high", "isCompleted": false }
      ],
      "nextWeek": [
         { "id": "task-7", "title": "Quarterly planning", "time": "Mon, 10:00 AM", "priority": "medium", "isCompleted": false }
      ]
    }
    ```

#### `POST /api/tasks` (for Screen 8: New Task)
*   **Description:** Creates a new task.
*   **Authentication:** Required.
*   **Request Body:**
    ```json
    {
      "title": "Finalize Q3 Budget",
      "description": "Review expenses and finalize the budget report.",
      "dueDate": "2024-09-20",
      "dueTime": "17:00",
      "priority": "high", // 'high', 'medium', 'low'
      "reminders": [10, 60, 1440] // in minutes before
    }
    ```
*   **Success Response (201 Created):** Returns the newly created task object.

#### `PATCH /api/tasks/:taskId`
*   **Description:** Updates an existing task (e.g., marking as complete, changing priority).
*   **Authentication:** Required.
*   **Request Body:** Can include any field to update.
    ```json
    {
      "isCompleted": true
    }
    ```
*   **Success Response (200 OK):** Returns the fully updated task object.

#### `DELETE /api/tasks/:taskId`
*   **Description:** Deletes a task.
*   **Authentication:** Required.
*   **Success Response (204 No Content):**

---

### 4. Calendar & Events (Screens 9-10)

#### `GET /api/events`
*   **Description:** Fetches events for a given time range (e.g., a month for the calendar view or a single day for "Today's Schedule").
*   **Authentication:** Required.
*   **Query Parameters:**
    *   `startDate`: `YYYY-MM-DD`
    *   `endDate`: `YYYY-MM-DD`
*   **Success Response (200 OK):**
    ```json
    [
      { "id": "event-1", "title": "Team Sync", "date": "2023-09-15", "startTime": "09:00", "endTime": "10:30", "location": "Conference Room A", "category": "work" },
      { "id": "event-2", "title": "Dentist Appointment", "date": "2023-09-15", "startTime": "14:00", "endTime": "15:00", "location": "Downtown Clinic", "category": "health" }
    ]
    ```

#### `POST /api/events` (for Screen 10: New Event)
*   **Description:** Creates a new calendar event.
*   **Authentication:** Required.
*   **Request Body:**
    ```json
    {
      "title": "Design Sprint Kickoff",
      "location": "Virtual / Zoom",
      "date": "2024-02-15",
      "startTime": "10:00",
      "endTime": "11:00",
      "category": "work",
      "notes": "Discuss project goals and timeline.",
      "reminder": 15 // in minutes before
    }
    ```
*   **Success Response (201 Created):** Returns the newly created event object.

---

### 5. Insights (Screen 11)

#### `GET /api/insights`
*   **Description:** Fetches analytical data for the insights page.
*   **Authentication:** Required.
*   **Query Parameters:**
    *   `period`: `day`, `week`, `month`, `year`. Defaults to `week`.
*   **Success Response (200 OK):**
    ```json
    {
      "flowScore": {
        "score": 82,
        "comparison": {
          "change": 12, // can be negative
          "period": "last week"
        }
      },
      "taskCompletion": [
        { "label": "Mon", "completed": 8, "total": 11 },
        { "label": "Tue", "completed": 7, "total": 9 },
        // ...and so on for the period
      ],
      "productiveTimes": [
        // Data structure for the heatmap, could be an array of objects
        // { day: 0-6 (Sun-Sat), hour: 0-23, intensity: 0-1 }
        { "day": 1, "hour": 9, "intensity": 0.8 },
        { "day": 1, "hour": 10, "intensity": 1.0 }
        // ...
      ]
    }
    ```

---

### 6. Profile & Settings (Screen 12)

#### `GET /api/users/me`
*   **Description:** Gets the current user's complete profile and settings information.
*   **Authentication:** Required.
*   **Success Response (200 OK):**
    ```json
    {
      "id": "user-uuid-123",
      "name": "John Smith",
      "email": "john.smith@email.com",
      "profilePictureUrl": "https://.../avatar.jpg",
      "settings": {
        "theme": "light", // 'light' or 'dark'
        "language": "en"
      }
    }
    ```

#### `PATCH /api/users/me/settings`
*   **Description:** Updates user-specific settings like theme or language.
*   **Authentication:** Required.
*   **Request Body:**
    ```json
    {
      "theme": "dark" // or { "language": "fr" }
    }
    ```
*   **Success Response (200 OK):** Returns the updated settings object.

#### `PUT /api/users/me/password`
*   **Description:** Allows an authenticated user to change their password.
*   **Authentication:** Required.
*   **Request Body:**
    ```json
    {
      "currentPassword": "Password123!",
      "newPassword": "NewSecurePassword456!"
    }
    ```
*   **Success Response (200 OK):**
    ```json
    { "message": "Password updated successfully." }
    ```

#### `POST /api/auth/logout`
*   **Description:** Logs the user out. Can be used to invalidate the refresh token on the server side.
*   **Authentication:** Required.
*   **Request Body:**
    ```json
    {
      "refreshToken": "ey..." // To invalidate it
    }
    ```
*   **Success Response (200 OK):**
    ```json
    { "message": "Logged out successfully." }
    ```
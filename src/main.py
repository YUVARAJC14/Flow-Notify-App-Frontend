from contextlib import asynccontextmanager
from fastapi import FastAPI, APIRouter, Depends
from fastapi.responses import JSONResponse
from .database.database import init_db
from .routers import users, tasks, dashboard, events, insights, auth
from . import models
from .security import get_current_user
from fastapi.middleware.cors import CORSMiddleware

@asynccontextmanager
async def lifespan(app: FastAPI):
    init_db()
    yield

def create_app():
    app = FastAPI(
        lifespan=lifespan,
        title="Flow Notify API",
        version="1.0.0",
        description="API for Flow Notify, a productivity and wellness app.",
        openapi_url="/openapi.json",
        docs_url="/docs",
        redoc_url="/redoc",
    )

    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],  # Or use your Android device IP
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    # Router for authentication endpoints (no security dependency)
    auth_router = APIRouter(prefix="/api")
    auth_router.include_router(auth.router)

    # Router for all other API endpoints (with security dependency)
    api_router = APIRouter(prefix="/api", dependencies=[Depends(get_current_user)])
    api_router.include_router(users.router)
    api_router.include_router(tasks.router)
    api_router.include_router(dashboard.router)
    api_router.include_router(events.router)
    api_router.include_router(insights.router)

    app.include_router(auth_router)
    app.include_router(api_router)

    @app.get("/")
    def read_root():
        return {"message": "Welcome to the Flow Notify API"}

    return app

app = create_app()

@app.exception_handler(Exception)
async def generic_exception_handler(request, exc):
    return JSONResponse(
        status_code=500,
        content={"message": "An unexpected error occurred on the server."},
    )

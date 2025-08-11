from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.responses import JSONResponse
from .database.database import init_db
from .routers import users, tasks, dashboard, events, insights, profile, auth
from . import models
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

    app.include_router(auth.router)
    app.include_router(users.router)
    app.include_router(tasks.router)
    app.include_router(dashboard.router)
    app.include_router(events.router)
    app.include_router(insights.router)
    app.include_router(profile.router)

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

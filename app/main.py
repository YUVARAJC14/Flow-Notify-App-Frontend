from contextlib import asynccontextmanager
from fastapi import FastAPI, APIRouter
from fastapi.responses import JSONResponse
from .database.database import init_db
from .routers import users, tasks, dashboard, events, insights, auth
from . import models
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import OAuth2PasswordBearer

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

    api_router = APIRouter(prefix="/api/v1")
    api_router.include_router(auth.router)
    api_router.include_router(users.router)
    api_router.include_router(tasks.router)
    api_router.include_router(dashboard.router)
    api_router.include_router(events.router)
    api_router.include_router(insights.router)

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
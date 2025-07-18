from contextlib import asynccontextmanager
from fastapi import FastAPI
from .database.database import init_db
from .routers import users, tasks, home, events, calendar, insights
from . import models

@asynccontextmanager
async def lifespan(app: FastAPI):
    init_db()
    yield

def create_app():
    app = FastAPI(lifespan=lifespan)

    app.include_router(users.router)
    app.include_router(tasks.router)
    app.include_router(home.router)
    app.include_router(events.router)
    app.include_router(calendar.router)
    app.include_router(insights.router)

    @app.get("/")
    def read_root():
        return {"message": "Welcome to the Flow Notify API"}

    return app

app = create_app()

from app.database.database import SessionLocal
from app.crud import create_user, create_user_task, create_user_event
from app.schemas import UserCreate, TaskCreate, EventCreate, PriorityEnum, ReminderEnum, CategoryEnum
from datetime import date, time, datetime, timedelta

def add_sample_data():
    db = SessionLocal()
    try:
        # Create a sample user
        user_data = UserCreate(username="testuser", password="testpassword")
        user = create_user(db, user_data)
        print(f"Created user: {user.username}")

        # Create sample tasks for the user
        today = date.today()
        tomorrow = today + timedelta(days=1)
        next_week = today + timedelta(weeks=1)

        task1_data = TaskCreate(
            title="Complete project report",
            description="Finish writing the final report for the Q3 project.",
            due_date=today,
            due_time=time(17, 0),
            priority=PriorityEnum.high,
            reminder=ReminderEnum.one_hour,
            completed=False
        )
        create_user_task(db, task1_data, user.id)
        print("Added task: Complete project report")

        task2_data = TaskCreate(
            title="Buy groceries",
            description="Milk, eggs, bread, and vegetables.",
            due_date=tomorrow,
            due_time=time(10, 0),
            priority=PriorityEnum.medium,
            reminder=ReminderEnum.ten_minutes,
            completed=False
        )
        create_user_task(db, task2_data, user.id)
        print("Added task: Buy groceries")

        task3_data = TaskCreate(
            title="Schedule dentist appointment",
            description="Call to book a check-up.",
            due_date=next_week,
            due_time=time(9, 0),
            priority=PriorityEnum.low,
            reminder=ReminderEnum.one_day,
            completed=True
        )
        create_user_task(db, task3_data, user.id)
        print("Added task: Schedule dentist appointment")

        # Create sample events for the user
        event1_data = EventCreate(
            title="Team Meeting",
            location="Conference Room A",
            start_datetime=datetime.combine(today, time(10, 0)),
            end_datetime=datetime.combine(today, time(11, 0)),
            category=CategoryEnum.work,
            notes="Discuss Q4 planning.",
            reminder_minutes_before=30
        )
        create_user_event(db, event1_data, user.id)
        print("Added event: Team Meeting")

        event2_data = EventCreate(
            title="Dinner with friends",
            location="Italian Restaurant",
            start_datetime=datetime.combine(tomorrow, time(19, 0)),
            end_datetime=datetime.combine(tomorrow, time(21, 0)),
            category=CategoryEnum.social,
            notes="Remember to bring the wine.",
            reminder_minutes_before=60
        )
        create_user_event(db, event2_data, user.id)
        print("Added event: Dinner with friends")

    finally:
        db.close()

if __name__ == "__main__":
    add_sample_data()

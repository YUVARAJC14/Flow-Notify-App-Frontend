from app.database.database import SessionLocal
from app.crud import create_user, create_user_task, create_user_event
from app.schemas import schemas
from datetime import date, time, datetime, timedelta

def add_sample_data():
    db = SessionLocal()
    try:
        # Create a sample user
        user_data = schemas.UserCreate(fullName="testuser", email="test@gmail.com", password="testpassword")
        user = create_user(db, user=user_data)
        print(f"Created user: {user.full_name}")

        # Create sample tasks for the user
        today = date.today()
        tomorrow = today + timedelta(days=1)
        next_week = today + timedelta(weeks=1)

        task1_data = schemas.TaskCreate(
            title="Complete project report",
            description="Finish writing the final report for the Q3 project.",
            dueDate=today.strftime("%Y-%m-%d"),
            dueTime=time(17, 0).strftime("%H:%M"),
            priority='high',
            reminders=[schemas.ReminderEnum.one_hour.value],
            completed=False
        )
        create_user_task(db, task=task1_data, user_id=user.id)
        print("Added task: Complete project report")

        task2_data = schemas.TaskCreate(
            title="Buy groceries",
            description="Milk, eggs, bread, and vegetables.",
            dueDate=tomorrow.strftime("%Y-%m-%d"),
            dueTime=time(10, 0).strftime("%H:%M"),
            priority='medium',
            reminders=[schemas.ReminderEnum.ten_minutes.value],
            completed=False
        )
        create_user_task(db, task=task2_data, user_id=user.id)
        print("Added task: Buy groceries")

        task3_data = schemas.TaskCreate(
            title="Schedule dentist appointment",
            description="Call to book a check-up.",
            dueDate=next_week.strftime("%Y-%m-%d"),
            dueTime=time(9, 0).strftime("%H:%M"),
            priority='low',
            reminders=[schemas.ReminderEnum.one_day.value],
            completed=True
        )
        create_user_task(db, task=task3_data, user_id=user.id)
        print("Added task: Schedule dentist appointment")

        # Create sample events for the user
        event1_data = schemas.EventCreate(
            title="Team Meeting",
            location="Conference Room A",
            start_datetime=datetime.combine(today, time(10, 0)),
            end_datetime=datetime.combine(today, time(11, 0)),
            category=schemas.CategoryEnum.work,
            notes="Discuss Q4 planning.",
            reminder_minutes_before=30
        )
        create_user_event(db, event=event1_data, user_id=user.id)
        print("Added event: Team Meeting")

        event2_data = schemas.EventCreate(
            title="Dinner with friends",
            location="Italian Restaurant",
            start_datetime=datetime.combine(tomorrow, time(19, 0)),
            end_datetime=datetime.combine(tomorrow, time(21, 0)),
            category=schemas.CategoryEnum.social,
            notes="Remember to bring the wine.",
            reminder_minutes_before=60
        )
        create_user_event(db, event=event2_data, user_id=user.id)
        print("Added event: Dinner with friends")

    finally:
        db.close()

if __name__ == "__main__":
    add_sample_data()

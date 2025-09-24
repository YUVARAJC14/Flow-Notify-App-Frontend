from datetime import datetime, timedelta
from typing import List, Dict, Optional

from app.schemas.nudge import Nudge, NudgeRequest

class NudgeService:
    def __init__(self):
        # Placeholder for ML model. In a real application, this would load a trained model.
        self.ml_model = self._load_ml_model()

    def _load_ml_model(self):
        """Loads or initializes the ML model for predicting user habits."""
        print("Loading ML model for nudge prediction...")
        return {"model_status": "ready"}

    def _get_user_historical_activity(self, user_id: int) -> Dict:
        """Fetches simulated user historical activity. In a real app, this would query a DB."""
        # Mock data for demonstration
        if user_id == 1:
            return {
                "avg_prep_time_for_meetings": timedelta(hours=1),
                "task_creation_before_deadline": timedelta(days=2)
            }
        return {}

    def _get_user_calendar_events(self, user_id: int, start_date: datetime, end_date: datetime) -> List[Dict]:
        """Fetches simulated calendar events. In a real app, this would query a calendar API or DB."""
        # Mock data for demonstration: a meeting tomorrow
        if user_id == 1:
            tomorrow = datetime.now() + timedelta(days=1)
            return [{
                "id": 101,
                "title": "Team Sync",
                "start_datetime": tomorrow.replace(hour=10, minute=0, second=0, microsecond=0),
                "end_datetime": tomorrow.replace(hour=11, minute=0, second=0, microsecond=0)
            }]
        return []

    def _get_user_tasks(self, user_id: int) -> List[Dict]:
        """Fetches simulated user tasks. In a real app, this would query a DB."""
        # Mock data for demonstration: a task due in 3 days
        if user_id == 1:
            due_date = datetime.now() + timedelta(days=3)
            return [{
                "id": 201,
                "title": "Prepare Q3 Report",
                "due_date": due_date.date(),
                "due_time": due_date.time()
            }]
        return []

    def generate_proactive_nudges(self, user_id: int, request: NudgeRequest) -> List[Nudge]:
        """Generates proactive nudges based on user patterns and upcoming events/tasks."""
        print(f"Generating nudges for user {user_id}")
        nudges = []

        user_activity = self._get_user_historical_activity(user_id)
        if not user_activity:
            print(f"No historical activity found for user {user_id}")
            return []

        # Monitor upcoming events
        search_start_date = datetime.now()
        search_end_date = search_start_date + timedelta(days=7) # Look ahead 7 days
        upcoming_events = self._get_user_calendar_events(user_id, search_start_date, search_end_date)

        for event in upcoming_events:
            prep_time_needed = user_activity.get("avg_prep_time_for_meetings", timedelta(minutes=0))
            nudge_time = event["start_datetime"] - prep_time_needed

            if datetime.now() < nudge_time < datetime.now() + timedelta(days=1): # Nudge if within next 24 hours
                nudges.append(Nudge(
                    user_id=user_id,
                    message=f"Reminder: Your '{event['title']}' meeting is coming up. Would you like to create a task to prepare?",
                    nudge_type="meeting_prep_suggestion",
                    timestamp=datetime.now(),
                    context={"event_id": event["id"], "event_title": event["title"]}
                ))

        # Monitor upcoming tasks and deadlines
        user_tasks = self._get_user_tasks(user_id)
        for task in user_tasks:
            task_due_datetime = datetime.combine(task["due_date"], task["due_time"])
            task_initiation_habit = user_activity.get("task_creation_before_deadline", timedelta(days=0))
            nudge_time = task_due_datetime - task_initiation_habit

            if datetime.now() < nudge_time < datetime.now() + timedelta(days=1): # Nudge if within next 24 hours of habit window
                 nudges.append(Nudge(
                    user_id=user_id,
                    message=f"Heads up! Your task '{task['title']}' is due soon. Time to get started?",
                    nudge_type="task_initiation_suggestion",
                    timestamp=datetime.now(),
                    context={"task_id": task["id"], "task_title": task["title"]}
                ))

        return nudges

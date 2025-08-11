from datetime import datetime, timedelta
from typing import List, Dict, Optional

from app.schemas.notification import NotificationContent, NotificationTimingSuggestion, NotificationInteraction

class NotificationTimingService:
    def __init__(self):
        # Placeholder for ML model. In a real application, this would load a trained model.
        self.ml_model = self._load_ml_model()

    def _load_ml_model(self):
        """Loads or initializes the ML model for predicting optimal notification times."""
        print("Loading ML model for notification timing...")
        return {"model_status": "ready"}

    def _get_user_notification_history(self, user_id: int) -> List[Dict]:
        """Fetches simulated user notification interaction history."""
        # Mock data for demonstration
        if user_id == 1:
            return [
                {"notification_id": 1, "interaction_type": "opened", "timestamp": datetime.now() - timedelta(hours=2), "scheduled_time": datetime.now() - timedelta(hours=2, minutes=5)},
                {"notification_id": 2, "interaction_type": "dismissed", "timestamp": datetime.now() - timedelta(hours=1), "scheduled_time": datetime.now() - timedelta(hours=1, minutes=5)}
            ]
        return []

    def _get_user_focus_hours(self, user_id: int) -> List[Dict]:
        """Fetches simulated user-defined focus hours or DND periods."""
        # Mock data: User prefers no notifications between 12 PM and 1 PM
        if user_id == 1:
            return [
                {"start_hour": 12, "end_hour": 13, "type": "do_not_disturb"}
            ]
        return []

    def _get_user_calendar_events(self, user_id: int, start_date: datetime, end_date: datetime) -> List[Dict]:
        """Fetches simulated user calendar events."""
        # Mock data: User has a meeting from 10 AM to 11 AM tomorrow
        if user_id == 1:
            tomorrow = datetime.now() + timedelta(days=1)
            return [
                {"title": "Important Meeting", "start": tomorrow.replace(hour=10, minute=0), "end": tomorrow.replace(hour=11, minute=0)}
            ]
        return []

    def _get_user_tasks(self, user_id: int) -> List[Dict]:
        """Fetches simulated user tasks."""
        # Mock data: User has a high priority task due soon
        if user_id == 1:
            return [
                {"title": "Finish Project Report", "priority": "High", "due_date": datetime.now() + timedelta(days=2)}
            ]
        return []

    def suggest_optimal_notification_time(self, user_id: int, notification_content: NotificationContent) -> NotificationTimingSuggestion:
        """Suggests an optimal time to deliver a notification based on ML model, user habits, and context."""
        print(f"Suggesting optimal time for notification for user {user_id}, type: {notification_content.notification_type}")

        # Placeholder for ML model prediction
        # In a real scenario, the ML model would analyze historical data, focus hours, etc.
        # to predict the best time.
        optimal_time = datetime.now() + timedelta(minutes=5) # Default to 5 minutes from now
        reason = "Default immediate delivery"

        user_history = self._get_user_notification_history(user_id)
        user_focus_hours = self._get_user_focus_hours(user_id)
        user_calendar = self._get_user_calendar_events(user_id, datetime.now(), datetime.now() + timedelta(days=1))
        user_tasks = self._get_user_tasks(user_id)

        # Simple logic based on mock data and rules
        # Avoid DND hours
        for fh in user_focus_hours:
            if fh["type"] == "do_not_disturb" and fh["start_hour"] <= optimal_time.hour < fh["end_hour"]:
                optimal_time = optimal_time.replace(hour=fh["end_hour"], minute=0, second=0, microsecond=0) + timedelta(minutes=1)
                reason = f"Adjusted to avoid DND hours ({fh['start_hour']}-{fh['end_hour']})"
                break

        # Avoid during important meetings (simple check)
        for event in user_calendar:
            if event["start"] <= optimal_time < event["end"]:
                optimal_time = event["end"] + timedelta(minutes=5) # Schedule after meeting
                reason = f"Adjusted to avoid during meeting: {event['title']}"
                break

        # Consider urgency (simple example: urgent notifications are sent sooner)
        if notification_content.urgency >= 8:
            optimal_time = datetime.now() + timedelta(minutes=1) # Send urgent notifications very soon
            reason = "Urgent notification, sending soon"

        return NotificationTimingSuggestion(scheduled_time=optimal_time, reason=reason)

    def record_notification_interaction(self, interaction: NotificationInteraction):
        """Records user interaction with a notification for ML model training."""
        print(f"Recording interaction for notification {interaction.notification_id}: {interaction.interaction_type}")
        # In a real application, this would save to a database for ML model training
        pass

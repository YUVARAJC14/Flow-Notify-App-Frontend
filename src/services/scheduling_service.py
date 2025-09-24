from datetime import datetime, timedelta
from typing import List, Dict, Optional

from app.schemas.scheduling import SchedulingSuggestion, SchedulingRequest

class SchedulingService:
    def __init__(self):
        # Placeholder for ML model. In a real application, this would load a trained model.
        self.ml_model = self._load_ml_model()

    def _load_ml_model(self):
        """Loads or initializes the ML model for predicting optimal task times."""
        # This is a mock-up. A real implementation would load a pre-trained model
        # or set up a new one for training.
        print("Loading ML model for scheduling...")
        return {"model_status": "ready"}

    def _get_user_work_habits(self, user_id: int) -> Dict:
        """Fetches simulated user work habits. In a real app, this would query a DB."""
        # Mock data for demonstration
        if user_id == 1:
            return {
                "preferred_start_hour": 9,
                "preferred_end_hour": 17,
                "preferred_task_type_times": {
                    "deep_work": {"start": 10, "end": 12},
                    "light_work": {"start": 14, "end": 16}
                }
            }
        return {}

    def _get_user_calendar_availability(self, user_id: int, start_date: datetime, end_date: datetime) -> List[Dict]:
        """Fetches simulated calendar availability. In a real app, this would query a calendar API or DB."""
        # Mock data for demonstration: assume user is busy every day from 12:00 to 13:00
        busy_slots = []
        current_date = start_date.date()
        while current_date <= end_date.date():
            busy_slots.append({
                "start": datetime.combine(current_date, datetime.min.time()).replace(hour=12, minute=0),
                "end": datetime.combine(current_date, datetime.min.time()).replace(hour=13, minute=0)
            })
            current_date += timedelta(days=1)
        return busy_slots

    def suggest_optimal_time(self, user_id: int, request: SchedulingRequest) -> Optional[SchedulingSuggestion]:
        """Suggests an optimal time slot for a given task based on user habits and calendar availability."""
        print(f"Suggesting optimal time for user {user_id}, task type: {request.task_type}, duration: {request.duration_minutes} minutes")

        user_habits = self._get_user_work_habits(user_id)
        if not user_habits:
            print(f"No work habits found for user {user_id}")
            return None

        # Define a search window (e.g., next 7 days)
        search_start_date = datetime.now()
        search_end_date = search_start_date + timedelta(days=7)

        calendar_availability = self._get_user_calendar_availability(user_id, search_start_date, search_end_date)

        # Simple scheduling logic (to be replaced by ML model prediction)
        # For demonstration, we'll try to find a slot within preferred hours, avoiding busy slots.
        preferred_start_hour = user_habits.get("preferred_start_hour", 9)
        preferred_end_hour = user_habits.get("preferred_end_hour", 17)

        # Prioritize times based on task type if available
        task_type_times = user_habits.get("preferred_task_type_times", {}).get(request.task_type)
        if task_type_times:
            preferred_start_hour = task_type_times["start"]
            preferred_end_hour = task_type_times["end"]

        current_check_time = search_start_date
        while current_check_time < search_end_date:
            # Check if current_check_time is within preferred work hours
            if preferred_start_hour <= current_check_time.hour < preferred_end_hour:
                proposed_end_time = current_check_time + timedelta(minutes=request.duration_minutes)

                # Check for conflicts with calendar availability
                is_conflict = False
                for busy_slot in calendar_availability:
                    if not (proposed_end_time <= busy_slot["start"] or current_check_time >= busy_slot["end"]):
                        is_conflict = True
                        break

                if not is_conflict:
                    print(f"Found optimal slot: {current_check_time} to {proposed_end_time}")
                    return SchedulingSuggestion(start_time=current_check_time, end_time=proposed_end_time)

            # Move to the next hour or next day if outside preferred hours
            if current_check_time.hour >= preferred_end_hour:
                current_check_time = current_check_time.replace(hour=preferred_start_hour, minute=0, second=0, microsecond=0) + timedelta(days=1)
            else:
                current_check_time += timedelta(hours=1)

        print("No optimal slot found.")
        return None

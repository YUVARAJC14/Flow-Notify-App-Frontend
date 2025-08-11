from datetime import date, datetime, time, timedelta
from typing import List, Optional, Tuple
from dateutil.rrule import rrule, rrulestr, DAILY, WEEKLY, MONTHLY, YEARLY

from app.models.models import Task, Event
from app.schemas.schemas import TaskCreate, EventCreate

class RecurrenceService:
    def __init__(self):
        pass

    def _parse_recurrence_rule(self, rrule_str: str, dtstart: datetime) -> Optional[rrule]:
        """Parses an RRULE string into a dateutil rrule object."""
        try:
            # rrulestr can parse a full RRULE string, but we need to ensure DTSTART is handled
            # For simplicity, we'll assume rrule_str is just the RRULE part for now
            # and construct the rrule object with dtstart.
            # A more robust solution might parse the full iCalendar string.
            return rrulestr(f"DTSTART:{dtstart.strftime('%Y%m%dT%H%M%S')}\nRRULE:{rrule_str}")
        except Exception as e:
            print(f"Error parsing recurrence rule {rrule_str}: {e}")
            return None

    def generate_recurring_tasks(self, base_task: Task, start_date: date, end_date: date) -> List[Task]:
        """Generates future occurrences of a recurring task."""
        if not base_task.recurrence_rule:
            return []

        generated_tasks = []
        dtstart = datetime.combine(base_task.due_date, base_task.due_time)
        rule = self._parse_recurrence_rule(base_task.recurrence_rule, dtstart)

        if not rule:
            return []

        # Limit the generation to a reasonable future period or recurrence_end_date
        # For now, generate up to end_date or base_task.recurrence_end_date
        effective_end_date = base_task.recurrence_end_date if base_task.recurrence_end_date and base_task.recurrence_end_date < end_date else end_date

        for dt in rule.between(dtstart, datetime.combine(effective_end_date, time.max)):
            if dt.date() > base_task.due_date: # Only generate future occurrences
                new_task = Task(
                    title=base_task.title,
                    description=base_task.description,
                    due_date=dt.date(),
                    due_time=dt.time(),
                    priority=base_task.priority,
                    reminder=base_task.reminder,
                    completed=False, # New occurrences are not completed
                    owner_id=base_task.owner_id,
                    recurrence_rule=base_task.recurrence_rule, # Keep rule for future generations
                    recurrence_end_date=base_task.recurrence_end_date
                )
                generated_tasks.append(new_task)
        return generated_tasks

    def generate_recurring_events(self, base_event: Event, start_date: date, end_date: date) -> List[Event]:
        """Generates future occurrences of a recurring event."""
        if not base_event.recurrence_rule:
            return []

        generated_events = []
        dtstart = base_event.start_datetime
        rule = self._parse_recurrence_rule(base_event.recurrence_rule, dtstart)

        if not rule:
            return []

        effective_end_date = base_event.recurrence_end_date if base_event.recurrence_end_date and base_event.recurrence_end_date < end_date else end_date

        for dt in rule.between(dtstart, datetime.combine(effective_end_date, time.max)):
            if dt.date() > base_event.start_datetime.date(): # Only generate future occurrences
                time_diff = base_event.end_datetime - base_event.start_datetime
                new_event = Event(
                    title=base_event.title,
                    location=base_event.location,
                    start_datetime=dt,
                    end_datetime=dt + time_diff,
                    category=base_event.category,
                    notes=base_event.notes,
                    reminder_minutes_before=base_event.reminder_minutes_before,
                    owner_id=base_event.owner_id,
                    recurrence_rule=base_event.recurrence_rule, # Keep rule for future generations
                    recurrence_end_date=base_event.recurrence_end_date
                )
                generated_events.append(new_event)
        return generated_events

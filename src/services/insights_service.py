from sqlalchemy.orm import Session
from .. import crud
from src.tasks import models as task_models
from src.events import models as event_models
from datetime import date, timedelta, datetime
from collections import Counter
import re

def _calculate_period_progress(db: Session, user_id: str, start_date: date, end_date: date, completion_weight: float, timeliness_weight: float) -> float:
    # --- Weighted Completion Rate ---
    priority_weights = {"High": 1.5, "Medium": 1.0, "Low": 0.5}
    total_weighted_effort = 0.0
    completed_weighted_effort = 0.0

    tasks = crud.get_tasks_by_date_range(db, user_id, start_date, end_date)
    for task in tasks:
        weight = priority_weights.get(task.priority.value, 1.0)
        total_weighted_effort += weight
        if task.completed:
            completed_weighted_effort += weight

    events = crud.get_events(db, user_id, start_date, end_date)
    total_weighted_effort += len(events) # Events have a default weight of 1.0
    completed_events_count = sum(1 for event in events if event.completed)
    completed_weighted_effort += completed_events_count

    if total_weighted_effort == 0:
        weighted_completion_score = 0.0
    else:
        weighted_completion_score = (completed_weighted_effort / total_weighted_effort) * 70 # Max 70 points

    # --- Timeliness Bonus/Penalty ---
    timeliness_score = 0.0
    completed_tasks = [task for task in tasks if task.completed and task.completed_at and task.due_date]
    
    for task in completed_tasks:
        due_datetime = datetime.combine(task.due_date, task.due_time or datetime.min.time())
        time_diff = due_datetime - task.completed_at
        if time_diff.total_seconds() > 3600: # More than 1 hour early
            timeliness_score += 2
        elif time_diff.total_seconds() < -3600: # More than 1 hour late
            timeliness_score -= 2
        else: # On time
            timeliness_score += 1

    # Normalize timeliness score to be out of a max of 30 points
    if completed_tasks:
        max_possible_timeliness = len(completed_tasks) * 2
        normalized_timeliness = (timeliness_score / max_possible_timeliness) * 30
    else:
        normalized_timeliness = 0

    # --- Final Score ---
    final_score = (weighted_completion_score * completion_weight) + (normalized_timeliness * timeliness_weight)
    return max(0, min(100, final_score)) # Ensure score is between 0 and 100

# --- New ML/NLP related functions ---

STOP_WORDS = set([
    "a", "an", "and", "are", "as", "at", "be", "but", "by",
    "for", "if", "in", "into", "is", "it", "no", "not", "of",
    "on", "or", "such", "that", "the", "their", "then", "there",
    "these", "they", "this", "to", "was", "will", "with"
])

def _preprocess_text(text: str) -> list[str]:
    text = text.lower()
    text = re.sub(r'[^a-z0-9\s]', '', text) # Remove punctuation
    words = text.split()
    return [word for word in words if word not in STOP_WORDS]

def get_activity_summary(db: Session, user_id: str, period: str) -> str:
    today = date.today()
    if period == "day":
        start_date = today
        end_date = today
    elif period == "week":
        start_date = today - timedelta(days=today.weekday())
        end_date = start_date + timedelta(days=6)
    elif period == "month":
        start_date = today.replace(day=1)
        end_date = (start_date + timedelta(days=32)).replace(day=1) - timedelta(days=1)
    else: # year
        start_date = today.replace(month=1, day=1)
        end_date = today.replace(month=12, day=31)

    completed_tasks = crud.get_completed_tasks_by_date_range(db, user_id, start_date, end_date)
    completed_events = crud.get_completed_events_by_date_range(db, user_id, start_date, end_date)

    all_text = []
    task_categories = Counter()
    event_categories = Counter()

    for task in completed_tasks:
        all_text.extend(_preprocess_text(task.title))
        if task.description: all_text.extend(_preprocess_text(task.description))
        if task.priority: task_categories[task.priority.value] += 1

    for event in completed_events:
        all_text.extend(_preprocess_text(event.title))
        if event.notes: all_text.extend(_preprocess_text(event.notes))
        if event.category: event_categories[event.category.value] += 1

    total_completed_items = len(completed_tasks) + len(completed_events)

    if total_completed_items == 0:
        return f"No completed activities found for this {period}. Time to get productive!"

    # Keyword extraction
    word_counts = Counter(all_text)
    top_keywords = [word for word, count in word_counts.most_common(3)]

    # Category analysis
    most_common_task_category = task_categories.most_common(1)
    most_common_event_category = event_categories.most_common(1)

    summary_parts = []
    summary_parts.append(f"This {period}, you completed {total_completed_items} activities.")

    if completed_tasks:
        summary_parts.append(f"You finished {len(completed_tasks)} tasks.")
        if most_common_task_category: summary_parts.append(f"Your top task priority was {most_common_task_category[0][0]}.")

    if completed_events:
        summary_parts.append(f"You attended {len(completed_events)} events.")
        if most_common_event_category: summary_parts.append(f"Your main event category was {most_common_event_category[0][0]}.")

    if top_keywords:
        summary_parts.append(f"Key themes included: {', '.join(top_keywords)}.")

    return " ".join(summary_parts)


def get_insights(db: Session, user_id: str, period: str):
    today = date.today()
    if period == "day":
        start_date = today
        end_date = today
        last_period_start_date = today - timedelta(days=1)
        last_period_end_date = today - timedelta(days=1)
    elif period == "week":
        start_date = today - timedelta(days=today.weekday())
        end_date = start_date + timedelta(days=6)
        last_period_start_date = start_date - timedelta(days=7)
        last_period_end_date = end_date - timedelta(days=7)
    elif period == "month":
        start_date = today.replace(day=1)
        end_date = (start_date + timedelta(days=32)).replace(day=1) - timedelta(days=1)
        last_month = start_date - timedelta(days=1)
        last_period_start_date = last_month.replace(day=1)
        last_period_end_date = (last_period_start_date + timedelta(days=32)).replace(day=1) - timedelta(days=1)
    else: # year
        start_date = today.replace(month=1, day=1)
        end_date = today.replace(month=12, day=31)
        last_period_start_date = start_date.replace(year=start_date.year - 1)
        last_period_end_date = end_date.replace(year=end_date.year - 1)

    # Flow Score
    if period == "day":
        completion_weight = 0.85
        timeliness_weight = 0.15
    elif period == "week":
        completion_weight = 0.70
        timeliness_weight = 0.30
    elif period == "month":
        completion_weight = 0.60
        timeliness_weight = 0.40
    else: # year
        completion_weight = 0.50
        timeliness_weight = 0.50

    current_flow = _calculate_period_progress(db, user_id, start_date, end_date, completion_weight, timeliness_weight)
    last_period_flow = _calculate_period_progress(db, user_id, last_period_start_date, last_period_end_date, completion_weight, timeliness_weight)
    flow_change = current_flow - last_period_flow

    # Task Completion
    task_completion = []
    if period == "day":
        tasks = crud.get_tasks_by_date(db, user_id, today)
        completed = sum(1 for task in tasks if task.completed)
        task_completion.append({"label": today.strftime("%a"), "completed": completed, "total": len(tasks)})
    elif period == "week":
        for i in range(7):
            day = start_date + timedelta(days=i)
            tasks = crud.get_tasks_by_date(db, user_id, day)
            completed = sum(1 for task in tasks if task.completed)
            task_completion.append({"label": day.strftime("%a"), "completed": completed, "total": len(tasks)})
    elif period == "month":
        for i in range(4):
            week_start = start_date + timedelta(weeks=i)
            week_end = week_start + timedelta(days=6)
            tasks = crud.get_tasks_by_date_range(db, user_id, week_start, week_end)
            completed = sum(1 for task in tasks if task.completed)
            task_completion.append({"label": f"Week {i+1}", "completed": completed, "total": len(tasks)})
    elif period == "year":
        for i in range(1, 13):
            month_start = today.replace(month=i, day=1)
            month_end = (month_start + timedelta(days=32)).replace(day=1) - timedelta(days=1)
            tasks = crud.get_tasks_by_date_range(db, user_id, month_start, month_end)
            completed = sum(1 for task in tasks if task.completed)
            task_completion.append({"label": month_start.strftime("%b"), "completed": completed, "total": len(tasks)})

    # Productive Times
    productive_times = []
    completed_tasks = crud.get_completed_tasks_by_date_range(db, user_id, start_date, end_date)
    for task in completed_tasks:
        if task.completed_at:
            productive_times.append({"day": task.completed_at.weekday(), "hour": task.completed_at.hour, "intensity": 0.5})

    return {
        "flowScore": {"score": int(current_flow), "comparison": {"change": int(flow_change), "period": f"last {period}"}},
        "taskCompletion": task_completion,
        "productiveTimes": productive_times
    }
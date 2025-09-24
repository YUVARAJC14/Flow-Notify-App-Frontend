import dateparser
from datetime import datetime, date, time
import re

class NLPService:
    def parse_natural_language_task(self, text: str):
        # Initialize parsed components
        task_description = text
        due_date = None
        due_time = None

        # Attempt to parse date and time
        # dateparser is quite powerful, but we might need some hints or pre-processing
        # to get the best results for specific patterns.

        # Example: "Remind me to call the vet tomorrow at 4 PM"
        # dateparser might extract "tomorrow at 4 PM"
        parsed_datetime = dateparser.parse(text, settings={'PREFER_DATES_FROM': 'future'})

        if parsed_datetime:
            # If a full datetime was parsed, extract date and time
            due_date = parsed_datetime.date()
            due_time = parsed_datetime.time()

            # Attempt to remove the date/time phrase from the task description
            # This is a simple approach and might need more sophisticated NLP for complex sentences
            date_time_patterns = [
                r" (today|tomorrow|tonight|this (morning|afternoon|evening|night))",
                r" (on|at) \d{1,2}(/\d{1,2})?(/\d{2,4})?", # e.g., on 12/25, at 5/5
                r" (at|by) \d{1,2}(:\d{2})? ?(am|pm)?", # e.g., at 4 PM, by 10:30
                r" (in|on) (monday|tuesday|wednesday|thursday|friday|saturday|sunday)",
                r" (next|this) (week|month|year)",
                r" (in) (\d+ (day|week|month|year)s?)",
                r" (every) (day|week|month|year)",
                r" (daily|weekly|monthly|yearly)",
            ]
            
            for pattern in date_time_patterns:
                match = re.search(pattern, task_description, re.IGNORECASE)
                if match:
                    task_description = task_description.replace(match.group(0), "").strip()
                    # Clean up any leading/trailing prepositions if they are left alone
                    task_description = re.sub(r"^(on|at|by|in)\s+", "", task_description, flags=re.IGNORECASE).strip()
                    task_description = re.sub(r"\s+(on|at|by|in)$", "", task_description, flags=re.IGNORECASE).strip()
                    break # Assume only one date/time phrase for simplicity

        # Basic intent recognition (very simple for now)
        # Remove common task-related phrases to get a cleaner description
        task_description = re.sub(r"^(remind me to|i need to|please|can you|to)\s+", "", task_description, flags=re.IGNORECASE).strip()
        task_description = re.sub(r"\s+(please|can you)$", "", task_description, flags=re.IGNORECASE).strip()

        return {
            "description": task_description,
            "due_date": due_date,
            "due_time": due_time
        }

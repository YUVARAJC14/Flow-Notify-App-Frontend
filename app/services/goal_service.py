from sqlalchemy.orm import Session
from app.models.models import Goal, User, Task
from app.schemas.schemas import GoalCreate, GoalStatusEnum
from typing import List, Optional
from datetime import date

class GoalService:
    def __init__(self):
        # Placeholder for ML model. In a real application, this would load a trained model.
        self.ml_model = self._load_ml_model()

    def _load_ml_model(self):
        """Loads or initializes the ML model for predicting task suggestions for goals."""
        print("Loading ML model for goal task suggestions...")
        return {"model_status": "ready"}

    def create_goal(self, db: Session, goal: GoalCreate, user_id: int) -> Goal:
        db_goal = Goal(**goal.model_dump(), owner_id=user_id)
        db.add(db_goal)
        db.commit()
        db.refresh(db_goal)
        return db_goal

    def get_goals(self, db: Session, user_id: int, status: Optional[GoalStatusEnum] = None) -> List[Goal]:
        query = db.query(Goal).filter(Goal.owner_id == user_id)
        if status:
            query = query.filter(Goal.status == status)
        return query.all()

    def get_goal_by_id(self, db: Session, goal_id: int, user_id: int) -> Optional[Goal]:
        return db.query(Goal).filter(Goal.id == goal_id, Goal.owner_id == user_id).first()

    def update_goal(self, db: Session, db_goal: Goal, goal_update: GoalCreate) -> Goal:
        goal_data = goal_update.model_dump(exclude_unset=True)
        for key, value in goal_data.items():
            setattr(db_goal, key, value)
        db.add(db_goal)
        db.commit()
        db.refresh(db_goal)
        return db_goal

    def delete_goal(self, db: Session, db_goal: Goal):
        db.delete(db_goal)
        db.commit()

    def suggest_tasks_for_goal(self, db: Session, goal_id: int, user_id: int) -> List[str]:
        """Suggests tasks to help achieve a goal based on ML model and user habits."""
        goal = self.get_goal_by_id(db, goal_id, user_id)
        if not goal:
            return []

        print(f"Suggesting tasks for goal: {goal.title} for user {user_id}")
        # Placeholder for ML model prediction
        # In a real scenario, the ML model would analyze user habits, goal type,
        # and existing tasks to suggest relevant new tasks.
        
        # Mock suggestions based on goal title
        suggestions = []
        if "report" in goal.title.lower():
            suggestions.append("Gather data for report")
            suggestions.append("Outline report structure")
            suggestions.append("Write executive summary")
        elif "project" in goal.title.lower():
            suggestions.append("Break down project into smaller steps")
            suggestions.append("Identify key stakeholders")
        
        return suggestions

    def update_goal_progress(self, db: Session, goal_id: int, user_id: int):
        """Updates goal progress based on associated completed tasks."""
        goal = self.get_goal_by_id(db, goal_id, user_id)
        if not goal:
            return

        associated_tasks = db.query(Task).filter(Task.goal_id == goal.id, Task.owner_id == user_id).all()
        if not associated_tasks:
            goal.progress = 0
            goal.status = GoalStatusEnum.not_started
        else:
            completed_tasks = [task for task in associated_tasks if task.completed]
            progress_percentage = int((len(completed_tasks) / len(associated_tasks)) * 100)
            goal.progress = progress_percentage
            if progress_percentage == 100:
                goal.status = GoalStatusEnum.completed
            elif progress_percentage > 0:
                goal.status = GoalStatusEnum.in_progress
            else:
                goal.status = GoalStatusEnum.not_started
        
        db.add(goal)
        db.commit()
        db.refresh(goal)

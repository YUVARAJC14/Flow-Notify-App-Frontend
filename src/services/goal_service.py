from sqlalchemy.orm import Session
from sqlalchemy.orm import Session
from src.auth import models as auth_models
from src.tasks import models as task_models
from ..schemas.schemas import GoalCreate, GoalStatusEnum
from typing import List, Optional

class GoalService:
    def __init__(self):
        # Placeholder for ML model. In a real application, this would load a trained model.
        self.ml_model = self._load_ml_model()

    def _load_ml_model(self):
        """Loads or initializes the ML model for predicting task suggestions for goals."""
        print("Loading ML model for goal task suggestions...")
        return {"model_status": "ready"}

    def create_goal(self, db: Session, goal: GoalCreate, user_id: int) -> task_models.Goal:
        db_goal = task_models.Goal(**goal.model_dump(), owner_id=user_id)
        db.add(db_goal)
        db.commit()
        db.refresh(db_goal)
        return db_goal

    def get_goals(self, db: Session, user_id: int, status: Optional[GoalStatusEnum] = None) -> List[task_models.Goal]:
        query = db.query(task_models.Goal).filter(task_models.Goal.owner_id == user_id)
        if status:
            query = query.filter(task_models.Goal.status == status)
        return query.all()

    def get_goal_by_id(self, db: Session, goal_id: int, user_id: int) -> Optional[task_models.Goal]:
        return db.query(task_models.Goal).filter(task_models.Goal.id == goal_id, task_models.Goal.owner_id == user_id).first()

    def update_goal(self, db: Session, db_goal: task_models.Goal, goal_update: GoalCreate) -> task_models.Goal:
        goal_data = goal_update.model_dump(exclude_unset=True)
        for key, value in goal_data.items():
            setattr(db_goal, key, value)
        db.add(db_goal)
        db.commit()
        db.refresh(db_goal)
        return db_goal

    def delete_goal(self, db: Session, db_goal: task_models.Goal):
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

        associated_tasks = db.query(task_models.Task).filter(task_models.Task.goal_id == goal.id, task_models.Task.owner_id == user_id).all()
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

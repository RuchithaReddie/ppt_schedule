from typing import Dict, Any, List
from datetime import datetime
from tasks.base_task import BaseTask

class WorkflowService:
    """Service to orchestrate and manage workflow execution"""
    
    def __init__(self):
        self.tasks: List[BaseTask] = []
        self.workflow_start = None
        self.workflow_end = None
        self.workflow_status = "pending"
    
    def add_task(self, task: BaseTask) -> None:
        """Add a task to the workflow"""
        self.tasks.append(task)
    
    def execute_workflow(self, task_sequence: List[str], inputs: Dict[str, Any]) -> Dict[str, Any]:
        """Execute tasks in sequence"""
        self.workflow_start = datetime.now()
        self.workflow_status = "running"
        
        results = {}
        
        try:
            for task_name in task_sequence:
                task = self._get_task_by_name(task_name)
                if not task:
                    raise ValueError(f"Task {task_name} not found in workflow")
                
                # Execute task with inputs
                task_result = task.execute(**inputs)
                results[task_name] = {
                    "result": task_result,
                    "metadata": task.get_metadata()
                }
                
                # Use output of one task as input to next
                inputs.update(task_result)
            
            self.workflow_status = "completed"
            
        except Exception as e:
            self.workflow_status = "failed"
            results["error"] = str(e)
        finally:
            self.workflow_end = datetime.now()
        
        return self._format_workflow_result(results)
    
    def execute_single_task(self, task_name: str, inputs: Dict[str, Any]) -> Dict[str, Any]:
        """Execute a single task"""
        self.workflow_start = datetime.now()
        self.workflow_status = "running"
        
        try:
            task = self._get_task_by_name(task_name)
            if not task:
                raise ValueError(f"Task {task_name} not found in workflow")
            
            result = task.execute(**inputs)
            self.workflow_status = "completed"
            
            return {
                "status": "success",
                "task": task_name,
                "result": result,
                "metadata": task.get_metadata()
            }
            
        except Exception as e:
            self.workflow_status = "failed"
            return {
                "status": "error",
                "task": task_name,
                "error": str(e)
            }
        finally:
            self.workflow_end = datetime.now()
    
    def _get_task_by_name(self, task_name: str) -> BaseTask:
        """Get task instance by name"""
        for task in self.tasks:
            if task.__class__.__name__ == task_name:
                return task
        return None
    
    def _format_workflow_result(self, results: Dict[str, Any]) -> Dict[str, Any]:
        """Format workflow execution results"""
        return {
            "status": self.workflow_status,
            "start_time": self.workflow_start.isoformat() if self.workflow_start else None,
            "end_time": self.workflow_end.isoformat() if self.workflow_end else None,
            "duration_seconds": self._get_workflow_duration(),
            "results": results
        }
    
    def _get_workflow_duration(self) -> float:
        """Get total workflow execution time"""
        if self.workflow_start and self.workflow_end:
            return (self.workflow_end - self.workflow_start).total_seconds()
        return 0

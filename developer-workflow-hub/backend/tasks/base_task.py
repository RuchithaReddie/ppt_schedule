from abc import ABC, abstractmethod
from typing import Dict, Any
from datetime import datetime

class BaseTask(ABC):
    """Abstract base class for all tasks in the workflow"""
    
    def __init__(self):
        self.task_name = self.__class__.__name__
        self.start_time = None
        self.end_time = None
        self.status = "pending"
        self.result = None
        
    @abstractmethod
    def execute(self, **kwargs) -> Dict[str, Any]:
        """Execute the task and return results"""
        pass
    
    def start(self):
        """Mark task as started"""
        self.start_time = datetime.now()
        self.status = "running"
        
    def complete(self, result: Dict[str, Any]):
        """Mark task as completed"""
        self.end_time = datetime.now()
        self.status = "completed"
        self.result = result
        
    def fail(self, error: str):
        """Mark task as failed"""
        self.end_time = datetime.now()
        self.status = "failed"
        self.result = {"error": error}
        
    def get_duration(self) -> float:
        """Get execution time in seconds"""
        if self.start_time and self.end_time:
            return (self.end_time - self.start_time).total_seconds()
        return 0
    
    def get_metadata(self) -> Dict[str, Any]:
        """Get task metadata"""
        return {
            "task_name": self.task_name,
            "status": self.status,
            "start_time": self.start_time.isoformat() if self.start_time else None,
            "end_time": self.end_time.isoformat() if self.end_time else None,
            "duration_seconds": self.get_duration()
        }

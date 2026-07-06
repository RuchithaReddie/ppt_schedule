from flask import jsonify, request
from typing import Dict, Any
from tasks.repository_analysis_task import RepositoryAnalysisTask
from tasks.documentation_task import DocumentationTask
from tasks.code_quality_task import CodeQualityTask
from services.workflow_service import WorkflowService
from services.report_service import ReportService

class TaskController:
    """Controller to handle task execution requests"""
    
    def __init__(self):
        self.workflow_service = WorkflowService()
        self.report_service = ReportService()
        self._initialize_tasks()
    
    def _initialize_tasks(self):
        """Initialize all available tasks"""
        self.workflow_service.add_task(RepositoryAnalysisTask())
        self.workflow_service.add_task(DocumentationTask())
        self.workflow_service.add_task(CodeQualityTask())
    
    def execute_repository_analysis(self, repo_url: str) -> Dict[str, Any]:
        """Execute repository analysis task"""
        try:
            result = self.workflow_service.execute_single_task(
                "RepositoryAnalysisTask",
                {"repo_url": repo_url}
            )
            
            if result.get("status") == "success":
                # Generate report
                report_path = self.report_service.generate_repository_report(
                    result.get("result", {})
                )
                result["report_path"] = report_path
            
            return result
        except Exception as e:
            return {
                "status": "error",
                "error": str(e)
            }
    
    def execute_documentation_task(self, repo_url: str) -> Dict[str, Any]:
        """Execute documentation generation task"""
        try:
            result = self.workflow_service.execute_single_task(
                "DocumentationTask",
                {"repo_url": repo_url}
            )
            
            if result.get("status") == "success":
                # Generate report
                report_path = self.report_service.generate_documentation_report(
                    result.get("result", {})
                )
                result["report_path"] = report_path
            
            return result
        except Exception as e:
            return {
                "status": "error",
                "error": str(e)
            }
    
    def execute_code_quality_task(self, file_content: str, file_name: str) -> Dict[str, Any]:
        """Execute code quality analysis task"""
        try:
            result = self.workflow_service.execute_single_task(
                "CodeQualityTask",
                {
                    "file_path": file_name,
                    "file_content": file_content
                }
            )
            
            if result.get("status") == "success":
                # Generate report
                report_path = self.report_service.generate_code_quality_report(
                    result.get("result", {})
                )
                result["report_path"] = report_path
            
            return result
        except Exception as e:
            return {
                "status": "error",
                "error": str(e)
            }
    
    def execute_full_workflow(self, repo_url: str) -> Dict[str, Any]:
        """Execute complete workflow: Analysis → Documentation → Quality"""
        try:
            results = self.workflow_service.execute_workflow(
                ["RepositoryAnalysisTask", "DocumentationTask"],
                {"repo_url": repo_url}
            )
            
            # Generate workflow report
            report_path = self.report_service.generate_workflow_report(results)
            results["report_path"] = report_path
            
            return results
        except Exception as e:
            return {
                "status": "error",
                "error": str(e)
            }

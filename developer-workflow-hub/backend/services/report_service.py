from pathlib import Path
from typing import Dict, Any, List
from datetime import datetime
from config import Config

class ReportService:
    """Service to generate and save reports"""
    
    def __init__(self):
        self.reports_dir = Config.REPORTS_DIR
        self.report_format = "{timestamp}_{report_type}_report.txt"
    
    def generate_repository_report(self, repo_analysis: Dict[str, Any]) -> str:
        """Generate repository analysis report"""
        content = self._build_repository_report(repo_analysis)
        return self._save_report("repository", content)
    
    def generate_documentation_report(self, documentation: Dict[str, Any]) -> str:
        """Generate documentation report"""
        content = self._build_documentation_report(documentation)
        return self._save_report("documentation", content)
    
    def generate_code_quality_report(self, code_analysis: Dict[str, Any]) -> str:
        """Generate code quality report"""
        content = self._build_code_quality_report(code_analysis)
        return self._save_report("code-quality", content)
    
    def generate_workflow_report(self, workflow_results: Dict[str, Any]) -> str:
        """Generate complete workflow report"""
        content = self._build_workflow_report(workflow_results)
        return self._save_report("workflow", content)
    
    def _build_repository_report(self, data: Dict[str, Any]) -> str:
        """Build repository analysis report content"""
        lines = [
            "=" * 80,
            "REPOSITORY ANALYSIS REPORT",
            "=" * 80,
            f"\nGenerated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            f"\nRepository: {data.get('repository_name')}",
            f"Owner: {data.get('owner')}",
            f"URL: {data.get('repository_url')}",
            f"\nDescription:\n{data.get('description', 'N/A')}",
            f"\n\nStatistics:",
            f"  - Stars: {data.get('stars', 0)}",
            f"  - Forks: {data.get('forks', 0)}",
            f"  - Watchers: {data.get('watchers', 0)}",
            f"  - Open Issues: {data.get('open_issues', 0)}",
            f"  - Primary Language: {data.get('language', 'Unknown')}",
            f"  - Created: {data.get('created_at', 'N/A')}",
            f"  - Updated: {data.get('updated_at', 'N/A')}",
            f"\n\nTechnologies Used:",
        ]
        
        languages = data.get('languages', {})
        if languages:
            for lang, lines_count in sorted(languages.items(), key=lambda x: x[1], reverse=True):
                lines.append(f"  - {lang}: {lines_count} lines")
        else:
            lines.append("  - No language data available")
        
        topics = data.get('topics', [])
        if topics:
            lines.append(f"\n\nTopics: {', '.join(topics)}")
        
        lines.extend([
            f"\n\nClone Command:",
            f"  {data.get('clone_url', 'N/A')}",
            "\n" + "=" * 80
        ])
        
        return "\n".join(lines)
    
    def _build_documentation_report(self, data: Dict[str, Any]) -> str:
        """Build documentation report content"""
        lines = [
            "=" * 80,
            "PROJECT DOCUMENTATION REPORT",
            "=" * 80,
            f"\nGenerated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            f"\nProject: {data.get('project_name')}",
            f"Repository: {data.get('repository_url')}",
            f"\n\nOverview:",
            f"{data.get('description', 'No description')}",
            f"\n\nKey Features:",
        ]
        
        features = data.get('features', [])
        for i, feature in enumerate(features, 1):
            lines.append(f"  {i}. {feature}")
        
        lines.append(f"\n\nTechnologies:")
        technologies = data.get('technologies', [])
        for tech in technologies:
            lines.append(f"  - {tech}")
        
        lines.extend([
            f"\n\nStatistics:",
            f"  - Stars: {data.get('statistics', {}).get('stars', 0)}",
            f"  - Forks: {data.get('statistics', {}).get('forks', 0)}",
            f"  - Open Issues: {data.get('statistics', {}).get('open_issues', 0)}",
            f"\n\nUsage Instructions:",
            f"{data.get('usage', 'See repository documentation')}",
            f"\n\nClone & Setup:",
            f"  {data.get('clone_command', 'N/A')}",
            "\n" + "=" * 80
        ])
        
        return "\n".join(lines)
    
    def _build_code_quality_report(self, data: Dict[str, Any]) -> str:
        """Build code quality analysis report"""
        lines = [
            "=" * 80,
            "CODE QUALITY ANALYSIS REPORT",
            "=" * 80,
            f"\nGenerated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            f"\nFile: {data.get('file_name')}",
            f"Overall Score: {data.get('overall_score', 0)}/100",
            f"Total Issues Found: {data.get('total_issues', 0)}",
            f"\n\nIssues Detected:",
        ]
        
        issues = data.get('issues', [])
        if issues:
            for i, issue in enumerate(issues, 1):
                lines.append(f"\n  {i}. {issue.get('type', 'Unknown')}")
                lines.append(f"     Severity: {issue.get('severity', 'unknown')}")
                lines.append(f"     Line: {issue.get('line', 'N/A')}")
                lines.append(f"     Suggestion: {issue.get('suggestion', 'N/A')}")
        else:
            lines.append("  No issues detected!")
        
        lines.append(f"\n\nRecommendations:")
        recommendations = data.get('recommendations', [])
        for i, rec in enumerate(recommendations, 1):
            lines.append(f"  {i}. {rec}")
        
        lines.extend([
            f"\n\nScore Interpretation:",
            f"  - 90-100: Excellent",
            f"  - 70-89: Good",
            f"  - 50-69: Fair (improvements needed)",
            f"  - Below 50: Poor (significant refactoring recommended)",
            "\n" + "=" * 80
        ])
        
        return "\n".join(lines)
    
    def _build_workflow_report(self, data: Dict[str, Any]) -> str:
        """Build complete workflow execution report"""
        lines = [
            "=" * 80,
            "DEVELOPER WORKFLOW AUTOMATION REPORT",
            "=" * 80,
            f"\nExecution Date: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            f"Status: {data.get('status', 'Unknown').upper()}",
            f"Total Duration: {data.get('duration_seconds', 0):.2f} seconds",
            f"\n\nTasks Executed:",
        ]
        
        results = data.get('results', {})
        for task_name, task_data in results.items():
            if task_name != 'error':
                metadata = task_data.get('metadata', {})
                lines.append(f"\n  - {task_name}")
                lines.append(f"    Status: {metadata.get('status', 'unknown')}")
                lines.append(f"    Duration: {metadata.get('duration_seconds', 0):.2f}s")
        
        if 'error' in results:
            lines.append(f"\n\nErrors:")
            lines.append(f"  {results['error']}")
        
        lines.extend([
            "\n" + "=" * 80,
            "End of Report",
            "=" * 80
        ])
        
        return "\n".join(lines)
    
    def _save_report(self, report_type: str, content: str) -> str:
        """Save report to file"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"{timestamp}_{report_type}_report.txt"
        filepath = self.reports_dir / filename
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        
        return str(filepath)

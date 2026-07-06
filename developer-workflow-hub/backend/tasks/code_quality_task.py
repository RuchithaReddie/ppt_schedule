from tasks.base_task import BaseTask
from typing import Dict, Any, List
import re

class CodeQualityTask(BaseTask):
    """Task to analyze code quality"""
    
    def execute(self, **kwargs) -> Dict[str, Any]:
        """Analyze code quality from uploaded file"""
        self.start()
        
        try:
            file_path = kwargs.get("file_path")
            file_content = kwargs.get("file_content")
            
            if not file_path or not file_content:
                raise ValueError("file_path and file_content are required")
            
            issues = self._analyze_code(file_content)
            
            result = {
                "file_name": file_path,
                "total_issues": len(issues),
                "issues": issues,
                "overall_score": self._calculate_score(issues),
                "recommendations": self._generate_recommendations(issues)
            }
            
            self.complete(result)
            return result
            
        except Exception as e:
            self.fail(str(e))
            raise
    
    def _analyze_code(self, content: str) -> List[Dict[str, Any]]:
        """Perform code quality checks"""
        issues = []
        lines = content.split('\n')
        
        # Check 1: Long methods
        issues.extend(self._check_long_methods(lines))
        
        # Check 2: Large classes
        issues.extend(self._check_large_classes(lines))
        
        # Check 3: Hardcoded values
        issues.extend(self._check_hardcoded_values(lines))
        
        # Check 4: Poor variable names
        issues.extend(self._check_poor_variable_names(lines))
        
        # Check 5: Missing documentation
        issues.extend(self._check_missing_documentation(lines))
        
        return issues
    
    def _check_long_methods(self, lines: List[str]) -> List[Dict[str, Any]]:
        """Check for methods longer than 20 lines"""
        issues = []
        method_start = None
        method_name = None
        method_lines = 0
        
        for i, line in enumerate(lines, 1):
            if re.search(r'(public|private|protected)?\s*(static)?\s*\w+\s+\w+\s*\(', line):
                if method_start:
                    if method_lines > 20:
                        issues.append({
                            "type": "Long Method",
                            "line": method_start,
                            "method": method_name,
                            "lines": method_lines,
                            "severity": "medium",
                            "suggestion": "Consider breaking down this method into smaller functions"
                        })
                method_start = i
                method_name = re.search(r'(\w+)\s*\(', line)
                method_name = method_name.group(1) if method_name else "Unknown"
                method_lines = 0
            elif method_start:
                method_lines += 1
        
        return issues
    
    def _check_large_classes(self, lines: List[str]) -> List[Dict[str, Any]]:
        """Check for classes larger than 200 lines"""
        issues = []
        
        class_start = None
        class_name = None
        class_lines = 0
        brace_count = 0
        
        for i, line in enumerate(lines, 1):
            if re.search(r'(public|private|protected)?\s*class\s+(\w+)', line):
                class_start = i
                match = re.search(r'class\s+(\w+)', line)
                class_name = match.group(1) if match else "Unknown"
                class_lines = 0
                brace_count = 0
            
            if class_start:
                class_lines += 1
                brace_count += line.count('{') - line.count('}')
                
                if brace_count == 0 and class_lines > 1:
                    if class_lines > 200:
                        issues.append({
                            "type": "Large Class",
                            "line": class_start,
                            "class": class_name,
                            "lines": class_lines,
                            "severity": "medium",
                            "suggestion": "Consider splitting this class using Single Responsibility Principle"
                        })
                    class_start = None
        
        return issues
    
    def _check_hardcoded_values(self, lines: List[str]) -> List[Dict[str, Any]]:
        """Check for hardcoded values"""
        issues = []
        
        for i, line in enumerate(lines, 1):
            # Look for string literals
            if re.search(r'=["\'].*["\']', line) and 'final' not in line and 'static' not in line:
                match = re.search(r'=["\']([^"\']{10,})["\']', line)
                if match:
                    issues.append({
                        "type": "Hardcoded Value",
                        "line": i,
                        "content": match.group(1)[:50],
                        "severity": "low",
                        "suggestion": "Move this to a configuration file or constant"
                    })
            
            # Look for magic numbers
            if re.search(r'=\s*\d{2,}', line) and 'final' not in line and 'static' not in line:
                match = re.search(r'=\s*(\d+)', line)
                if match and int(match.group(1)) > 100:
                    issues.append({
                        "type": "Magic Number",
                        "line": i,
                        "value": match.group(1),
                        "severity": "low",
                        "suggestion": "Define this as a named constant"
                    })
        
        return issues[:5]  # Limit to top 5
    
    def _check_poor_variable_names(self, lines: List[str]) -> List[Dict[str, Any]]:
        """Check for poor variable naming"""
        issues = []
        poor_names = ['x', 'y', 'z', 'a', 'b', 'c', 'tmp', 'temp', 'data', 'obj']
        
        for i, line in enumerate(lines, 1):
            # Look for variable declarations
            if re.search(r'(int|String|boolean|var|let|const)\s+(\w+)', line):
                match = re.search(r'(int|String|boolean|var|let|const)\s+(\w+)', line)
                var_name = match.group(2) if match else None
                
                if var_name and var_name in poor_names:
                    issues.append({
                        "type": "Poor Variable Name",
                        "line": i,
                        "variable": var_name,
                        "severity": "low",
                        "suggestion": "Use a more descriptive variable name"
                    })
        
        return issues[:5]
    
    def _check_missing_documentation(self, lines: List[str]) -> List[Dict[str, Any]]:
        """Check for missing documentation/comments"""
        issues = []
        
        for i, line in enumerate(lines, 1):
            if re.search(r'(public|private|protected)\s+.*\(', line):
                # Check if there's a comment above
                if i > 1 and not lines[i-2].strip().startswith('//') and not lines[i-2].strip().startswith('/*'):
                    issues.append({
                        "type": "Missing Documentation",
                        "line": i,
                        "severity": "low",
                        "suggestion": "Add a comment or JavaDoc for this method"
                    })
        
        return issues[:5]
    
    def _calculate_score(self, issues: List[Dict[str, Any]]) -> float:
        """Calculate overall code quality score"""
        if not issues:
            return 100.0
        
        severity_points = {"critical": 25, "high": 10, "medium": 5, "low": 2}
        total_points = sum(severity_points.get(issue.get("severity", "low"), 2) for issue in issues)
        
        score = max(0, 100 - total_points)
        return round(score, 2)
    
    def _generate_recommendations(self, issues: List[Dict[str, Any]]) -> List[str]:
        """Generate actionable recommendations"""
        recommendations = []
        
        issue_types = {}
        for issue in issues:
            issue_type = issue.get("type", "Unknown")
            issue_types[issue_type] = issue_types.get(issue_type, 0) + 1
        
        if issue_types.get("Long Method", 0) > 0:
            recommendations.append("Refactor long methods by extracting smaller functions")
        
        if issue_types.get("Large Class", 0) > 0:
            recommendations.append("Apply Single Responsibility Principle to break down classes")
        
        if issue_types.get("Hardcoded Value", 0) > 0:
            recommendations.append("Extract configuration values to constants or environment variables")
        
        if issue_types.get("Poor Variable Name", 0) > 0:
            recommendations.append("Use meaningful, descriptive names for variables")
        
        if issue_types.get("Missing Documentation", 0) > 0:
            recommendations.append("Add comprehensive documentation and JavaDoc comments")
        
        return recommendations

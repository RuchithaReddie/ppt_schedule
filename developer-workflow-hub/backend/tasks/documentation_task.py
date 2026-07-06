from tasks.base_task import BaseTask
from utils.github_client import GitHubClient
from typing import Dict, Any
import re

class DocumentationTask(BaseTask):
    """Task to generate documentation from repository analysis"""
    
    def __init__(self):
        super().__init__()
        self.github_client = GitHubClient()
    
    def execute(self, **kwargs) -> Dict[str, Any]:
        """Generate documentation from repository data"""
        self.start()
        
        try:
            repo_url = kwargs.get("repo_url")
            if not repo_url:
                raise ValueError("repo_url is required")
            
            # Fetch repository data
            repo_data = self.github_client.get_repository(repo_url)
            languages = self.github_client.get_repository_languages(repo_url)
            topics = self.github_client.get_repository_topics(repo_url)
            readme = self.github_client.get_repository_readme(repo_url)
            
            # Extract features from description and README
            features = self._extract_features(repo_data, readme)
            
            documentation = {
                "project_name": repo_data.get("name"),
                "description": repo_data.get("description") or "No description available",
                "features": features,
                "technologies": list(languages.keys()) if languages else [],
                "repository_url": repo_url,
                "topics": topics,
                "statistics": {
                    "stars": repo_data.get("stargazers_count"),
                    "forks": repo_data.get("forks_count"),
                    "watchers": repo_data.get("watchers_count"),
                    "open_issues": repo_data.get("open_issues_count")
                },
                "usage": self._generate_usage_instructions(repo_data),
                "readme_preview": readme[:500] if readme else None,
                "clone_command": f"git clone {repo_data.get('clone_url')}"
            }
            
            self.complete(documentation)
            return documentation
            
        except Exception as e:
            self.fail(str(e))
            raise
    
    def _extract_features(self, repo_data: Dict[str, Any], readme: str = None) -> list:
        """Extract features from repository data and README"""
        features = []
        
        # Extract from description
        description = repo_data.get("description", "")
        if description:
            # Split by common separators
            parts = re.split(r'[,;.-]', description)
            features.extend([p.strip() for p in parts if len(p.strip()) > 5][:5])
        
        # Extract from README if available
        if readme:
            # Look for bullet points
            lines = readme.split('\n')
            for line in lines:
                if line.strip().startswith(('- ', '* ', '+ ')):
                    feature = line.strip()[2:].strip()
                    if len(feature) > 5 and feature not in features:
                        features.append(feature)
                    if len(features) >= 10:
                        break
        
        return features[:10] if features else ["Feature detection in progress"]
    
    def _generate_usage_instructions(self, repo_data: Dict[str, Any]) -> str:
        """Generate basic usage instructions"""
        language = repo_data.get("language", "").lower() or "unknown language"
        
        instructions = {
            "python": "1. Install dependencies: pip install -r requirements.txt\n2. Run: python main.py",
            "javascript": "1. Install dependencies: npm install\n2. Run: npm start",
            "java": "1. Build: mvn clean install\n2. Run: mvn exec:java",
            "go": "1. Build: go build\n2. Run: ./executable",
            "rust": "1. Build: cargo build\n2. Run: cargo run"
        }
        
        return instructions.get(language, f"See repository README for {language} setup instructions")

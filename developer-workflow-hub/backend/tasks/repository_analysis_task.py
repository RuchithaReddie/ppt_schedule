from tasks.base_task import BaseTask
from utils.github_client import GitHubClient
from typing import Dict, Any

class RepositoryAnalysisTask(BaseTask):
    """Task to analyze a GitHub repository"""
    
    def __init__(self):
        super().__init__()
        self.github_client = GitHubClient()
    
    def execute(self, **kwargs) -> Dict[str, Any]:
        """Execute repository analysis"""
        self.start()
        
        try:
            repo_url = kwargs.get("repo_url")
            if not repo_url:
                raise ValueError("repo_url is required")
            
            # Fetch repository data
            repo_data = self.github_client.get_repository(repo_url)
            languages = self.github_client.get_repository_languages(repo_url)
            topics = self.github_client.get_repository_topics(repo_url)
            
            result = {
                "repository_url": repo_url,
                "repository_name": repo_data.get("name"),
                "owner": repo_data.get("owner", {}).get("login"),
                "description": repo_data.get("description"),
                "stars": repo_data.get("stargazers_count"),
                "forks": repo_data.get("forks_count"),
                "watchers": repo_data.get("watchers_count"),
                "open_issues": repo_data.get("open_issues_count"),
                "language": repo_data.get("language"),
                "is_private": repo_data.get("private"),
                "created_at": repo_data.get("created_at"),
                "updated_at": repo_data.get("updated_at"),
                "pushed_at": repo_data.get("pushed_at"),
                "clone_url": repo_data.get("clone_url"),
                "topics": topics,
                "languages": languages,
                "contributors_url": repo_data.get("contributors_url")
            }
            
            self.complete(result)
            return result
            
        except Exception as e:
            self.fail(str(e))
            raise

import requests
from typing import Dict, Any, Optional
from config import Config

class GitHubClient:
    """Client for interacting with GitHub API"""
    
    def __init__(self, token: Optional[str] = None):
        self.base_url = Config.GITHUB_API_BASE_URL
        self.headers = {
            "Accept": "application/vnd.github.v3+json",
            "User-Agent": "DeveloperWorkflowHub"
        }
        if token:
            self.headers["Authorization"] = f"token {token}"
    
    def extract_repo_info(self, repo_url: str) -> tuple:
        """Extract owner and repo name from GitHub URL"""
        # Handle various URL formats
        if "github.com/" in repo_url:
            parts = repo_url.split("github.com/")[-1].strip("/").split("/")
            if len(parts) >= 2:
                return parts[0], parts[1].replace(".git", "")
        raise ValueError("Invalid GitHub repository URL")
    
    def get_repository(self, repo_url: str) -> Dict[str, Any]:
        """Fetch repository information from GitHub API"""
        owner, repo = self.extract_repo_info(repo_url)
        url = f"{self.base_url}/repos/{owner}/{repo}"
        
        try:
            response = requests.get(url, headers=self.headers, timeout=10)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            raise Exception(f"Failed to fetch repository: {str(e)}")
    
    def get_repository_readme(self, repo_url: str) -> Optional[str]:
        """Fetch README content from repository"""
        owner, repo = self.extract_repo_info(repo_url)
        url = f"{self.base_url}/repos/{owner}/{repo}/readme"
        
        try:
            response = requests.get(url, headers=self.headers, timeout=10)
            if response.status_code == 200:
                import base64
                content = response.json().get("content", "")
                return base64.b64decode(content).decode("utf-8")
            return None
        except requests.exceptions.RequestException:
            return None
    
    def get_repository_languages(self, repo_url: str) -> Dict[str, int]:
        """Fetch programming languages used in repository"""
        owner, repo = self.extract_repo_info(repo_url)
        url = f"{self.base_url}/repos/{owner}/{repo}/languages"
        
        try:
            response = requests.get(url, headers=self.headers, timeout=10)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException:
            return {}
    
    def get_repository_topics(self, repo_url: str) -> list:
        """Fetch repository topics/tags"""
        owner, repo = self.extract_repo_info(repo_url)
        url = f"{self.base_url}/repos/{owner}/{repo}/topics"
        
        headers = self.headers.copy()
        headers["Accept"] = "application/vnd.github.mercy-preview+json"
        
        try:
            response = requests.get(url, headers=headers, timeout=10)
            if response.status_code == 200:
                return response.json().get("names", [])
            return []
        except requests.exceptions.RequestException:
            return []

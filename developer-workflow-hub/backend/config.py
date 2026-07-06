import os
from pathlib import Path

# Configuration
class Config:
    GITHUB_API_BASE_URL = "https://api.github.com"
    REPORTS_DIR = Path(__file__).parent.parent / "reports"
    UPLOAD_FOLDER = Path(__file__).parent.parent / "uploads"
    MAX_FILE_SIZE = 5 * 1024 * 1024  # 5MB
    
    # Create directories if they don't exist
    REPORTS_DIR.mkdir(exist_ok=True)
    UPLOAD_FOLDER.mkdir(exist_ok=True)

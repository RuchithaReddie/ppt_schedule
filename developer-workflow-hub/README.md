# Developer Workflow Automation Hub

## 🚀 Overview

A professional **Developer Workflow Automation Hub** that automates repetitive developer activities using AI and clean architecture principles. The application demonstrates SOLID principles, modular design, and REST API integration.

### Core Concept

> "The goal of our application is to automate repetitive developer activities using AI. Every task follows the same workflow: the frontend invokes a REST API, the backend executes the requested task, generates a report, stores it locally, and returns the result."

---

## 🎯 Features

### 1. **Repository Analysis**
- Analyze any GitHub repository
- Fetch repository details via GitHub API
- Extract technologies, statistics, and metadata
- Generate professional analysis reports

### 2. **Documentation Generator**
- Create comprehensive project documentation
- Extract features and setup instructions
- List technologies and key statistics
- Generate README-style documentation

### 3. **Code Quality Analysis**
- Upload and analyze code files
- Detect:
  - Long methods (>20 lines)
  - Large classes (>200 lines)
  - Hardcoded values
  - Poor variable naming
  - Missing documentation
- Generate quality scores and recommendations

### 4. **Full Workflow Orchestration**
- Execute multiple tasks in sequence
- Pass results between tasks
- Generate comprehensive workflow reports
- Track execution time and status

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│         Angular-like Frontend           │
│    (HTML/CSS/JavaScript SPA)            │
└─────────────────────────────────────────┘
                    │
                    ↓
        ┌───────────────────────┐
        │   Flask REST API      │
        │   (Port 5000)         │
        └───────────────────────┘
                    │
        ┌───────────┴───────────┐
        ↓                       ↓
    ┌─────────────┐    ┌──────────────────┐
    │TaskController   WorkflowService    │
    └─────────────┘    └──────────────────┘
        │                       │
        └───────────┬───────────┘
                    ↓
        ┌──────────────────────┐
        │   Task Library       │
        ├──────────────────────┤
        │ • BaseTask (Abstract)│
        │ • RepositoryTask     │
        │ • DocumentationTask  │
        │ • CodeQualityTask    │
        └──────────────────────┘
                    │
        ┌───────────┴────────────┐
        ↓                        ↓
    ┌──────────┐          ┌─────────────┐
    │GitHub API│          │ReportService│
    └──────────┘          └─────────────┘
```

---

## 📁 Project Structure

```
developer-workflow-hub/
├── backend/
│   ├── app.py                 # Main Flask application
│   ├── config.py              # Configuration settings
│   ├── requirements.txt        # Python dependencies
│   │
│   ├── tasks/
│   │   ├── __init__.py
│   │   ├── base_task.py       # Abstract base class
│   │   ├── repository_analysis_task.py
│   │   ├── documentation_task.py
│   │   └── code_quality_task.py
│   │
│   ├── services/
│   │   ├── __init__.py
│   │   ├── workflow_service.py # Orchestrates task execution
│   │   └── report_service.py   # Generates reports
│   │
│   ├── controllers/
│   │   ├── __init__.py
│   │   └── task_controller.py  # Handles API requests
│   │
│   └── utils/
│       ├── __init__.py
│       └── github_client.py    # GitHub API integration
│
├── frontend/
│   ├── index.html              # Single Page Application
│   ├── style.css               # Responsive styling
│   └── script.js               # API communication
│
├── reports/                    # Generated reports directory
└── README.md
```

---

## 🛠️ Technology Stack

### Backend
- **Framework**: Flask (Python)
- **API**: REST API with CORS support
- **External**: GitHub API v3
- **Architecture**: SOLID Principles, Clean Code

### Frontend
- **HTML5** for structure
- **CSS3** for responsive design
- **Vanilla JavaScript** for interactivity
- **Fetch API** for HTTP requests

### Design Patterns
- **Abstract Base Class**: `BaseTask` for task implementations
- **Service Layer**: `WorkflowService`, `ReportService`
- **Controller Pattern**: `TaskController` for API handling
- **Dependency Injection**: Loose coupling between components

---

## 🚀 Getting Started

### Prerequisites
- Python 3.8+
- pip (Python package manager)
- Modern web browser

### Installation

1. **Clone the repository**
```bash
cd developer-workflow-hub/backend
```

2. **Create virtual environment** (optional but recommended)
```bash
python -m venv venv

# On Windows
venv\Scripts\activate

# On macOS/Linux
source venv/bin/activate
```

3. **Install dependencies**
```bash
pip install -r requirements.txt
```

### Running the Application

1. **Start the backend server**
```bash
python app.py
```

The server will start on `http://localhost:5000`

2. **Open the frontend**
   - Navigate to `frontend/index.html` in your browser
   - Or use a local server:
   ```bash
   # Python 3
   python -m http.server 8000 --directory frontend
   ```
   - Then visit `http://localhost:8000`

---

## 📊 API Endpoints

### Health Check
```
GET /
```

### List Available Tasks
```
GET /api/tasks
```

### Repository Analysis
```
POST /api/tasks/repository-analysis
Content-Type: application/json

{
  "repo_url": "https://github.com/owner/repository"
}
```

### Documentation Generation
```
POST /api/tasks/documentation
Content-Type: application/json

{
  "repo_url": "https://github.com/owner/repository"
}
```

### Code Quality Analysis
```
POST /api/tasks/code-quality
Content-Type: multipart/form-data

file: <Java/Code file>
```

### Full Workflow Execution
```
POST /api/workflow/execute
Content-Type: application/json

{
  "repo_url": "https://github.com/owner/repository"
}
```

### List Reports
```
GET /api/reports
```

### Download Report
```
GET /api/reports/download/<filename>
```

---

## 💡 Design Decisions

### 1. **Task Abstraction**
All tasks inherit from `BaseTask`, ensuring consistent interface:
- `execute()` - Execute the task
- `start()` / `complete()` / `fail()` - Lifecycle management
- `get_metadata()` - Task metadata

### 2. **Service Layer**
- **WorkflowService**: Orchestrates task execution and manages task sequence
- **ReportService**: Centralizes report generation logic

### 3. **REST API Architecture**
- Clean, RESTful endpoints
- CORS enabled for frontend communication
- Error handling and status codes
- JSON response format

### 4. **Code Quality Checks**
Implements basic static analysis:
- Long method detection
- Large class detection
- Hardcoded values detection
- Poor naming conventions
- Missing documentation

### 5. **Report Generation**
- Automatic report generation for each task
- Timestamped filenames
- Local storage in `reports/` directory
- Downloadable via API

---

## 🔄 Workflow Example

### Complete Workflow Flow:
```
1. User submits GitHub URL
   ↓
2. RepositoryAnalysisTask executes
   → Fetches repo data from GitHub API
   → Saves repository-report.txt
   ↓
3. DocumentationTask executes
   → Uses repository data from previous task
   → Generates documentation-report.txt
   ↓
4. WorkflowService generates workflow-report.txt
   ↓
5. Results returned to frontend
   ↓
6. User downloads reports
```

---

## 📝 Example Usage

### Repository Analysis
```bash
curl -X POST http://localhost:5000/api/tasks/repository-analysis \
  -H "Content-Type: application/json" \
  -d '{"repo_url": "https://github.com/torvalds/linux"}'
```

### Code Quality Analysis
```bash
curl -X POST http://localhost:5000/api/tasks/code-quality \
  -F "file=@MyFile.java"
```

---

## 🎓 SOLID Principles Implementation

| Principle | Implementation |
|-----------|-----------------|
| **S**ingle Responsibility | Each task has one responsibility; services handle specific concerns |
| **O**pen/Closed | Can add new tasks by extending `BaseTask` without modifying existing code |
| **L**iskov Substitution | All tasks implement the same interface via `BaseTask` |
| **I**nterface Segregation | Services expose only necessary methods |
| **D**ependency Inversion | Controllers depend on abstractions (services) not concrete implementations |

---

## 🧪 Testing the Application

1. **Test Repository Analysis**
   - URL: `https://github.com/torvalds/linux`
   - Should return repository statistics and metadata

2. **Test Documentation Generation**
   - Same URL as repository analysis
   - Should generate feature list and setup instructions

3. **Test Code Quality**
   - Upload any Java/Python file
   - Should detect issues and generate quality score

4. **Test Full Workflow**
   - Provide GitHub URL
   - Should execute both analysis and documentation tasks

---

## 📋 Generated Reports

Reports are saved in the `reports/` directory:
- `YYYYMMDD_HHMMSS_repository_report.txt`
- `YYYYMMDD_HHMMSS_documentation_report.txt`
- `YYYYMMDD_HHMMSS_code-quality_report.txt`
- `YYYYMMDD_HHMMSS_workflow_report.txt`

---

## 🔐 Error Handling

The application includes comprehensive error handling:
- Invalid GitHub URLs
- API rate limiting
- File upload validation
- Graceful error messages

---

## 🚀 Deployment Considerations

### For Production:
1. Remove `debug=True` from Flask app
2. Add authentication/authorization
3. Implement rate limiting
4. Add caching for GitHub API responses
5. Use environment variables for configuration
6. Deploy with a production WSGI server (Gunicorn, uWSGI)

---

## 📚 Training Alignment

This project demonstrates:
- ✅ **GitHub Integration** - Repository analysis via GitHub API
- ✅ **AI/Automation** - Automated task execution and report generation
- ✅ **Clean Code** - SOLID principles, meaningful naming, modular design
- ✅ **REST APIs** - Backend API design and frontend integration
- ✅ **Documentation** - Auto-generated reports and structured documentation
- ✅ **Codebase Understanding** - Repository analysis and documentation

---

## 📧 Support

For issues or questions, please refer to the code documentation and comments throughout the project.

---

## 📄 License

This project is provided as-is for educational purposes.

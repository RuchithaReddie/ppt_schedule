# Visual Architecture Diagrams

## 1. System Architecture Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                       USER INTERFACE                             │
│                    (Single Page App)                             │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  Repository Analysis │ Documentation │ Code Quality │ ...  │ │
│  │  Workflow Tab        │ Reports Tab    │ Full Workflow       │ │
│  └────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
                             │
                             │ HTTP Requests (JSON)
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│                    REST API BACKEND                              │
│                   (Flask, Port 5000)                             │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │  POST /api/tasks/repository-analysis                       │ │
│  │  POST /api/tasks/documentation                             │ │
│  │  POST /api/tasks/code-quality                              │ │
│  │  POST /api/workflow/execute                                │ │
│  │  GET  /api/reports                                         │ │
│  └────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
                             │
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│                 BUSINESS LOGIC LAYER                             │
│  ┌──────────────┐  ┌──────────────────┐  ┌──────────────────┐  │
│  │TaskController│  │WorkflowService   │  │ReportService     │  │
│  │ - Validates  │  │ - Orchestrates   │  │ - Generates      │  │
│  │ - Routes     │  │ - Manages tasks  │  │ - Stores files   │  │
│  └──────────────┘  └──────────────────┘  └──────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
                             │
                             ↓
┌──────────────────────────────────────────────────────────────────┐
│                  TASK EXECUTION LAYER                            │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                     BaseTask (Abstract)                      ││
│  │  ┌──────────────────┐  ┌───────────────┐  ┌──────────────┐ ││
│  │  │RepositoryTask   │  │DocTask        │  │QualityTask   │ ││
│  │  │ - Fetch metadata │  │- Extract data │  │- Analyze     │ ││
│  │  │ - Get languages  │  │- Generate doc │  │- Score code  │ ││
│  │  │ - Extract topics │  │- Setup guide  │  │- Recommend   │ ││
│  │  └──────────────────┘  └───────────────┘  └──────────────┘ ││
│  └─────────────────────────────────────────────────────────────┘│
└──────────────────────────────────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        ↓                    ↓                     ↓
┌──────────────┐      ┌────────────────┐   ┌─────────────────┐
│ GitHub API   │      │ File System    │   │ Code Analyzer   │
│ - Repository │      │ - Local Files  │   │ - Parse code    │
│ - Languages  │      │ - Report Docs  │   │ - Quality Check │
│ - Topics     │      │ - Storage      │   │ - Metrics       │
└──────────────┘      └────────────────┘   └─────────────────┘
```

---

## 2. Task Execution Flow

```
┌─────────────────────────────────────────┐
│    User Submits Request                 │
│  (GitHub URL or Code File)              │
└─────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  Frontend Validation                    │
│  - Check input not empty               │
│  - Format URL if needed                │
└─────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  Send HTTP Request to API               │
│  POST /api/tasks/{task-name}            │
│  Content-Type: application/json         │
└─────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  TaskController Receives Request        │
│  - Validates input                      │
│  - Prepares parameters                  │
│  - Calls WorkflowService                │
└─────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  WorkflowService Routes to Task         │
│  - Finds task instance                  │
│  - Calls task.execute()                 │
└─────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  Task Execution                         │
│  - task.start()                         │
│  - Process data                         │
│  - Call external APIs if needed         │
│  - task.complete(result)                │
└─────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  ReportService Generates Report         │
│  - Format data                          │
│  - Create text document                 │
│  - Save with timestamp                  │
│  - Return file path                     │
└─────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  Response to Frontend                   │
│  {                                      │
│    status: "success",                   │
│    result: {...},                       │
│    report_path: "path/to/file.txt"      │
│  }                                      │
└─────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  Frontend Displays Results              │
│  - Show metadata                        │
│  - Display data                         │
│  - Provide download option              │
└─────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────┐
│  User Downloads Report                  │
│  - Browser downloads file               │
│  - Local storage                        │
└─────────────────────────────────────────┘
```

---

## 3. Task Class Hierarchy

```
                    ┌──────────────────────┐
                    │   BaseTask (ABC)     │
                    │  ─────────────────── │
                    │  + execute()         │
                    │  + start()           │
                    │  + complete()        │
                    │  + fail()            │
                    │  + get_duration()    │
                    │  + get_metadata()    │
                    └──────────────────────┘
                              △
                    ┌─────────┼─────────┐
                    │         │         │
                    ↓         ↓         ↓
        ┌──────────────────┐ ┌──────────────┐ ┌────────────────┐
        │RepositoryAnalysis│ │Documentation │ │CodeQuality     │
        ├──────────────────┤ ├──────────────┤ ├────────────────┤
        │+ execute()       │ │+ execute()   │ │+ execute()     │
        │+ github_client   │ │+ github_cli  │ │+ file_content  │
        └──────────────────┘ └──────────────┘ └────────────────┘
            │                    │                   │
            ↓                    ↓                   ↓
        Returns:             Returns:            Returns:
        - repo_data          - documentation     - issues
        - languages          - features          - score
        - topics             - technologies      - recommendations
        - statistics         - setup_guide       - analysis
```

---

## 4. Request-Response Flow (Example)

```
┌─────────────────────────────────────────────────────────────┐
│                  CLIENT (Frontend)                          │
│                                                              │
│  User Input: https://github.com/torvalds/linux            │
│       │                                                     │
│       └─→ analyzeRepository()                             │
│           │                                                │
│           └─→ fetch(POST /api/tasks/repository-analysis, {│
│                   repo_url: "..."                          │
│               })                                           │
└─────────────────────────────────────────────────────────────┘
                         │
                    HTTP POST
                  (JSON Body)
                         │
                         ↓
┌─────────────────────────────────────────────────────────────┐
│                 SERVER (Flask Backend)                      │
│                                                              │
│  @app.route("/api/tasks/repository-analysis", "POST")      │
│  def repository_analysis():                                │
│      data = request.get_json()                             │
│      result = task_controller.execute(data)               │
│      return jsonify(result)                               │
└─────────────────────────────────────────────────────────────┘
                         │
                   HTTP Response
                    (JSON Body)
                         │
                         ↓
┌─────────────────────────────────────────────────────────────┐
│                  CLIENT (Frontend)                          │
│                                                              │
│  Response: {                                               │
│    status: "success",                                      │
│    result: {                                               │
│      repository_name: "linux",                             │
│      owner: "torvalds",                                    │
│      stars: 186000,                                        │
│      languages: { C: 45%, Python: 30%, ... },             │
│      ...                                                   │
│    },                                                      │
│    report_path: "reports/20240707_120000_repo_report.txt" │
│  }                                                         │
│       │                                                    │
│       └─→ displayRepositoryResult(data)                  │
│           └─→ Render UI with results                     │
│               └─→ Show download button                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Workflow Orchestration Flow

```
Start: User Submits GitHub URL
       │
       ↓
┌──────────────────────────────────────┐
│  WorkflowService.execute_workflow()  │
│  Tasks: ["RepositoryTask",           │
│          "DocumentationTask"]         │
└──────────────────────────────────────┘
       │
       ↓
┌──────────────────────────────────────┐
│  Task 1: RepositoryAnalysisTask      │
│  Input: { repo_url: "..." }          │
│  ├─ task.start()                     │
│ │ ├─ GitHubClient.get_repository()   │
│  │ ├─ GitHubClient.get_languages()   │
│  │ └─ GitHubClient.get_topics()      │
│  └─ task.complete(result)            │
│     Returns: repo_data               │
└──────────────────────────────────────┘
       │
       ├─ Merge: inputs.update(repo_data)
       │
       ↓
┌──────────────────────────────────────┐
│  Task 2: DocumentationTask           │
│  Input: { repo_url, ...repo_data }   │
│  ├─ task.start()                     │
│  │ ├─ Extract features from desc     │
│  │ ├─ Get technologies               │
│  │ └─ Generate setup guide           │
│  └─ task.complete(result)            │
│     Returns: documentation_data      │
└──────────────────────────────────────┘
       │
       ↓
┌──────────────────────────────────────┐
│  All Tasks Complete                  │
│  Combined Results:                   │
│  {                                   │
│    "status": "completed",            │
│    "results": {                      │
│      "Task1": {...},                 │
│      "Task2": {...}                  │
│    }                                 │
│  }                                   │
└──────────────────────────────────────┘
       │
       ↓
┌──────────────────────────────────────┐
│  ReportService.generate_workflow()   │
│  Creates: workflow-report.txt        │
│  Saves to: reports/                  │
└──────────────────────────────────────┘
       │
       ↓
End: Return to User with Report Download
```

---

## 6. Code Quality Analysis Process

```
┌──────────────────────────┐
│  User Uploads Code File  │
│  (Java, Python, etc.)    │
└──────────────────────────┘
            │
            ↓
┌──────────────────────────────────────┐
│  Frontend: Read File Content         │
│  - Use FileReader API                │
│  - Convert to string                 │
│  - Send to backend                   │
└──────────────────────────────────────┘
            │
            ↓
┌──────────────────────────────────────┐
│  Backend: CodeQualityTask.execute()  │
│  Input: file_content, file_name      │
└──────────────────────────────────────┘
            │
            ├──────────┬──────────┬──────────┬──────────┐
            ↓          ↓          ↓          ↓          ↓
    ┌────────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌──────────┐
    │Check Long  │  │Check Big │  │Hardcoded│  │Poor Var │  │Missing   │
    │Methods     │  │Classes   │  │Values   │  │Names    │  │Docs      │
    │(>20 lines) │  │(>200L)   │  │Strings  │  │a,b,tmp  │  │Comments  │
    └────────────┘  └─────────┘  └─────────┘  └─────────┘  └──────────┘
            │          │          │          │          │
            └──────────┴──────────┴──────────┴──────────┘
                              │
                              ↓
                    ┌──────────────────┐
                    │  Collect Issues  │
                    │  - Type          │
                    │  - Line number   │
                    │  - Severity      │
                    │  - Suggestion    │
                    └──────────────────┘
                              │
                              ↓
                    ┌──────────────────┐
                    │Calculate Score   │
                    │ 100 - penalties  │
                    │ (0-100 scale)    │
                    └──────────────────┘
                              │
                              ↓
                    ┌──────────────────┐
                    │Generate Recs     │
                    │Based on issues   │
                    │Actionable advice │
                    └──────────────────┘
                              │
                              ↓
                    ┌──────────────────┐
                    │Create Report     │
                    │code-quality.txt  │
                    │Save & Download   │
                    └──────────────────┘
```

---

## 7. Error Handling Flow

```
┌──────────────────────────────┐
│    Request Received          │
└──────────────────────────────┘
            │
            ↓
    ┌───────────────────┐
    │Input Valid?       │
    └───────────────────┘
        │         │
      YES        NO
        │         │
        ↓         └──→ ┌──────────────────┐
    ┌─────────────┐    │Return 400 Error  │
    │Execute Task │    │Bad Request       │
    └─────────────┘    └──────────────────┘
        │
        ↓
    ┌─────────────────────┐
    │Task Succeeds?       │
    └─────────────────────┘
        │         │
      YES        NO
        │         │
        ↓         └──→ ┌──────────────────┐
    ┌──────────────┐    │task.fail(error)  │
    │Generate Report   │Return 400 Error  │
    │Save File     │    │Error Message     │
    └──────────────┘    └──────────────────┘
        │
        ↓
    ┌──────────────────┐
    │Return 200 OK     │
    │With Result Data  │
    │& Report Path     │
    └──────────────────┘
        │
        ↓
    ┌──────────────────┐
    │Frontend Handles  │
    │- Display result  │
    │- Show download   │
    │- Error display   │
    └──────────────────┘
```

---

## 8. SOLID Principles in Architecture

```
┌─────────────────────────────────────────────────────────┐
│              SOLID Design Implementation                │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  S - Single Responsibility                             │
│  ├─ BaseTask → Only task lifecycle                     │
│  ├─ RepoTask → Only repository analysis               │
│  ├─ WorkflowService → Only orchestration              │
│  └─ ReportService → Only report generation            │
│                                                          │
│  O - Open/Closed                                       │
│  ├─ Add new tasks without modifying BaseTask          │
│  ├─ Extend without changing existing code             │
│  └─ Open for extension, closed for modification       │
│                                                          │
│  L - Liskov Substitution                               │
│  ├─ All tasks are interchangeable                      │
│  ├─ WorkflowService doesn't care about type           │
│  └─ task.execute() works for all                       │
│                                                          │
│  I - Interface Segregation                             │
│  ├─ Services expose only needed methods               │
│  ├─ No fat interfaces                                  │
│  └─ Clients depend on specific interfaces             │
│                                                          │
│  D - Dependency Inversion                              │
│  ├─ Depend on abstractions (BaseTask)                 │
│  ├─ Not on concrete implementations                    │
│  └─ Easy to swap implementations                       │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## 9. API Endpoint Usage Diagram

```
┌────────────────────────────────────────────────────────┐
│                  Available Endpoints                   │
├────────────────────────────────────────────────────────┤
│                                                        │
│  GET / 
│  └─→ Health Check                                    │
│      Returns: {status: "online", ...}               │
│                                                     │
│  GET /api/tasks                                     │
│  └─→ List Available Tasks                          │
│      Returns: [{name, description, endpoint}, ...] │
│                                                     │
│  POST /api/tasks/repository-analysis               │
│  Input: {repo_url: "..."}                           │
│  └─→ RepositoryAnalysisTask                        │
│      Returns: {status, result, report_path}        │
│                                                     │
│  POST /api/tasks/documentation                     │
│  Input: {repo_url: "..."}                           │
│  └─→ DocumentationTask                             │
│      Returns: {status, result, report_path}        │
│                                                     │
│  POST /api/tasks/code-quality                      │
│  Input: multipart file upload                      │
│  └─→ CodeQualityTask                               │
│      Returns: {status, result, report_path}        │
│                                                     │
│  POST /api/workflow/execute                        │
│  Input: {repo_url: "..."}                           │
│  └─→ Execute RepositoryTask + DocumentationTask    │
│      Returns: {status, results, report_path}       │
│                                                     │
│  GET /api/reports                                  │
│  └─→ List All Generated Reports                    │
│      Returns: [{name, path, created}, ...]         │
│                                                     │
│  GET /api/reports/download/<filename>              │
│  └─→ Download Specific Report                      │
│      Returns: File download                        │
│                                                     │
└────────────────────────────────────────────────────────┘
```

---

## 10. File Organization Structure

```
developer-workflow-hub/
│
├─ 📄 Documentation Files
│  ├── README.md (Main documentation)
│  ├── QUICKSTART.md (Getting started)
│  ├── ARCHITECTURE.md (Technical details)
│  ├── PROJECT_SUMMARY.md (Summary)
│  ├── PRESENTATION_GUIDE.md (Demo guide)
│  └── This file (Diagrams)
│
├─ 🔧 Backend (Python/Flask)
│  ├── app.py (Main Flask app)
│  ├── config.py (Settings)
│  ├── requirements.txt (Dependencies)
│  │
│  ├── tasks/ (Execution Engine)
│  │  ├── __init__.py
│  │  ├── base_task.py (Abstract)
│  │  ├── repository_analysis_task.py
│  │  ├── documentation_task.py
│  │  └── code_quality_task.py
│  │
│  ├── services/ (Business Logic)
│  │  ├── __init__.py
│  │  ├── workflow_service.py
│  │  └── report_service.py
│  │
│  ├── controllers/ (API Handlers)
│  │  ├── __init__.py
│  │  └── task_controller.py
│  │
│  └── utils/ (Utilities)
│      ├── __init__.py
│      └── github_client.py
│
├─ 🎨 Frontend (HTML/CSS/JS)
│  ├── index.html (UI)
│  ├── style.css (Styling)
│  └── script.js (Logic)
│
└─ 📊 reports/ (Output)
   └── (Generated report files)
```

---

These diagrams provide visual representations of:
1. Complete system architecture
2. Task execution flow
3. Class hierarchy
4. Request-response patterns
5. Workflow orchestration
6. Code quality analysis
7. Error handling
8. SOLID principles
9. API endpoints
10. File organization

Use these in presentations and documentation!

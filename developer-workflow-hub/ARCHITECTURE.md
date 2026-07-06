# Architecture Documentation

## System Overview

The **Developer Workflow Automation Hub** follows a modern, scalable architecture that separates concerns and implements SOLID principles throughout.

```
┌────────────────────────────────────────────┐
│         Frontend (SPA)                      │
│    HTML5 + CSS3 + Vanilla JavaScript       │
│    - Single Page Application               │
│    - RESTful API Communication            │
└────────────────────────────────────────────┘
                     │
                     │ HTTP Requests
                     ↓
┌────────────────────────────────────────────┐
│         Flask Backend Server               │
│         REST API (Port 5000)               │
│    - CORS Enabled                          │
│    - Error Handling                        │
│    - Request Routing                       │
└────────────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        ↓            ↓            ↓
┌──────────────┐  ┌─────────────────────┐  ┌──────────────┐
│TaskController│  │WorkflowService      │  │ReportService │
│  - Route     │  │  - Orchestrate      │  │ - Generate   │
│  - Validate  │  │  - Execute tasks    │  │ - Storage    │
│  - Transform │  │  - Track progress   │  │ - Format     │
└──────────────┘  └─────────────────────┘  └──────────────┘
        │                 │                       │
        └─────────────────┼───────────────────────┘
                          ↓
            ┌─────────────────────────────┐
            │    Task Execution Layer     │
            ├─────────────────────────────┤
            │  BaseTask (Abstract)        │
            │  ├─ RepositoryAnalysisTask  │
            │  ├─ DocumentationTask       │
            │  └─ CodeQualityTask         │
            └─────────────────────────────┘
                          │
        ┌─────────────────┼──────────────────┐
        ↓                 ↓                   ↓
    ┌───────────┐   ┌──────────────┐   ┌──────────────┐
    │GitHub API │   │File System   │   │Code Analyzer │
    │ - Repos   │   │ - Storage    │   │ - Quality    │
    │ - Data    │   │ - Reports    │   │ - Metrics    │
    └───────────┘   └──────────────┘   └──────────────┘
```

---

## Component Architecture

### 1. **Frontend Layer**
**Purpose**: User interface and API interaction

**Components**:
- `index.html` - UI structure
- `style.css` - Responsive styling
- `script.js` - API client and event handling

**Responsibilities**:
- Display user interface
- Collect user input
- Make API calls
- Display results
- Download reports

**Technology**:
- Single Page Application (SPA)
- Vanilla JavaScript (no frameworks for simplicity)
- CSS3 Grid and Flexbox
- Fetch API for HTTP communication

### 2. **API Layer**
**File**: `backend/app.py`

**Endpoints**:
```
GET  /                                 # Health check
GET  /api/tasks                        # List available tasks
POST /api/tasks/repository-analysis    # Analyze repo
POST /api/tasks/documentation          # Generate docs
POST /api/tasks/code-quality           # Analyze code
POST /api/workflow/execute             # Execute workflow
GET  /api/reports                      # List reports
GET  /api/reports/download/<filename>  # Download report
```

**Responsibilities**:
- Route HTTP requests
- Validate input
- Call appropriate controller methods
- Format and return responses
- Error handling

### 3. **Controller Layer**
**File**: `backend/controllers/task_controller.py`

**Methods**:
- `execute_repository_analysis()` - Route to RepositoryAnalysisTask
- `execute_documentation_task()` - Route to DocumentationTask
- `execute_code_quality_task()` - Route to CodeQualityTask
- `execute_full_workflow()` - Orchestrate multiple tasks

**Responsibilities**:
- Receive API requests
- Prepare input data
- Delegate to WorkflowService
- Generate reports
- Return results

### 4. **Service Layer**

#### **WorkflowService**
**File**: `backend/services/workflow_service.py`

**Methods**:
- `add_task()` - Register task
- `execute_workflow()` - Execute task sequence
- `execute_single_task()` - Execute one task
- `_get_task_by_name()` - Task lookup
- `_format_workflow_result()` - Format output

**Responsibilities**:
- Manage task registry
- Orchestrate task execution
- Pass results between tasks
- Track execution metadata
- Handle task failures

**Design Pattern**: Service Locator Pattern

#### **ReportService**
**File**: `backend/services/report_service.py`

**Methods**:
- `generate_repository_report()` - Generate repo report
- `generate_documentation_report()` - Generate docs report
- `generate_code_quality_report()` - Generate quality report
- `generate_workflow_report()` - Generate workflow report
- `_save_report()` - Save to file

**Responsibilities**:
- Generate formatted reports
- Save reports to disk
- Format report content
- Return file paths

**Design Pattern**: Builder Pattern

### 5. **Task Layer**

#### **BaseTask (Abstract)**
**File**: `backend/tasks/base_task.py`

**Methods**:
- `execute()` - Execute task (abstract)
- `start()` - Mark as started
- `complete()` - Mark as completed
- `fail()` - Mark as failed
- `get_duration()` - Get execution time
- `get_metadata()` - Get task metadata

**Design Pattern**: Abstract Factory Pattern

#### **Concrete Tasks**

**RepositoryAnalysisTask**
- Fetches GitHub repository data
- Extracts statistics and metadata
- Returns structured data

**DocumentationTask**
- Analyzes repository data
- Extracts features
- Generates documentation
- Creates setup instructions

**CodeQualityTask**
- Analyzes uploaded code
- Detects code issues:
  - Long methods
  - Large classes
  - Hardcoded values
  - Poor naming
  - Missing documentation
- Generates quality score
- Creates recommendations

### 6. **Utility Layer**
**File**: `backend/utils/github_client.py`

**Class**: `GitHubClient`

**Methods**:
- `extract_repo_info()` - Parse GitHub URL
- `get_repository()` - Fetch repo data
- `get_repository_readme()` - Fetch README
- `get_repository_languages()` - Fetch languages
- `get_repository_topics()` - Fetch topics

**Responsibilities**:
- Encapsulate GitHub API interaction
- Handle authentication
- Error handling

---

## Data Flow

### Scenario 1: Repository Analysis
```
1. User enters GitHub URL in frontend
2. Frontend calls POST /api/tasks/repository-analysis
3. TaskController.execute_repository_analysis() receives request
4. WorkflowService.execute_single_task() is called
5. RepositoryAnalysisTask.execute() runs:
   - GitHubClient.get_repository() fetches data
   - GitHubClient.get_repository_languages() fetches tech
   - Returns structured result
6. ReportService.generate_repository_report() creates report
7. Response returned to frontend with report path
8. User downloads report
```

### Scenario 2: Full Workflow
```
1. User enters GitHub URL
2. Frontend calls POST /api/workflow/execute
3. TaskController.execute_full_workflow() receives request
4. WorkflowService.execute_workflow() runs sequence:
   a) RepositoryAnalysisTask executes
      - Returns repo data
   b) DocumentationTask executes
      - Receives repo data from previous task
      - Uses it to generate documentation
5. ReportService.generate_workflow_report() creates summary
6. Response includes results from both tasks
7. Frontend displays combined results
```

### Scenario 3: Code Quality Analysis
```
1. User uploads Java file
2. Frontend reads file content
3. Frontend calls POST /api/tasks/code-quality with file
4. TaskController.execute_code_quality_task() receives file
5. CodeQualityTask.execute() analyzes:
   - Checks for long methods
   - Detects large classes
   - Finds hardcoded values
   - Evaluates naming
   - Checks documentation
6. Calculates quality score
7. ReportService.generate_code_quality_report() creates report
8. Response with issues and recommendations
9. User reviews and downloads report
```

---

## SOLID Principles Implementation

### Single Responsibility Principle (SRP)
- **BaseTask** - Only handles task lifecycle
- **RepositoryAnalysisTask** - Only analyzes repositories
- **DocumentationTask** - Only generates documentation
- **CodeQualityTask** - Only analyzes code quality
- **WorkflowService** - Only orchestrates tasks
- **ReportService** - Only generates reports
- **TaskController** - Only routes requests

### Open/Closed Principle (OCP)
- New tasks can be added by extending `BaseTask`
- Existing code doesn't need modification
- Example: To add a new task:
  ```python
  class NewTask(BaseTask):
      def execute(self, **kwargs):
          # Implementation
          pass
  ```

### Liskov Substitution Principle (LSP)
- All tasks are interchangeable via `BaseTask`
- `WorkflowService` doesn't care about concrete task types
- All tasks have same interface:
  ```python
  result = task.execute(**inputs)
  metadata = task.get_metadata()
  ```

### Interface Segregation Principle (ISP)
- Services expose only necessary methods
- `ReportService` only exposes report generation
- `GitHubClient` only exposes GitHub operations
- Clients depend on specific interfaces

### Dependency Inversion Principle (DIP)
- `TaskController` depends on `WorkflowService` (abstraction)
- `WorkflowService` depends on `BaseTask` (abstraction)
- Not depending on concrete implementations
- Easy to swap implementations

---

## Configuration

**File**: `backend/config.py`

```python
GITHUB_API_BASE_URL = "https://api.github.com"
REPORTS_DIR = Path(".../reports")
UPLOAD_FOLDER = Path(".../uploads")
MAX_FILE_SIZE = 5 * 1024 * 1024
```

---

## Error Handling Strategy

### Frontend Error Handling
- Try-catch blocks around Fetch API calls
- User-friendly error messages
- Validation before sending requests

### Backend Error Handling
- Try-except blocks in routes
- Task failure handling via `task.fail()`
- HTTP status codes (200, 400, 404, 500)
- JSON error responses

### Example Error Response
```json
{
  "status": "error",
  "error": "Invalid repository URL",
  "task": "RepositoryAnalysisTask"
}
```

---

## Performance Considerations

### Optimization Strategies
1. **Caching**: Can add Redis for GitHub API responses
2. **Async Processing**: Can use Celery for long-running tasks
3. **Rate Limiting**: Implement to prevent abuse
4. **Pagination**: For large result sets
5. **Compression**: Gzip for report downloads

### Current Performance
- Repository Analysis: 1-3 seconds
- Documentation Generation: 1-2 seconds
- Code Quality Analysis: <1 second
- Full Workflow: 3-5 seconds

---

## Security Considerations

### Current Implementation
- CORS headers for API access control
- Input validation on all endpoints
- File upload validation (type and size)
- URL validation for GitHub repos

### Production Recommendations
1. Add authentication/authorization (JWT)
2. Implement rate limiting (Flask-Limiter)
3. Add request size limits
4. Validate and sanitize all inputs
5. Use HTTPS instead of HTTP
6. Add CSRF protection
7. Implement audit logging

---

## Deployment Architecture

### Local Development
```
Client Browser → Flask Dev Server (localhost:5000)
```

### Production
```
Client Browser → 
  Nginx (Reverse Proxy) → 
  Gunicorn (WSGI Server) → 
  Flask Application → 
  External APIs (GitHub)
```

---

## Extension Points

### Adding a New Task
1. Create new file in `tasks/` folder
2. Inherit from `BaseTask`
3. Implement `execute()` method
4. Add to `WorkflowService` in `__init__`
5. Create API endpoint in `app.py`

### Adding a New API Endpoint
1. Create route in `app.py`
2. Add method in `TaskController`
3. Call appropriate service
4. Return JSON response

### Adding a New Report Type
1. Add method in `ReportService`
2. Create formatting method
3. Call `_save_report()`
4. Integrate into task

---

## Testing Strategy

### Unit Tests (Recommended)
- Test each task independently
- Mock GitHub API responses
- Test service methods
- Test utility functions

### Integration Tests (Recommended)
- Test task orchestration
- Test API endpoints
- Test report generation
- Test full workflows

### Manual Testing
- Use provided test cases in QUICKSTART.md
- Test with various repositories
- Test with different code files
- Test error scenarios

---

## Monitoring & Logging (Future)

### Recommended Additions
1. Structured logging (Python logging module)
2. Request/response logging
3. Error tracking (Sentry)
4. Performance metrics (Prometheus)
5. Health checks
6. Alerts for failures

---

This architecture ensures:
✅ **Maintainability** - Clear separation of concerns  
✅ **Scalability** - Easy to add new tasks and features  
✅ **Testability** - Each component can be tested independently  
✅ **Extensibility** - New functionality without modifying existing code  
✅ **Reliability** - Comprehensive error handling  
✅ **Performance** - Optimized data flow  

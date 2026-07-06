# PROJECT ARCHITECTURE AND DESIGN

Project: `ppt_schedule`

This document explains the full-stack architecture of the `ppt_schedule` project for interview and demo presentation. The project contains:

- `developer-workflow-api` - Spring Boot backend
- `developer-workflow-ui` - Angular frontend plus a plain HTML/JavaScript fallback UI

The system demonstrates a developer workflow dashboard that can run three backend tasks:

- `DOCUMENTATION_GENERATOR`
- `CODE_QUALITY_REPORT`
- `REPOSITORY_ANALYZER`

## 1. High-Level System Overview

The application is a workflow automation demo. A user opens the UI, enters a local project folder path, chooses one of the available workflow tasks, and receives a structured result from the Spring Boot backend.

The backend owns the business logic. The frontend owns interaction, loading state, error display, and result presentation.

### Main Runtime Components

| Layer | Technology | Main Files |
|---|---|---|
| Frontend UI | Angular | `developer-workflow-ui/src/app/features/task-library/task-library.component.ts`, `developer-workflow-ui/src/app/shared/components/task-card/task-card.component.ts`, `developer-workflow-ui/src/app/features/report-viewer/report-viewer.component.ts` |
| Fallback UI | Plain HTML/CSS/JavaScript + Node HTTP server | `developer-workflow-ui/fallback-index.html`, `developer-workflow-ui/fallback-server.js` |
| API Controller | Spring Boot REST | `developer-workflow-api/src/main/java/com/example/workflowhub/controller/TaskController.java` |
| Application Service | Spring service boundary | `developer-workflow-api/src/main/java/com/example/workflowhub/service/TaskExecutionService.java`, `developer-workflow-api/src/main/java/com/example/workflowhub/service/DefaultTaskExecutionService.java` |
| Task Routing | Factory pattern | `developer-workflow-api/src/main/java/com/example/workflowhub/service/TaskServiceFactory.java` |
| Task Business Logic | Individual Spring services | `DocumentationGeneratorService.java`, `CodeQualityReportService.java`, `RepositoryAnalyzerService.java` |
| DTOs | Request/response contracts | `TaskRequest.java`, `TaskResponseDTO.java`, `TaskResultDTO.java` |

## 2. End-to-End System Flow

### Architecture Diagram

```text
Frontend (Angular UI or Fallback UI)
        |
        v
TaskApiService / fallback fetch()
        |
        v
Spring Boot Controller
TaskController
        |
        v
Application Service Layer
TaskExecutionService -> DefaultTaskExecutionService
        |
        v
TaskServiceFactory
        |
        v
Concrete TaskService
DocumentationGeneratorService
CodeQualityReportService
RepositoryAnalyzerService
        |
        v
TaskResult + ReportStorageService
        |
        v
TaskResultDTO response
        |
        v
Frontend Result Panel
ReportViewerComponent / fallback result panel
```

### API Endpoints

The backend exposes two main endpoints from `developer-workflow-api/src/main/java/com/example/workflowhub/controller/TaskController.java`.

| Endpoint | Method | Purpose | Response |
|---|---|---|---|
| `/api/tasks` | `GET` | Returns all available tasks | List of `TaskResponseDTO` |
| `/api/tasks/execute` | `POST` | Executes a selected task for a project path | `TaskResultDTO` |

### Flow 1: User Loads Tasks

1. The Angular application loads `TaskLibraryComponent` from `developer-workflow-ui/src/app/features/task-library/task-library.component.ts`.
2. `TaskLibraryComponent.ngOnInit()` calls its private `loadTasks()` method.
3. `loadTasks()` calls `TaskApiService.getTasks()` from `developer-workflow-ui/src/app/core/services/task-api.service.ts`.
4. `TaskApiService` sends `GET /api/tasks`.
5. `TaskController.getAvailableTasks()` receives the request.
6. The controller delegates to `TaskExecutionService.getAvailableTasks()`.
7. `DefaultTaskExecutionService` asks `TaskServiceFactory.getAllTaskServices()` for all registered task services.
8. Each `TaskService` contributes metadata:
   - task type
   - task name
   - description
9. The backend returns a list of `TaskResponseDTO` objects.
10. Angular stores the tasks in `TaskLibraryComponent.tasks`.
11. `task-library.component.html` renders each task using `TaskCardComponent`.

### Flow 2: User Selects a Task

1. The user sees task cards rendered by `developer-workflow-ui/src/app/shared/components/task-card/task-card.component.html`.
2. Each card displays:
   - `taskName`
   - description
   - `Run Task` button
3. The card emits the selected task through its `runTask` output.
4. `TaskLibraryComponent.runTask(task)` receives the selected task.

This keeps `TaskCardComponent` presentational. It does not know API details, backend routes, or task execution logic.

### Flow 3: User Runs a Task

1. `TaskLibraryComponent.runTask(task)` reads the current project path from the input field.
2. It stores the submitted path in `latestProjectPath` so the result panel shows the path used for that specific run.
3. It calls `TaskApiService.executeTask()`.
4. `TaskApiService.executeTask()` sends this payload to `POST /api/tasks/execute`:

```json
{
  "taskType": "CODE_QUALITY_REPORT",
  "projectPath": "C:\\Users\\rreddy\\Desktop\\ppt_schedule"
}
```

5. `TaskController.executeTask()` receives the request body as `TaskRequest`.
6. The controller delegates to `TaskExecutionService.executeTask(request)`.
7. `DefaultTaskExecutionService` calls `TaskServiceFactory.getTaskService(request.getTaskType())`.
8. `TaskServiceFactory` selects the correct implementation:
   - `DOCUMENTATION_GENERATOR` -> `DocumentationGeneratorService`
   - `CODE_QUALITY_REPORT` -> `CodeQualityReportService`
   - `REPOSITORY_ANALYZER` -> `RepositoryAnalyzerService`
9. The selected service executes business logic and returns a `TaskResult` model.
10. `ReportStorageService` saves the generated report file under the configured reports folder.
11. `DefaultTaskExecutionService` maps the internal result into `TaskResultDTO`.
12. The DTO is returned to Angular.

### Flow 4: User Receives Result

1. Angular receives the `TaskResult` response in `TaskLibraryComponent.showResult()`.
2. `TaskLibraryComponent` passes the result and project path to `ReportViewerComponent`.
3. `developer-workflow-ui/src/app/features/report-viewer/report-viewer.component.html` displays:
   - task name
   - status badge
   - project path
   - metrics when available
   - generated file name
   - timestamp
4. For `CODE_QUALITY_REPORT`, `ReportViewerComponent` parses the existing summary text to display:
   - TODO count
   - FIXME count
   - large files count

No backend API change is required for the Code Quality dashboard metrics. The UI derives those values from the existing `summary` field.

## 3. Fallback UI Flow

The project also includes a fallback frontend for situations where Angular cannot be built or dependencies cannot be installed.

### Files

- `developer-workflow-ui/fallback-index.html`
- `developer-workflow-ui/fallback-server.js`

### Why the Fallback UI Exists

The fallback UI exists because Angular requires `node_modules` and a successful build/dev server setup. In restricted environments, npm registry access can fail with errors such as `EACCES`. The fallback UI avoids that problem by using only:

- plain HTML
- CSS
- vanilla JavaScript
- Node's built-in `http`, `fs`, and `path` modules

It does not require Angular, React, Vite, or installed frontend packages.

### How the Fallback Server Works

`developer-workflow-ui/fallback-server.js` starts a lightweight HTTP server on port `4200`.

It does two jobs:

1. Serves `fallback-index.html` for browser requests.
2. Proxies `/api` requests to the backend at `http://localhost:8080`.

Fallback flow:

```text
Browser -> http://localhost:4200
        -> fallback-server.js
        -> fallback-index.html
        -> fetch('/api/tasks')
        -> fallback-server.js proxy
        -> http://localhost:8080/api/tasks
        -> Spring Boot response
        -> fallback result panel
```

This means the demo can still run even if Angular build support is unavailable.

## 4. GitHub Copilot Role

GitHub Copilot is used as a development assistant, not as a replacement for engineering decisions.

In this project, Copilot can realistically help with:

- generating repetitive Spring Boot DTO boilerplate
- suggesting Angular component bindings
- creating service/controller skeletons
- refactoring repeated UI logic into helper methods
- writing fallback JavaScript fetch and loading-state code
- adding comments that explain end-to-end flow
- improving naming and folder organization

### Examples of Copilot-Assisted Areas

| Area | Files | How Copilot Helps |
|---|---|---|
| Angular API service | `developer-workflow-ui/src/app/core/services/task-api.service.ts` | Helps write typed `HttpClient` calls and shared error handling |
| Angular container component | `developer-workflow-ui/src/app/features/task-library/task-library.component.ts` | Helps organize task loading, run-task flow, loading state, and error state |
| Angular presentational task card | `developer-workflow-ui/src/app/shared/components/task-card/task-card.component.ts` | Helps scaffold input/output bindings and card behavior |
| Angular result panel | `developer-workflow-ui/src/app/features/report-viewer/report-viewer.component.ts` | Helps map existing response data into a structured dashboard layout |
| Spring Boot controller | `developer-workflow-api/src/main/java/com/example/workflowhub/controller/TaskController.java` | Helps generate REST endpoint structure while keeping logic in service layer |
| Spring service layer | `developer-workflow-api/src/main/java/com/example/workflowhub/service/DefaultTaskExecutionService.java` | Helps separate orchestration from task-specific business logic |
| Factory pattern | `developer-workflow-api/src/main/java/com/example/workflowhub/service/TaskServiceFactory.java` | Helps route `TaskType` to the correct service implementation |
| Fallback UI | `developer-workflow-ui/fallback-index.html`, `developer-workflow-ui/fallback-server.js` | Helps generate plain JavaScript fetch logic, proxy server, and no-framework UI |

### Realistic Interview Explanation

A good way to explain Copilot's role:

> I used GitHub Copilot to speed up boilerplate and refactoring, especially DTOs, Angular bindings, and repetitive service/controller structure. The architecture decisions still follow standard Spring Boot and Angular patterns: controller-service separation, DTO boundaries, dependency injection, and component separation.

The project also includes comments such as:

- `// Copilot: controller stays HTTP-only; orchestration is delegated to the service layer.`
- `// Copilot: generated service layer based on task type and kept controller free of business logic.`
- `// Copilot: refactored into factory pattern for extensibility and Open/Closed task routing.`
- `// Copilot: simplified UI binding logic so task execution is orchestrated in one Angular container.`

These comments are useful during a demo because they show how AI assistance supported normal clean-code refactoring.

## 5. SOLID Principles Mapping

### S - Single Responsibility Principle

Single Responsibility means a class or component should have one clear reason to change.

| File | Responsibility |
|---|---|
| `developer-workflow-api/src/main/java/com/example/workflowhub/controller/TaskController.java` | Handles HTTP requests and responses only. It does not execute task business logic. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/DefaultTaskExecutionService.java` | Orchestrates the task execution use case: select service, execute task, save report, return DTO. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/TaskServiceFactory.java` | Selects the correct task service for a `TaskType`. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/ReportStorageService.java` | Saves generated report content to files. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/DocumentationGeneratorService.java` | Generates documentation report content only. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/CodeQualityReportService.java` | Analyzes code quality signals only. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/RepositoryAnalyzerService.java` | Analyzes repository structure only. |
| `developer-workflow-ui/src/app/features/task-library/task-library.component.ts` | Acts as the Angular container for loading tasks, running tasks, and managing UI state. |
| `developer-workflow-ui/src/app/shared/components/task-card/task-card.component.ts` | Displays one task card and emits a selected task event. |
| `developer-workflow-ui/src/app/features/report-viewer/report-viewer.component.ts` | Displays task results and formats metrics for the result panel. |

Interview explanation:

> The controller changes only if HTTP routing changes. A task service changes only if its task behavior changes. The report storage service changes only if report persistence changes.

### O - Open/Closed Principle

Open/Closed means the system should be open for extension but closed for modification.

| File | How OCP Is Applied |
|---|---|
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/TaskService.java` | Defines the common task contract. New tasks implement this interface. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/TaskServiceFactory.java` | Uses Spring-injected `List<TaskService>` to collect task implementations. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/model/TaskType.java` | Defines supported task types. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/DocumentationGeneratorService.java` | One concrete extension of `TaskService`. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/CodeQualityReportService.java` | One concrete extension of `TaskService`. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/RepositoryAnalyzerService.java` | One concrete extension of `TaskService`. |

To add a new task, the expected change is:

1. Add a new `TaskType` value.
2. Create a new class implementing `TaskService`.
3. Let Spring inject it into `TaskServiceFactory`.

The controller does not need a new `if` or `switch` block for each task.

Interview explanation:

> The factory makes the system extensible. I can add a new workflow service without rewriting controller logic or frontend endpoint logic.

### L - Liskov Substitution Principle

Liskov Substitution means objects of a common interface should be replaceable without breaking the caller.

| File | How LSP Is Applied |
|---|---|
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/TaskService.java` | Common interface for all task services. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/DocumentationGeneratorService.java` | Can be used wherever `TaskService` is expected. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/CodeQualityReportService.java` | Can be used wherever `TaskService` is expected. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/RepositoryAnalyzerService.java` | Can be used wherever `TaskService` is expected. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/DefaultTaskExecutionService.java` | Calls `TaskService.execute()` without knowing the concrete service class. |

All task services provide:

- `getTaskType()`
- `getTaskName()`
- `getDescription()`
- `execute(TaskRequest request)`

Because the execution layer only depends on `TaskService`, any concrete task can be substituted as long as it follows that contract.

### I - Interface Segregation Principle

Interface Segregation means interfaces should be small and focused.

| File | How ISP Is Applied |
|---|---|
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/TaskService.java` | Contains only task metadata and execution methods needed by task services. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/TaskExecutionService.java` | Contains only application use-case methods: list tasks and execute task. |
| `developer-workflow-ui/src/app/core/services/task-api.service.ts` | Exposes only frontend API operations needed by components: `getTasks()` and `executeTask()`. |

The interfaces are not bloated. For example, `TaskService` does not include report storage methods, HTTP methods, or UI-specific concepts.

Interview explanation:

> The backend has small contracts. Task services only implement task behavior. The application execution service only exposes use cases needed by the controller.

### D - Dependency Inversion Principle

Dependency Inversion means high-level modules should depend on abstractions instead of concrete low-level classes.

| File | How DIP Is Applied |
|---|---|
| `developer-workflow-api/src/main/java/com/example/workflowhub/controller/TaskController.java` | Depends on `TaskExecutionService`, not on concrete task services. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/DefaultTaskExecutionService.java` | Uses `TaskServiceFactory` and `TaskService` abstraction for task execution. |
| `developer-workflow-api/src/main/java/com/example/workflowhub/service/TaskServiceFactory.java` | Stores task implementations as `TaskService`, not concrete service types. |
| `developer-workflow-ui/src/app/features/task-library/task-library.component.ts` | Depends on `TaskApiService` abstraction for HTTP communication instead of calling `fetch` directly. |
| `developer-workflow-ui/src/app/core/services/task-api.service.ts` | Encapsulates Angular `HttpClient` details away from UI components. |

Interview explanation:

> The controller does not know whether the task is documentation, code quality, or repository analysis. It only knows the use-case service. The frontend component does not know raw HTTP details; it calls `TaskApiService`.

## 6. Clean Code Practices With File References

### Backend Clean Code

| Practice | Files | Explanation |
|---|---|---|
| Controller-service separation | `TaskController.java`, `TaskExecutionService.java`, `DefaultTaskExecutionService.java` | HTTP logic is separate from use-case orchestration. |
| DTO usage | `TaskRequest.java`, `TaskResponseDTO.java`, `TaskResultDTO.java` | API input/output objects are explicit and typed. |
| Meaningful naming | `DocumentationGeneratorService.java`, `CodeQualityReportService.java`, `RepositoryAnalyzerService.java` | Class names explain business purpose. |
| Shared analysis helper | `ProjectStructureAnalyzer.java` | File traversal and technology detection are not duplicated across services. |
| Small focused model records | `ProjectStructure.java`, `AnalyzedProjectFile.java` | Project scan data is passed through typed records. |
| Dedicated persistence helper | `ReportStorageService.java` | Report file writing is isolated from business task services. |
| Centralized error handling | `GlobalExceptionHandler.java` | Exceptions are converted to HTTP responses in one place. |

### Frontend Clean Code

| Practice | Files | Explanation |
|---|---|---|
| Container component | `features/task-library/task-library.component.ts` | Owns task loading, task execution, loading state, error state, and latest result. |
| Presentational component | `shared/components/task-card/task-card.component.ts` | Displays a task and emits user intent. It does not call APIs. |
| Result display component | `features/report-viewer/report-viewer.component.ts` | Owns result formatting and metric extraction. |
| Reusable loading component | `shared/components/loading-indicator/loading-indicator.component.ts` | Keeps loading display separate and reusable. |
| API abstraction | `core/services/task-api.service.ts` | Centralizes backend communication and error conversion. |
| Typed models | `core/models/task.model.ts`, `task-request.model.ts`, `task-result.model.ts` | Keeps frontend request/response data explicit. |

### Naming Conventions

Backend naming:

- Controllers end with `Controller`: `TaskController`
- Services end with `Service`: `CodeQualityReportService`
- DTOs end with `DTO`: `TaskResultDTO`
- Exceptions end with `Exception`: `InvalidProjectFolderException`
- Models describe domain values: `TaskType`, `TaskResult`

Frontend naming:

- Components end with `.component.ts`
- Services end with `.service.ts`
- Models end with `.model.ts`
- Feature components are under `features/`
- Reusable components are under `shared/components/`
- API services and models are under `core/`

## 7. Folder-Level Explanation

### Backend: `developer-workflow-api/`

```text
developer-workflow-api/
  src/main/java/com/example/workflowhub/
    controller/
    dto/
    exception/
    model/
    service/
    task/
  src/main/resources/
  reports/
```

| Folder | Purpose | Important Files |
|---|---|---|
| `controller/` | REST API entry point | `TaskController.java` |
| `dto/` | Request and response contracts | `TaskRequest.java`, `TaskResponseDTO.java`, `TaskResultDTO.java` |
| `exception/` | Custom exceptions and centralized error handling | `GlobalExceptionHandler.java`, `InvalidProjectFolderException.java`, `TaskNotFoundException.java` |
| `model/` | Internal domain models | `TaskType.java`, `TaskResult.java` |
| `service/` | Business services, factory, orchestration, report storage | `TaskService.java`, `TaskServiceFactory.java`, `DefaultTaskExecutionService.java`, `DocumentationGeneratorService.java`, `CodeQualityReportService.java`, `RepositoryAnalyzerService.java`, `ReportStorageService.java` |
| `task/` | Shared project analysis utilities and records | `ProjectStructureAnalyzer.java`, `ProjectStructure.java`, `AnalyzedProjectFile.java` |
| `src/main/resources/` | Spring Boot configuration | `application.properties` |
| `reports/` | Generated task reports | Markdown reports generated by backend tasks |

### Frontend: `developer-workflow-ui/`

```text
developer-workflow-ui/
  src/app/
    core/
      models/
      services/
    features/
      task-library/
      report-viewer/
    shared/
      components/
  fallback-index.html
  fallback-server.js
  proxy.conf.json
```

| Folder/File | Purpose | Important Files |
|---|---|---|
| `src/app/core/models/` | TypeScript interfaces for backend data | `task.model.ts`, `task-request.model.ts`, `task-result.model.ts` |
| `src/app/core/services/` | Angular service abstraction over backend API | `task-api.service.ts` |
| `src/app/features/task-library/` | Main workflow container feature | `task-library.component.ts`, `task-library.component.html` |
| `src/app/features/report-viewer/` | Structured result dashboard UI | `report-viewer.component.ts`, `report-viewer.component.html` |
| `src/app/shared/components/task-card/` | Reusable task card display | `task-card.component.ts`, `task-card.component.html` |
| `src/app/shared/components/loading-indicator/` | Reusable loading indicator | `loading-indicator.component.ts`, `loading-indicator.component.html` |
| `fallback-index.html` | No-build demo UI | Plain HTML/CSS/JavaScript dashboard |
| `fallback-server.js` | No-dependency Node server and API proxy | Serves fallback UI and proxies `/api` to Spring Boot |
| `proxy.conf.json` | Angular dev proxy config | Routes Angular `/api` calls to backend port `8080` |

## 8. Task-by-Task Explanation

### DOCUMENTATION_GENERATOR

| Layer | File | Role |
|---|---|---|
| UI card | `task-card.component.html` | Displays Documentation Generator task. |
| API call | `task-api.service.ts` | Sends selected `taskType` and `projectPath`. |
| Controller | `TaskController.java` | Receives `/api/tasks/execute`. |
| Factory | `TaskServiceFactory.java` | Routes `DOCUMENTATION_GENERATOR` to `DocumentationGeneratorService`. |
| Service | `DocumentationGeneratorService.java` | Scans project metadata and builds documentation content. |
| Shared analyzer | `ProjectStructureAnalyzer.java` | Reads folders, files, important metadata, and technologies. |
| Result | `TaskResultDTO.java` | Returns generated report metadata to frontend. |

### CODE_QUALITY_REPORT

| Layer | File | Role |
|---|---|---|
| UI card | `task-card.component.html` | Displays Code Quality Report task. |
| API call | `task-api.service.ts` | Sends selected `taskType` and `projectPath`. |
| Controller | `TaskController.java` | Receives `/api/tasks/execute`. |
| Factory | `TaskServiceFactory.java` | Routes `CODE_QUALITY_REPORT` to `CodeQualityReportService`. |
| Service | `CodeQualityReportService.java` | Counts TODO/FIXME comments, finds naming issues, and identifies large files. |
| Result UI | `report-viewer.component.ts` | Extracts TODO, FIXME, and large file metrics from the existing summary. |
| Result template | `report-viewer.component.html` | Shows metrics in a dashboard-style card. |

### REPOSITORY_ANALYZER

| Layer | File | Role |
|---|---|---|
| UI card | `task-card.component.html` | Displays Repository Analyzer task. |
| API call | `task-api.service.ts` | Sends selected `taskType` and `projectPath`. |
| Controller | `TaskController.java` | Receives `/api/tasks/execute`. |
| Factory | `TaskServiceFactory.java` | Routes `REPOSITORY_ANALYZER` to `RepositoryAnalyzerService`. |
| Service | `RepositoryAnalyzerService.java` | Builds repository summary, file type counts, technologies, folders, and largest files. |
| Shared analyzer | `ProjectStructureAnalyzer.java` | Performs shared project scan logic. |
| Result UI | `report-viewer.component.html` | Displays structured status, path, report file, and timestamp. |

## 9. Request and Response Examples

### GET `/api/tasks`

Purpose: load available tasks into the UI.

Example response:

```json
[
  {
    "taskType": "DOCUMENTATION_GENERATOR",
    "taskName": "Documentation Generator",
    "description": "Generates project documentation content from workflow inputs."
  },
  {
    "taskType": "CODE_QUALITY_REPORT",
    "taskName": "Code Quality Report",
    "description": "Creates a code quality report with improvement suggestions."
  },
  {
    "taskType": "REPOSITORY_ANALYZER",
    "taskName": "Repository Analyzer",
    "description": "Analyzes repository structure and produces a summary report."
  }
]
```

### POST `/api/tasks/execute`

Purpose: execute one selected task.

Example request:

```json
{
  "taskType": "REPOSITORY_ANALYZER",
  "projectPath": "C:\\Users\\rreddy\\Desktop\\ppt_schedule"
}
```

Example response:

```json
{
  "taskType": "REPOSITORY_ANALYZER",
  "taskName": "Repository Analyzer",
  "status": "SUCCESS",
  "summary": "Repository analysis completed for ppt_schedule with 80 files and 24 folders.",
  "reportFileName": "repository-analysis-2026-07-07-013241.md",
  "generatedAt": "2026-07-07T01:32:41"
}
```

## 10. How to Present This in an Interview

A concise explanation:

> This is a full-stack developer workflow automation project. The Angular frontend loads available tasks from Spring Boot, lets the user enter a project path, runs a selected workflow task, and displays the result in a structured dashboard panel. The backend uses a controller-service-factory design. The controller is HTTP-only, the execution service owns the use case, the factory routes by task type, and each task has its own service class. This demonstrates SOLID principles, especially SRP, OCP, LSP, and DIP.

For Copilot:

> I used GitHub Copilot mainly to speed up scaffolding and refactoring. It helped with DTO boilerplate, Angular component bindings, service/controller skeletons, fallback JavaScript, and explanatory comments. I still made the architecture decisions based on clean code and Spring/Angular best practices.

For fallback UI:

> The fallback UI proves that the backend can still be demonstrated even when Angular dependencies cannot be installed. It uses a small Node server and plain HTML/JavaScript to call the same backend endpoints.

## 11. Key Interview Talking Points

- `TaskController` is thin and follows SRP.
- `TaskServiceFactory` demonstrates the Factory Pattern and OCP.
- All task services implement `TaskService`, demonstrating LSP.
- `TaskExecutionService` and `TaskService` are small focused interfaces, demonstrating ISP.
- The controller depends on `TaskExecutionService`, not concrete services, demonstrating DIP.
- Angular separates container, presentational, API, and result-display responsibilities.
- The fallback UI demonstrates practical engineering under dependency/network constraints.
- DTOs keep API responses typed and predictable.
- The same two endpoints support both Angular and fallback UI.

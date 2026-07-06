# Developer Workflow Architecture

This project demonstrates a Copilot-assisted full-stack workflow using an Angular frontend and Spring Boot backend.

## End-to-End Request Flow

1. Angular UI: the user enters a project path and clicks a task card.
2. `TaskLibraryComponent`: orchestrates UI state, loading, errors, and selected task execution.
3. `TaskApiService`: sends `GET /api/tasks` or `POST /api/tasks/execute`.
4. `TaskController`: handles HTTP only and delegates to the application service.
5. `TaskExecutionService`: defines the use-case boundary for listing and executing tasks.
6. `DefaultTaskExecutionService`: asks `TaskServiceFactory` for the correct task service.
7. `TaskServiceFactory`: routes by `TaskType` without controller branching.
8. Concrete service executes one business task:
   - `DocumentationGeneratorService`
   - `CodeQualityReportService`
   - `RepositoryAnalyzerService`
9. `ReportStorageService`: saves the generated report file.
10. `TaskResultDTO`: returns structured result data to Angular.
11. `ReportViewerComponent`: displays status, project path, metrics when available, generated file, and timestamp.

## SOLID Mapping

- Single Responsibility: `TaskController` handles HTTP, `TaskServiceFactory` routes tasks, each task service owns one workflow, and `ReportStorageService` only stores reports.
- Open/Closed: new task types can be added by implementing `TaskService`; the factory collects implementations through Spring dependency injection.
- Liskov Substitution: all task services implement the same `TaskService` contract and can be used interchangeably by the factory.
- Interface Segregation: `TaskService` contains only task metadata and execution methods needed by this workflow.
- Dependency Inversion: controller depends on `TaskExecutionService`, and the execution layer depends on the `TaskService` abstraction instead of concrete task classes.

## Clean Code Notes

- DTOs are named by purpose: `TaskResponseDTO` for available tasks and `TaskResultDTO` for execution results.
- UI responsibilities are separated: `TaskLibraryComponent` is the container, `TaskCardComponent` is presentational, and `ReportViewerComponent` renders results.
- Shared project scanning lives in `ProjectStructureAnalyzer`, avoiding duplicated file traversal in each task.
- Code uses early returns for empty states and validation branches.

## Copilot-Assisted Development Markers

The code includes demo comments showing AI-assisted refactoring moments:

- `TaskController`: controller kept HTTP-only.
- `DefaultTaskExecutionService`: service layer generated around task type execution.
- `TaskServiceFactory`: factory pattern introduced for extensibility.
- `TaskLibraryComponent`: UI binding logic simplified into a container component.

These markers can be used in an interview to explain how Copilot helped shape the architecture while the final design still follows standard Spring Boot and Angular practices.

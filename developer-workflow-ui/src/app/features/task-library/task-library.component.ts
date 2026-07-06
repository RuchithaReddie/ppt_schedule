import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Task } from '../../core/models/task.model';
import { TaskResult } from '../../core/models/task-result.model';
import { TaskApiService } from '../../core/services/task-api.service';
import { ReportViewerComponent } from '../report-viewer/report-viewer.component';
import { LoadingIndicatorComponent } from '../../shared/components/loading-indicator/loading-indicator.component';
import { TaskCardComponent } from '../../shared/components/task-card/task-card.component';

@Component({
  selector: 'app-task-library',
  standalone: true,
  imports: [FormsModule, LoadingIndicatorComponent, ReportViewerComponent, TaskCardComponent],
  templateUrl: './task-library.component.html'
})
export class TaskLibraryComponent implements OnInit {
  private readonly taskApiService = inject(TaskApiService);

  tasks: Task[] = [];
  projectPath = '';
  latestProjectPath = '';
  latestResult?: TaskResult;
  errorMessage = '';
  isLoading = false;

  ngOnInit(): void {
    this.loadTasks();
  }

  runTask(task: Task): void {
    // Copilot: simplified UI binding logic so task execution is orchestrated in one Angular container.
    // Flow step: user click becomes a TaskApiService request, then ReportViewer receives the result.
    this.beginRequest();
    this.latestProjectPath = this.projectPath.trim();

    this.taskApiService.executeTask({ taskType: task.taskType, projectPath: this.latestProjectPath }).subscribe({
      next: (result) => this.showResult(result),
      error: (error: unknown) => this.showError(error)
    });
  }

  private loadTasks(): void {
    this.beginRequest();

    this.taskApiService.getTasks().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.isLoading = false;
      },
      error: (error: unknown) => this.showError(error)
    });
  }

  private beginRequest(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.latestResult = undefined;
  }

  private showResult(result: TaskResult): void {
    this.latestResult = result;
    this.isLoading = false;
  }

  private showError(error: unknown): void {
    this.errorMessage = this.getMessage(error);
    this.isLoading = false;
  }

  private getMessage(error: unknown): string {
    if (error instanceof Error) {
      return error.message;
    }

    if (typeof error === 'object' && error !== null && 'message' in error && typeof error.message === 'string') {
      return error.message;
    }

    return 'Unable to complete the request. Please try again.';
  }
}
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Task } from '../../core/models/task.model';
import { TaskResult } from '../../core/models/task-result.model';
import { TaskApiService } from '../../core/services/task-api.service';
import { LoadingIndicatorComponent } from '../../shared/components/loading-indicator/loading-indicator.component';
import { ReportViewerComponent } from '../report-viewer/report-viewer.component';
import { TaskCardComponent } from '../../shared/components/task-card/task-card.component';

@Component({
  selector: 'app-task-library',
  standalone: true,
  imports: [FormsModule, LoadingIndicatorComponent, ReportViewerComponent, TaskCardComponent],
  templateUrl: './task-library.component.html',
  styleUrl: './task-library.component.css'
})
export class TaskLibraryComponent implements OnInit {
  tasks: Task[] = [];
  projectPath = '';
  latestResult?: TaskResult;
  errorMessage = '';
  isLoading = false;

  constructor(private readonly taskApiService: TaskApiService) {
  }

  ngOnInit(): void {
    this.loadTasks();
  }

  runTask(task: Task): void {
    this.clearMessages();
    this.isLoading = true;

    this.taskApiService.executeTask({
      taskType: task.taskType,
      projectPath: this.projectPath.trim()
    }).subscribe({
      next: (result) => this.handleTaskSuccess(result),
      error: (error: HttpErrorResponse) => this.handleTaskError(error)
    });
  }

  private loadTasks(): void {
    this.isLoading = true;
    this.taskApiService.getTasks().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => this.handleTaskError(error)
    });
  }

  private handleTaskSuccess(result: TaskResult): void {
    this.latestResult = result;
    this.isLoading = false;
  }

  private handleTaskError(error: HttpErrorResponse): void {
    this.errorMessage = this.extractErrorMessage(error);
    this.isLoading = false;
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.latestResult = undefined;
  }

  private extractErrorMessage(error: HttpErrorResponse): string {
    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }
    if (error.error?.error) {
      return error.error.error;
    }
    return 'Unable to complete the request. Please try again.';
  }
}
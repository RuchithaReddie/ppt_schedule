import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';
import { Task } from '../models/task.model';
import { TaskRequest } from '../models/task-request.model';
import { TaskResult } from '../models/task-result.model';

@Injectable({ providedIn: 'root' })
export class TaskApiService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiBaseUrl = '/api/tasks';

  getTasks(): Observable<Task[]> {
    return this.httpClient.get<Task[]>(this.apiBaseUrl).pipe(catchError((error) => this.handleError(error)));
  }

  executeTask(request: TaskRequest): Observable<TaskResult> {
    // Flow step: Angular sends the selected task type and project path to Spring Boot.
    return this.httpClient.post<TaskResult>(`${this.apiBaseUrl}/execute`, request).pipe(catchError((error) => this.handleError(error)));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    const message = typeof error.error === 'string'
      ? error.error
      : error.error?.error || 'Unable to complete the request. Please try again.';

    return throwError(() => new Error(message));
  }
}
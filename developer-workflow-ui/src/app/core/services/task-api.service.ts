import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Task } from '../models/task.model';
import { TaskRequest } from '../models/task-request.model';
import { TaskResult } from '../models/task-result.model';

@Injectable({
  providedIn: 'root'
})
export class TaskApiService {
  private readonly apiUrl = '/api/tasks';

  constructor(private readonly httpClient: HttpClient) {
  }

  getTasks(): Observable<Task[]> {
    return this.httpClient.get<Task[]>(this.apiUrl);
  }

  executeTask(request: TaskRequest): Observable<TaskResult> {
    return this.httpClient.post<TaskResult>(`${this.apiUrl}/execute`, request);
  }
}
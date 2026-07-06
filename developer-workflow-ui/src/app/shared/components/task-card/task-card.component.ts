import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Task } from '../../../core/models/task.model';

@Component({
  selector: 'app-task-card',
  standalone: true,
  templateUrl: './task-card.component.html',
  styleUrl: './task-card.component.css'
})
export class TaskCardComponent {
  @Input() task?: Task;
  @Input() disabled = false;
  @Output() runTask = new EventEmitter<Task>();

  onRunTask(): void {
    if (this.task && !this.disabled) {
      this.runTask.emit(this.task);
    }
  }
}
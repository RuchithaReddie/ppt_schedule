import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Task } from '../../../core/models/task.model';

@Component({
  selector: 'app-task-card',
  standalone: true,
  templateUrl: './task-card.component.html'
})
export class TaskCardComponent {
  @Input({ required: true }) task!: Task;
  @Input() disabled = false;
  @Output() runTask = new EventEmitter<Task>();

  readonly descriptions: Record<string, string> = {
    REPOSITORY_ANALYZER: 'Analyze a local project and generate a repository report.',
    DOCUMENTATION_GENERATOR: 'Generate project documentation from the selected repository.',
    CODE_QUALITY_REPORT: 'Generate a simple offline code quality report.'
  };

  get description(): string {
    return this.descriptions[this.task.taskType] ?? this.task.description;
  }
}
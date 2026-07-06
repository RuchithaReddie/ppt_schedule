import { DatePipe } from '@angular/common';
import { Component, Input } from '@angular/core';
import { TaskResult } from '../../core/models/task-result.model';

@Component({
  selector: 'app-report-viewer',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './report-viewer.component.html',
  styleUrl: './report-viewer.component.css'
})
export class ReportViewerComponent {
  @Input() result?: TaskResult;
}
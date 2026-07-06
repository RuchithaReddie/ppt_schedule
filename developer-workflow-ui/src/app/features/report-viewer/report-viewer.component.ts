import { Component, Input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TaskResult } from '../../core/models/task-result.model';

@Component({
  selector: 'app-report-viewer',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './report-viewer.component.html'
})
export class ReportViewerComponent {
  @Input() result?: TaskResult;
  @Input() projectPath = '';

  get isCodeQualityReport(): boolean {
    return this.result?.taskType === 'CODE_QUALITY_REPORT';
  }

  get statusClass(): string {
    return this.result?.status?.toLowerCase() === 'success' ? 'success' : 'failure';
  }

  get todoCount(): number {
    // Flow step: metrics are derived from the existing backend summary, so the API contract stays unchanged.
    return this.extractCount(/(\d+)\s+TODO comments?/i);
  }

  get fixmeCount(): number {
    return this.extractCount(/(\d+)\s+FIXME comments?/i);
  }

  get largeFilesCount(): number {
    return this.extractCount(/(\d+)\s+large files?/i);
  }

  private extractCount(pattern: RegExp): number {
    const match = this.result?.summary?.match(pattern);
    return match ? Number(match[1]) : 0;
  }
}
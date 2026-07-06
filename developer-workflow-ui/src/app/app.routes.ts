import { Routes } from '@angular/router';
import { TaskLibraryComponent } from './features/task-library/task-library.component';
import { ReportViewerComponent } from './features/report-viewer/report-viewer.component';

export const routes: Routes = [
  {
    path: '',
    component: TaskLibraryComponent
  },
  {
    path: 'reports',
    component: ReportViewerComponent
  }
];
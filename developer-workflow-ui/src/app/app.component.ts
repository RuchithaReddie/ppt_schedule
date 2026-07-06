import { Component } from '@angular/core';
import { TaskLibraryComponent } from './features/task-library/task-library.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [TaskLibraryComponent],
  templateUrl: './app.component.html'
})
export class AppComponent {
}
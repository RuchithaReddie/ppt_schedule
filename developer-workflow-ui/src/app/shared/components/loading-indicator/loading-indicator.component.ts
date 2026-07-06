import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading-indicator',
  standalone: true,
  templateUrl: './loading-indicator.component.html'
})
export class LoadingIndicatorComponent {
  @Input() isLoading = false;
}
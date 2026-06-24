import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading',
  template: `
    <div class="loading-container" *ngIf="isLoading">
      <mat-progress-spinner 
        [mode]="mode" 
        [diameter]="diameter"
        [color]="color">
      </mat-progress-spinner>
      <p *ngIf="message" class="loading-message">{{ message }}</p>
    </div>
  `,
  styles: [`
    .loading-container {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      padding: 32px;
    }
    
    .loading-message {
      margin-top: 16px;
      color: #666;
      font-size: 14px;
    }
  `]
})
export class LoadingComponent {
  @Input() isLoading = true;
  @Input() mode: 'determinate' | 'indeterminate' = 'indeterminate';
  @Input() diameter = 50;
  @Input() color: 'primary' | 'accent' | 'warn' = 'primary';
  @Input() message = 'Loading...';
}

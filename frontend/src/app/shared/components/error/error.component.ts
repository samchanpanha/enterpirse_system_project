import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-error',
  template: `
    <div class="error-state" *ngIf="error">
      <mat-icon color="warn" style="font-size: 48px; height: 48px; width: 48px;">error_outline</mat-icon>
      <h3>{{ title }}</h3>
      <p>{{ error }}</p>
      <button mat-raised-button color="primary" (click)="onRetry.emit()" *ngIf="showRetry">
        Retry
      </button>
    </div>
  `,
  styles: [`
    .error-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px 24px;
      text-align: center;
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }
    
    h3 {
      margin: 16px 0 8px;
      color: #333;
    }
    
    p {
      color: #666;
      max-width: 400px;
      line-height: 1.5;
    }
  `]
})
export class ErrorComponent {
  @Input() error: string | null = null;
  @Input() title = 'Error';
  @Input() showRetry = true;
}

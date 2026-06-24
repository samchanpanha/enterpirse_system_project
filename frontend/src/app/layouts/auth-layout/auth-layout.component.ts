import { Component } from '@angular/core';

@Component({
  selector: 'app-auth-layout',
  template: `
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <mat-icon>assessment</mat-icon>
          <h1>Report System</h1>
          <p>Enterprise Reporting Dashboard</p>
        </div>
        
        <div class="auth-content">
          <router-outlet></router-outlet>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    
    .auth-card {
      background: white;
      border-radius: 8px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
      overflow: hidden;
      width: 100%;
      max-width: 450px;
    }
    
    .auth-header {
      background: linear-gradient(135deg, #3f51b5 0%, #5c6bc0 100%);
      color: white;
      padding: 32px 24px;
      text-align: center;
    }
    
    .auth-header mat-icon {
      font-size: 48px;
      height: 48px;
      width: 48px;
      margin-bottom: 16px;
    }
    
    .auth-header h1 {
      margin: 0 0 8px;
      font-size: 28px;
      font-weight: 500;
    }
    
    .auth-header p {
      margin: 0;
      opacity: 0.9;
      font-size: 14px;
    }
    
    .auth-content {
      padding: 32px 24px;
    }
  `]
})
export class AuthLayoutComponent { }

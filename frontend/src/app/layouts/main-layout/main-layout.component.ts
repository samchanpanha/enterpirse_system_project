import { Component } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-main-layout',
  template: `
    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav #sidenav mode="side" opened class="sidenav">
        <div class="sidenav-header">
          <mat-icon>assessment</mat-icon>
          <span>Report System</span>
        </div>
        
        <mat-nav-list>
          <a mat-list-item routerLink="/dashboard" routerLinkActive="active-list-item">
            <mat-icon matListItemIcon>dashboard</mat-icon>
            <span matListItemTitle>Dashboard</span>
          </a>
          
          <a mat-list-item routerLink="/reports" routerLinkActive="active-list-item">
            <mat-icon matListItemIcon>description</mat-icon>
            <span matListItemTitle>Reports</span>
          </a>
          
          <mat-divider></mat-divider>
          
          <a mat-list-item routerLink="/reports/templates" routerLinkActive="active-list-item">
            <mat-icon matListItemIcon>folder</mat-icon>
            <span matListItemTitle>Templates</span>
          </a>
          
          <a mat-list-item routerLink="/reports/schedules" routerLinkActive="active-list-item">
            <mat-icon matListItemIcon>schedule</mat-icon>
            <span matListItemTitle>Schedules</span>
          </a>
        </mat-nav-list>
        
        <div class="sidenav-footer">
          <button mat-button (click)="logout()">
            <mat-icon>logout</mat-icon>
            Logout
          </button>
        </div>
      </mat-sidenav>
      
      <mat-sidenav-content>
        <mat-toolbar color="primary" class="toolbar">
          <button mat-icon-button (click)="sidenav.toggle()">
            <mat-icon>menu</mat-icon>
          </button>
          <span class="toolbar-spacer"></span>
          
          <button mat-icon-button [matMenuTriggerFor]="userMenu">
            <mat-icon>account_circle</mat-icon>
          </button>
          
          <mat-menu #userMenu="matMenu">
            <div matMenuItem class="user-info">
              <strong>{{ username }}</strong>
            </div>
            <mat-divider></mat-divider>
            <button mat-menu-item (click)="logout()">
              <mat-icon>logout</mat-icon>
              Logout
            </button>
          </mat-menu>
        </mat-toolbar>
        
        <main class="content">
          <router-outlet></router-outlet>
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .sidenav-container {
      height: 100vh;
    }
    
    .sidenav {
      width: 250px;
      display: flex;
      flex-direction: column;
    }
    
    .sidenav-header {
      display: flex;
      align-items: center;
      padding: 16px;
      font-size: 18px;
      font-weight: 500;
      color: #3f51b5;
      border-bottom: 1px solid #e0e0e0;
    }
    
    .sidenav-header mat-icon {
      margin-right: 12px;
    }
    
    .active-list-item {
      background-color: rgba(63, 81, 181, 0.1);
      color: #3f51b5;
    }
    
    .sidenav-footer {
      margin-top: auto;
      padding: 16px;
      border-top: 1px solid #e0e0e0;
    }
    
    .toolbar {
      position: sticky;
      top: 0;
      z-index: 100;
    }
    
    .toolbar-spacer {
      flex: 1 1 auto;
    }
    
    .user-info {
      padding: 8px 16px;
    }
    
    .content {
      padding: 24px;
      background-color: #f5f5f5;
      min-height: calc(100vh - 64px);
    }
  `]
})
export class MainLayoutComponent {
  constructor(private keycloakService: KeycloakService) {}

  get username(): string {
    return this.keycloakService.getUsername() || 'User';
  }

  logout(): void {
    this.keycloakService.logout(window.location.origin);
  }
}

import { Component, signal, effect } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AvatarModule } from 'primeng/avatar';
import { AuthService } from '../../core/services/auth.service';
import { NavService, NavItem } from '../../core/services/nav.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, AvatarModule],
  template: `
    <aside
      class="sidebar"
      [class.collapsed]="nav.collapsed()"
    >
      <div class="sidebar-header">
        @if (!nav.collapsed()) {
          <i class="pi pi-chart-bar text-2xl text-indigo-400"></i>
          <span class="text-lg font-bold tracking-wide">Report System</span>
        } @else {
          <i class="pi pi-chart-bar text-2xl text-indigo-400"></i>
        }
      </div>

      <div class="sidebar-user">
        <p-avatar
          [label]="(auth.user()?.name || 'U')[0]"
          styleClass="bg-indigo-500 text-white"
          shape="circle"
          size="normal"
        />
        @if (!nav.collapsed()) {
          <div class="user-info">
            <span class="user-name">{{ auth.user()?.name || 'User' }}</span>
            <span class="user-role">Administrator</span>
          </div>
        }
      </div>

      <nav class="sidebar-nav">
        @for (item of nav.navItems; track item.route) {
          @if (item.children?.length) {
            <div class="nav-group">
              <button
                class="nav-item"
                [class.active]="isGroupActive(item)"
                (click)="toggleGroup(item.route)"
              >
                <i [class]="item.icon" class="nav-icon"></i>
                @if (!nav.collapsed()) {
                  <span class="nav-label">{{ item.label }}</span>
                  <i
                    class="pi pi-chevron-down nav-arrow"
                    [class.rotated]="expandedGroups()[item.route]"
                  ></i>
                  @if (item.badge) {
                    <span class="nav-badge" [class]="item.badgeColor || 'bg-indigo-500'">{{ item.badge }}</span>
                  }
                }
              </button>
              @if (expandedGroups()[item.route] && !nav.collapsed()) {
                <div class="nav-children">
                  @for (child of item.children; track child.route) {
                    <a
                      [routerLink]="child.route"
                      routerLinkActive="active"
                      class="nav-child-item"
                    >
                      <span class="nav-dot"></span>
                      <span class="nav-label">{{ child.label }}</span>
                    </a>
                  }
                </div>
              }
            </div>
          } @else {
            <a
              [routerLink]="item.route"
              routerLinkActive="active"
              class="nav-item"
              [routerLinkActiveOptions]="{ exact: item.route === '/dashboard' }"
            >
              <i [class]="item.icon" class="nav-icon"></i>
              @if (!nav.collapsed()) {
                <span class="nav-label">{{ item.label }}</span>
                @if (item.badge) {
                  <span class="nav-badge" [class]="item.badgeColor || 'bg-indigo-500'">{{ item.badge }}</span>
                }
              }
            </a>
          }
        }
      </nav>

      <div class="sidebar-footer">
        @if (!nav.collapsed()) {
          <button class="footer-btn" (click)="auth.logout()">
            <i class="pi pi-sign-out"></i>
            <span>Sign Out</span>
          </button>
        } @else {
          <button class="footer-btn" (click)="auth.logout()" title="Sign Out">
            <i class="pi pi-sign-out"></i>
          </button>
        }
      </div>
    </aside>
  `,
  styles: [`
    :host { display: contents; }
    .sidebar {
      display: flex;
      flex-direction: column;
      width: 260px;
      min-width: 260px;
      height: 100vh;
      background: #1e293b;
      color: #cbd5e1;
      transition: width 0.25s ease, min-width 0.25s ease;
      overflow: hidden;
      z-index: 100;
    }
    .sidebar.collapsed {
      width: 64px;
      min-width: 64px;
    }
    .sidebar-header {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 20px 20px;
      border-bottom: 1px solid #334155;
      white-space: nowrap;
    }
    .sidebar.collapsed .sidebar-header {
      justify-content: center;
      padding: 20px 0;
    }
    .sidebar-user {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px 20px;
      border-bottom: 1px solid #334155;
      white-space: nowrap;
    }
    .sidebar.collapsed .sidebar-user {
      justify-content: center;
      padding: 16px 0;
    }
    .user-info {
      display: flex;
      flex-direction: column;
      gap: 2px;
      overflow: hidden;
    }
    .user-name {
      font-size: 0.875rem;
      font-weight: 600;
      color: #f1f5f9;
    }
    .user-role {
      font-size: 0.75rem;
      color: #94a3b8;
    }
    .sidebar-nav {
      flex: 1;
      overflow-y: auto;
      padding: 8px 0;
    }
    .sidebar-nav::-webkit-scrollbar {
      width: 3px;
    }
    .sidebar-nav::-webkit-scrollbar-thumb {
      background: #475569;
      border-radius: 4px;
    }
    .nav-group {
      display: flex;
      flex-direction: column;
    }
    .nav-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 20px;
      color: #94a3b8;
      text-decoration: none;
      cursor: pointer;
      border: none;
      background: none;
      width: 100%;
      text-align: left;
      font-size: 0.875rem;
      font-family: inherit;
      transition: all 0.15s ease;
      white-space: nowrap;
      position: relative;
      border-left: 3px solid transparent;
    }
    .sidebar.collapsed .nav-item {
      justify-content: center;
      padding: 10px 0;
    }
    .nav-item:hover {
      color: #e2e8f0;
      background: #334155;
    }
    .nav-item.active {
      color: #818cf8;
      background: #1e293b;
      border-left-color: #818cf8;
    }
    .nav-icon {
      font-size: 1.125rem;
      width: 1.5rem;
      text-align: center;
      flex-shrink: 0;
    }
    .sidebar.collapsed .nav-icon {
      width: auto;
    }
    .nav-label {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .nav-arrow {
      font-size: 0.75rem;
      transition: transform 0.2s ease;
      flex-shrink: 0;
    }
    .nav-arrow.rotated {
      transform: rotate(180deg);
    }
    .nav-badge {
      font-size: 0.625rem;
      padding: 1px 6px;
      border-radius: 999px;
      color: white;
      font-weight: 600;
      flex-shrink: 0;
    }
    .nav-children {
      display: flex;
      flex-direction: column;
    }
    .nav-child-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 20px 8px 52px;
      color: #94a3b8;
      text-decoration: none;
      font-size: 0.8125rem;
      transition: all 0.15s ease;
      white-space: nowrap;
    }
    .nav-child-item:hover {
      color: #e2e8f0;
      background: #334155;
    }
    .nav-child-item.active {
      color: #818cf8;
    }
    .nav-dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: currentColor;
      flex-shrink: 0;
    }
    .sidebar-footer {
      border-top: 1px solid #334155;
      padding: 8px 0;
    }
    .footer-btn {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 20px;
      color: #94a3b8;
      cursor: pointer;
      border: none;
      background: none;
      width: 100%;
      text-align: left;
      font-size: 0.875rem;
      font-family: inherit;
      transition: all 0.15s ease;
      white-space: nowrap;
    }
    .sidebar.collapsed .footer-btn {
      justify-content: center;
      padding: 10px 0;
    }
    .footer-btn:hover {
      color: #ef4444;
      background: #334155;
    }
  `],
})
export class SidebarComponent {
  protected readonly nav: NavService;
  protected readonly auth: AuthService;
  readonly expandedGroups = signal<Record<string, boolean>>({});

  constructor(nav: NavService, auth: AuthService) {
    this.nav = nav;
    this.auth = auth;
  }

  isGroupActive(item: NavItem): boolean {
    return item.children?.some((c) =>
      typeof window !== 'undefined' && window.location.pathname.startsWith(c.route)
    ) ?? false;
  }

  toggleGroup(route: string) {
    this.expandedGroups.update((g) => ({ ...g, [route]: !g[route] }));
  }
}

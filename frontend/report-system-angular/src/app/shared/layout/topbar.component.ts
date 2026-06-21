import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AvatarModule } from 'primeng/avatar';
import { RippleModule } from 'primeng/ripple';
import { AuthService } from '../../core/services/auth.service';
import { NavService } from '../../core/services/nav.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [RouterLink, AvatarModule, RippleModule],
  template: `
    <header class="topbar">
      <div class="topbar-left">
        <button
          class="menu-btn"
          (click)="nav.toggleCollapsed()"
          pRipple
          title="Toggle sidebar"
        >
          <i class="pi pi-bars"></i>
        </button>
        <nav class="breadcrumbs">
          @for (crumb of breadcrumbs; track crumb.path; let last = $last) {
            @if (!last) {
              <a class="crumb-link" [routerLink]="crumb.path">{{ crumb.label }}</a>
              <i class="pi pi-chevron-right crumb-sep"></i>
            } @else {
              <span class="crumb-current">{{ crumb.label }}</span>
            }
          }
        </nav>
      </div>

      <div class="topbar-right">
        <button
          class="icon-btn"
          (click)="toggleDarkMode()"
          pRipple
          title="Toggle theme"
        >
          <i [class]="isDark ? 'pi pi-sun' : 'pi pi-moon'"></i>
        </button>
        <div class="user-section">
          <span class="user-label">{{ auth.user()?.name || 'User' }}</span>
          <p-avatar
            [label]="(auth.user()?.name || 'U')[0]"
            styleClass="bg-indigo-500 text-white"
            shape="circle"
            size="normal"
          />
          <button
            class="icon-btn logout-btn"
            (click)="auth.logout()"
            pRipple
            title="Sign out"
          >
            <i class="pi pi-sign-out"></i>
          </button>
        </div>
      </div>
    </header>
  `,
  styles: [`
    :host { display: contents; }
    .topbar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 60px;
      padding: 0 20px;
      background: #ffffff;
      border-bottom: 1px solid #e2e8f0;
      flex-shrink: 0;
    }
    :host-context(.dark) .topbar {
      background: #0f172a;
      border-bottom-color: #1e293b;
    }
    .topbar-left {
      display: flex;
      align-items: center;
      gap: 16px;
      flex: 1;
      min-width: 0;
    }
    .topbar-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .menu-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      border-radius: 8px;
      border: none;
      background: #f1f5f9;
      color: #475569;
      cursor: pointer;
      transition: all 0.15s ease;
      flex-shrink: 0;
    }
    :host-context(.dark) .menu-btn {
      background: #1e293b;
      color: #94a3b8;
    }
    .menu-btn:hover {
      background: #e2e8f0;
    }
    :host-context(.dark) .menu-btn:hover {
      background: #334155;
    }
    .breadcrumbs {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.8125rem;
      min-width: 0;
      overflow: hidden;
    }
    .crumb-link {
      color: #64748b;
      text-decoration: none;
      white-space: nowrap;
    }
    .crumb-link:hover {
      color: #6366f1;
      text-decoration: underline;
    }
    :host-context(.dark) .crumb-link {
      color: #94a3b8;
    }
    :host-context(.dark) .crumb-link:hover {
      color: #818cf8;
    }
    .crumb-sep {
      font-size: 0.625rem;
      color: #94a3b8;
      flex-shrink: 0;
    }
    .crumb-current {
      color: #1e293b;
      font-weight: 600;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    :host-context(.dark) .crumb-current {
      color: #e2e8f0;
    }
    .icon-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      border-radius: 8px;
      border: none;
      background: #f1f5f9;
      color: #475569;
      cursor: pointer;
      transition: all 0.15s ease;
    }
    :host-context(.dark) .icon-btn {
      background: #1e293b;
      color: #94a3b8;
    }
    .icon-btn:hover {
      background: #e2e8f0;
    }
    :host-context(.dark) .icon-btn:hover {
      background: #334155;
    }
    .user-section {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-left: 4px;
    }
    .user-label {
      font-size: 0.875rem;
      font-weight: 500;
      color: #334155;
    }
    :host-context(.dark) .user-label {
      color: #e2e8f0;
    }
    .logout-btn {
      background: transparent;
      color: #94a3b8;
    }
    .logout-btn:hover {
      color: #ef4444;
      background: #fef2f2;
    }
    :host-context(.dark) .logout-btn:hover {
      background: #1e293b;
    }
  `],
})
export class TopbarComponent {
  isDark = document.documentElement.classList.contains('dark');

  constructor(
    protected auth: AuthService,
    protected nav: NavService,
    private router: Router,
  ) {}

  get breadcrumbs(): { label: string; path: string }[] {
    const crumbs: { label: string; path: string }[] = [];
    const segments = this.router.url.split('?')[0].split('/').filter(Boolean);
    let path = '';

    for (const seg of segments) {
      path += '/' + seg;
      const label = seg.replace(/-/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());
      crumbs.push({ label, path });
    }

    if (crumbs.length === 0) {
      crumbs.push({ label: 'Dashboard', path: '/dashboard' });
    }

    return crumbs;
  }

  toggleDarkMode() {
    this.isDark = !this.isDark;
    document.documentElement.classList.toggle('dark', this.isDark);
  }
}

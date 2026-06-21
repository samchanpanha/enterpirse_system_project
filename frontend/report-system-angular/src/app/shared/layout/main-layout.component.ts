import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './sidebar.component';
import { TopbarComponent } from './topbar.component';
import { NavService } from '../../core/services/nav.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, TopbarComponent],
  template: `
    <div class="layout-wrapper">
      <app-sidebar />
      <div class="layout-main">
        <app-topbar />
        <main class="layout-content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styles: [`
    :host { display: contents; }
    .layout-wrapper {
      display: flex;
      height: 100vh;
      overflow: hidden;
    }
    .layout-main {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-width: 0;
      background: #f8fafc;
      overflow: hidden;
    }
    :host-context(.dark) .layout-main {
      background: #0f172a;
    }
    .layout-content {
      flex: 1;
      overflow-y: auto;
      padding: 24px;
    }
    @media (max-width: 768px) {
      .layout-content {
        padding: 16px;
      }
    }
  `],
})
export class MainLayoutComponent {
  constructor(protected nav: NavService) {}
}

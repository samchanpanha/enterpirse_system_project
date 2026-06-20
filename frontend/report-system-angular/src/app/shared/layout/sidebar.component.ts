import { Component, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <aside
      class="flex flex-column h-full bg-gray-900 text-white"
      style="width: 260px;"
    >
      <div class="flex align-items-center gap-2 px-4 py-4 border-bottom-1 border-gray-700">
        <i class="pi pi-chart-bar text-2xl text-indigo-400"></i>
        <span class="text-lg font-bold">Report System</span>
      </div>

      <nav class="flex-1 overflow-y-auto py-2">
        @for (item of navItems; track item.route) {
          <a
            [routerLink]="item.route"
            routerLinkActive="bg-gray-700 text-white border-left-3 border-indigo-400"
            class="flex align-items-center gap-3 px-4 py-3 text-gray-300 hover:bg-gray-800 hover:text-white cursor-pointer no-underline transition-colors"
            [routerLinkActiveOptions]="{ exact: item.route === '/dashboard' }"
          >
            <i [class]="item.icon" class="text-lg" style="width: 1.5rem;"></i>
            <span class="text-sm font-medium">{{ item.label }}</span>
          </a>
        }
      </nav>

      <div class="border-top-1 border-gray-700 p-3">
        <div class="flex align-items-center gap-2 text-sm text-gray-400">
          <i class="pi pi-circle-on text-green-400 text-xs"></i>
          {{ auth.user()?.name || 'User' }}
        </div>
      </div>
    </aside>
  `,
})
export class SidebarComponent {
  protected readonly auth: AuthService;

  navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'pi pi-home', route: '/dashboard' },
    { label: 'Properties', icon: 'pi pi-building', route: '/properties' },
    { label: 'Restaurant', icon: 'pi pi-shop', route: '/restaurant' },
    { label: 'Inventory', icon: 'pi pi-box', route: '/inventory' },
    { label: 'Finance', icon: 'pi pi-wallet', route: '/finance' },
    { label: 'Payments', icon: 'pi pi-credit-card', route: '/payment' },
    { label: 'Reports', icon: 'pi pi-chart-line', route: '/reports' },
  ];

  constructor(auth: AuthService) {
    this.auth = auth;
  }
}

import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [AvatarModule, MenuModule],
  template: `
    <header
      class="flex align-items-center justify-content-between px-4 py-2 bg-white border-bottom-1 border-gray-200"
      style="height: 60px;"
    >
      <div class="flex align-items-center gap-3">
        <button
          (click)="toggleDarkMode()"
          class="p-2 border-none border-circle bg-gray-100 hover:bg-gray-200 cursor-pointer transition-colors"
          pRipple
        >
          <i [class]="isDark ? 'pi pi-sun' : 'pi pi-moon'" class="text-sm"></i>
        </button>
      </div>

      <div class="flex align-items-center gap-2">
        <span class="text-sm text-gray-600 font-medium">
          {{ auth.user()?.name || 'User' }}
        </span>
        <p-avatar
          [label]="(auth.user()?.name || 'U')[0]"
          styleClass="bg-indigo-500 text-white"
          shape="circle"
          size="normal"
        />
        <button
          (click)="logout()"
          class="ml-2 p-2 border-none bg-transparent text-gray-500 hover:text-red-500 cursor-pointer transition-colors"
          title="Sign out"
        >
          <i class="pi pi-sign-out text-lg"></i>
        </button>
      </div>
    </header>
  `,
})
export class TopbarComponent {
  isDark = document.documentElement.classList.contains('dark');

  constructor(
    protected auth: AuthService,
    private router: Router,
  ) {}

  toggleDarkMode() {
    this.isDark = !this.isDark;
    document.documentElement.classList.toggle('dark', this.isDark);
  }

  logout() {
    this.auth.logout();
  }
}

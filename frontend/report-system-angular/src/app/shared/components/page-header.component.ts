import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

export interface IBreadcrumb {
  label: string;
  routerLink?: string;
}

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="mb-4">
      @if (breadcrumbs?.length) {
        <nav class="flex align-items-center gap-1 text-sm text-gray-500 mb-1">
          @for (crumb of breadcrumbs; track crumb.label; let last = $last) {
            @if (!last && crumb.routerLink) {
              <a
                [routerLink]="crumb.routerLink"
                class="text-gray-500 no-underline hover:text-indigo-500 transition-colors"
              >
                {{ crumb.label }}
              </a>
              <i class="pi pi-chevron-right text-xs text-gray-400"></i>
            } @else {
              <span class="text-gray-800 font-medium">{{ crumb.label }}</span>
            }
          }
        </nav>
      }
      <div class="flex align-items-center gap-3">
        <h1 class="text-2xl font-bold text-gray-900 m-0">{{ title }}</h1>
        @if (subtitle) {
          <span class="text-sm text-gray-500 mt-1">{{ subtitle }}</span>
        }
      </div>
    </div>
  `,
})
export class PageHeaderComponent {
  @Input() title = '';
  @Input() subtitle = '';
  @Input() breadcrumbs: IBreadcrumb[] = [];
}

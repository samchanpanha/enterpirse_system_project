import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [ButtonModule],
  template: `
    <div class="flex flex-column align-items-center justify-content-center py-6 px-4">
      <i [class]="icon" class="text-5xl text-gray-300 mb-3"></i>
      @if (title) {
        <h3 class="text-lg font-semibold text-gray-600 m-0 mb-2">{{ title }}</h3>
      }
      @if (description) {
        <p class="text-sm text-gray-400 m-0 mb-4 text-center max-w-24rem">{{ description }}</p>
      }
      @if (actionLabel) {
        <p-button
          [label]="actionLabel"
          icon="pi pi-plus"
          severity="primary"
          (onClick)="action.emit()"
        />
      }
    </div>
  `,
})
export class EmptyStateComponent {
  @Input() icon = 'pi pi-inbox';
  @Input() title = '';
  @Input() description = '';
  @Input() actionLabel = '';
  @Output() action = new EventEmitter<void>();
}

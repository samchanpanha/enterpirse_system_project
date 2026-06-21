import { Component, Input, Output, EventEmitter } from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [DialogModule, ButtonModule],
  template: `
    <p-dialog
      [(visible)]="visible"
      [header]="title"
      [modal]="true"
      [closable]="true"
      [draggable]="false"
      [resizable]="false"
      styleClass="w-24rem"
      (onHide)="cancel.emit()"
    >
      <p class="m-0 text-sm text-gray-600">{{ message }}</p>
      <ng-template pTemplate="footer">
        <div class="flex gap-2 justify-content-end">
          <p-button
            label="Cancel"
            severity="secondary"
            [text]="true"
            (onClick)="cancel.emit()"
          />
          <p-button
            [label]="confirmText"
            [severity]="destructive ? 'danger' : 'primary'"
            (onClick)="confirm.emit()"
          />
        </div>
      </ng-template>
    </p-dialog>
  `,
})
export class ConfirmDialogComponent {
  @Input() visible = false;
  @Input() title = '';
  @Input() message = '';
  @Input() confirmText = 'Confirm';
  @Input() destructive = false;
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
}

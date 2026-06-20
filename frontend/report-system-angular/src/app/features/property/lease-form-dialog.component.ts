import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { DatePickerModule } from 'primeng/datepicker';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { PropertyApiService } from './services/property-api.service';
import { LeaseRequest } from './models/lease.model';

@Component({
  selector: 'app-lease-form-dialog',
  standalone: true,
  imports: [
    FormsModule,
    DialogModule,
    InputTextModule,
    InputNumberModule,
    DatePickerModule,
    ButtonModule,
  ],
  template: `
    <p-dialog
      [(visible)]="visible"
      header="New Lease"
      [modal]="true"
      [style]="{ width: '550px' }"
      (onHide)="onHide()"
    >
      <form #f="ngForm" (ngSubmit)="onSubmit()" class="flex flex-column gap-3">
        <div class="flex flex-column gap-1">
          <label class="text-sm font-medium">Tenant Name *</label>
          <input pInputText [(ngModel)]="model.tenantName" name="tenantName" required />
        </div>
        <div class="grid">
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">Phone</label>
            <input pInputText [(ngModel)]="model.tenantPhone" name="tenantPhone" class="w-full" />
          </div>
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">Email</label>
            <input pInputText [(ngModel)]="model.tenantEmail" name="tenantEmail" class="w-full" />
          </div>
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">Start Date *</label>
            <p-datepicker
              [(ngModel)]="startDate"
              name="startDate"
              dateFormat="yy-mm-dd"
              class="w-full"
            />
          </div>
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">End Date *</label>
            <p-datepicker
              [(ngModel)]="endDate"
              name="endDate"
              dateFormat="yy-mm-dd"
              class="w-full"
            />
          </div>
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">Rent Amount *</label>
            <p-inputNumber
              [(ngModel)]="model.rentAmount"
              name="rentAmount"
              mode="currency"
              currency="USD"
              class="w-full"
            />
          </div>
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">Deposit</label>
            <p-inputNumber
              [(ngModel)]="model.depositAmount"
              name="depositAmount"
              mode="currency"
              currency="USD"
              class="w-full"
            />
          </div>
        </div>

        <div class="flex gap-2 justify-content-end">
          <p-button label="Cancel" severity="secondary" (onClick)="onHide()" />
          <p-button type="submit" label="Create Lease" [loading]="saving" />
        </div>
      </form>
    </p-dialog>
  `,
})
export class LeaseFormDialogComponent implements OnChanges {
  @Input() visible = false;
  @Input() propertyId = '';
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() saved = new EventEmitter<void>();

  saving = false;
  startDate?: Date;
  endDate?: Date;

  model: LeaseRequest = {
    unitId: '',
    tenantName: '',
    tenantPhone: '',
    tenantEmail: '',
    startDate: '',
    endDate: '',
    rentAmount: 0,
    depositAmount: 0,
  };

  constructor(
    private api: PropertyApiService,
    private message: MessageService,
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['visible']?.currentValue) {
      this.model = {
        unitId: '',
        tenantName: '',
        tenantPhone: '',
        tenantEmail: '',
        startDate: '',
        endDate: '',
        rentAmount: 0,
        depositAmount: 0,
      };
      this.startDate = undefined;
      this.endDate = undefined;
    }
  }

  onSubmit() {
    if (!this.model.tenantName || !this.startDate || !this.endDate) return;

    this.model.startDate = this.formatDate(this.startDate);
    this.model.endDate = this.formatDate(this.endDate);

    // Use first available unit for now
    this.api.getUnitsByProperty(this.propertyId).subscribe({
      next: (units) => {
        if (units.length === 0) {
          this.message.add({
            severity: 'error',
            summary: 'Error',
            detail: 'No units available for this property',
          });
          return;
        }
        this.model.unitId = units[0].id;
        this.saving = true;
        this.api.createLease(this.model).subscribe({
          next: () => {
            this.message.add({
              severity: 'success',
              summary: 'Created',
              detail: 'Lease created successfully',
            });
            this.saving = false;
            this.saved.emit();
          },
          error: () => {
            this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to create lease' });
            this.saving = false;
          },
        });
      },
    });
  }

  private formatDate(d: Date): string {
    return d.toISOString().split('T')[0];
  }

  onHide() {
    this.visibleChange.emit(false);
  }
}

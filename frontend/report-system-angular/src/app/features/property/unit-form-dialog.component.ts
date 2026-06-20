import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { PropertyApiService } from './services/property-api.service';
import { Unit, UnitRequest } from './models/unit.model';

const UNIT_STATUSES = ['VACANT', 'OCCUPIED', 'MAINTENANCE'];
const UNIT_TYPES = ['STUDIO', 'ONE_BEDROOM', 'TWO_BEDROOM', 'THREE_BEDROOM', 'PENTHOUSE'];

@Component({
  selector: 'app-unit-form-dialog',
  standalone: true,
  imports: [
    FormsModule,
    DialogModule,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    ButtonModule,
  ],
  template: `
    <p-dialog
      [(visible)]="visible"
      [header]="unit ? 'Edit Unit' : 'Add Unit'"
      [modal]="true"
      [style]="{ width: '500px' }"
      (onHide)="onHide()"
    >
      <form #f="ngForm" (ngSubmit)="onSubmit()" class="flex flex-column gap-3">
        <div class="flex flex-column gap-1">
          <label class="text-sm font-medium">Label *</label>
          <input pInputText [(ngModel)]="model.label" name="label" required />
        </div>
        <div class="grid">
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">Floor</label>
            <p-inputNumber
              [(ngModel)]="model.floor"
              name="floor"
              class="w-full"
            />
          </div>
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">Status</label>
            <p-select
              [options]="statuses"
              [(ngModel)]="model.status"
              name="status"
              class="w-full"
            />
          </div>
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">Bedrooms</label>
            <p-inputNumber
              [(ngModel)]="model.bedrooms"
              name="bedrooms"
              class="w-full"
            />
          </div>
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">Bathrooms</label>
            <p-inputNumber
              [(ngModel)]="model.bathrooms"
              name="bathrooms"
              class="w-full"
            />
          </div>
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">Rent Amount</label>
            <p-inputNumber
              [(ngModel)]="model.rentAmount"
              name="rentAmount"
              mode="currency"
              currency="USD"
              class="w-full"
            />
          </div>
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">Type</label>
            <p-select
              [options]="types"
              [(ngModel)]="model.type"
              name="type"
              class="w-full"
            />
          </div>
        </div>

        <div class="flex gap-2 justify-content-end">
          <p-button label="Cancel" severity="secondary" (onClick)="onHide()" />
          <p-button type="submit" label="Save" [loading]="saving" />
        </div>
      </form>
    </p-dialog>
  `,
})
export class UnitFormDialogComponent implements OnChanges {
  @Input() visible = false;
  @Input() unit?: Unit;
  @Input() propertyId = '';
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() saved = new EventEmitter<void>();

  saving = false;
  model: UnitRequest = {
    propertyId: '',
    label: '',
    floor: 0,
    bedrooms: 0,
    bathrooms: 0,
    rentAmount: 0,
    status: 'VACANT',
    type: 'STUDIO',
  };

  statuses = UNIT_STATUSES;
  types = UNIT_TYPES;

  constructor(
    private api: PropertyApiService,
    private message: MessageService,
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['unit'] && this.unit) {
      this.model = {
        propertyId: this.unit.propertyId,
        label: this.unit.label,
        floor: this.unit.floor,
        bedrooms: this.unit.bedrooms,
        bathrooms: this.unit.bathrooms,
        rentAmount: this.unit.rentAmount,
        status: this.unit.status,
        type: this.unit.type,
      };
    }
    if (changes['visible']?.currentValue && !this.unit) {
      this.model = {
        propertyId: this.propertyId,
        label: '',
        floor: 0,
        bedrooms: 0,
        bathrooms: 0,
        rentAmount: 0,
        status: 'VACANT',
        type: 'STUDIO',
      };
    }
  }

  onSubmit() {
    if (!this.model.label) return;
    this.saving = true;

    const op = this.unit
      ? this.api.updateUnit(this.unit.id, this.model)
      : this.api.createUnit(this.model);

    op.subscribe({
      next: () => {
        this.message.add({
          severity: 'success',
          summary: this.unit ? 'Updated' : 'Created',
          detail: `Unit ${this.model.label} saved`,
        });
        this.saving = false;
        this.saved.emit();
      },
      error: () => {
        this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to save unit' });
        this.saving = false;
      },
    });
  }

  onHide() {
    this.visibleChange.emit(false);
  }
}

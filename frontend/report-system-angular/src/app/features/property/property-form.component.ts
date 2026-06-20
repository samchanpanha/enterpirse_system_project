import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextarea } from 'primeng/inputtextarea';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { PropertyApiService } from './services/property-api.service';
import { Property, PropertyRequest } from './models/property.model';

const PROPERTY_TYPES = [
  'APARTMENT', 'CONDO', 'HOUSE', 'VILLA', 'COMMERCIAL', 'WAREHOUSE', 'LAND', 'OFFICE',
];
const STATUSES = ['ACTIVE', 'INACTIVE', 'UNDER_MAINTENANCE'];

@Component({
  selector: 'app-property-form',
  standalone: true,
  imports: [
    FormsModule,
    InputTextModule,
    InputTextarea,
    SelectModule,
    ButtonModule,
    ToastModule,
  ],
  template: `
    <p-toast />
    <div class="max-w-3xl mx-auto">
      <div class="flex align-items-center gap-2 mb-4">
        <button
          class="p-2 border-none bg-transparent cursor-pointer text-gray-500 hover:text-gray-700"
          (click)="back()"
        >
          <i class="pi pi-arrow-left text-xl"></i>
        </button>
        <h2 class="text-xl font-bold m-0">
          {{ isEdit ? 'Edit Property' : 'Add Property' }}
        </h2>
      </div>

      <form #form="ngForm" (ngSubmit)="onSubmit()" class="flex flex-column gap-3">
        <div class="grid">
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">Name *</label>
            <input
              pInputText
              [(ngModel)]="model.name"
              name="name"
              required
              class="w-full"
            />
          </div>
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">Type *</label>
            <p-select
              [options]="propertyTypes"
              [(ngModel)]="model.type"
              name="type"
              required
              class="w-full"
              placeholder="Select type"
            />
          </div>
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">Address</label>
            <input pInputText [(ngModel)]="model.address" name="address" class="w-full" />
          </div>
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">City</label>
            <input pInputText [(ngModel)]="model.city" name="city" class="w-full" />
          </div>
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">District</label>
            <input pInputText [(ngModel)]="model.district" name="district" class="w-full" />
          </div>
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">Status</label>
            <p-select
              [options]="statuses"
              [(ngModel)]="model.status"
              name="status"
              class="w-full"
              placeholder="Select status"
            />
          </div>
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">Owner Name</label>
            <input pInputText [(ngModel)]="model.ownerName" name="ownerName" class="w-full" />
          </div>
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">Owner Phone</label>
            <input pInputText [(ngModel)]="model.ownerPhone" name="ownerPhone" class="w-full" />
          </div>
          <div class="col-12">
            <label class="text-sm font-medium block mb-1">Notes</label>
            <textarea
              pInputTextarea
              [(ngModel)]="model.notes"
              name="notes"
              rows="3"
              class="w-full"
            ></textarea>
          </div>
        </div>

        <div class="flex gap-2 justify-content-end">
          <p-button
            label="Cancel"
            severity="secondary"
            (onClick)="back()"
          />
          <p-button
            type="submit"
            label="Save"
            [loading]="saving"
          />
        </div>
      </form>
    </div>
  `,
})
export class PropertyFormComponent implements OnInit {
  isEdit = false;
  propertyId?: string;
  saving = false;

  model: PropertyRequest = {
    name: '',
    type: '',
    address: '',
    city: '',
    district: '',
    status: 'ACTIVE',
    ownerName: '',
    ownerPhone: '',
    notes: '',
  };

  propertyTypes = PROPERTY_TYPES;
  statuses = STATUSES;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: PropertyApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.propertyId = id;
      this.loadProperty(id);
    }
  }

  loadProperty(id: string) {
    this.api.getProperty(id).subscribe({
      next: (p) => {
        this.model = {
          name: p.name,
          type: p.type,
          address: p.address,
          city: p.city,
          district: p.district,
          status: p.status,
          ownerName: p.ownerName,
          ownerPhone: p.ownerPhone,
          notes: p.notes,
        };
      },
    });
  }

  onSubmit() {
    if (!this.model.name || !this.model.type) return;
    this.saving = true;

    const op = this.isEdit
      ? this.api.updateProperty(this.propertyId!, this.model)
      : this.api.createProperty(this.model);

    op.subscribe({
      next: () => {
        this.message.add({
          severity: 'success',
          summary: this.isEdit ? 'Updated' : 'Created',
          detail: `Property ${this.isEdit ? 'updated' : 'created'} successfully`,
        });
        this.back();
      },
      error: () => {
        this.message.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to save property',
        });
        this.saving = false;
      },
    });
  }

  back() {
    this.router.navigate(['/properties']);
  }
}

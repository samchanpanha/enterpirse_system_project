import { Component, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { RealtyApiService } from './services/realty-api.service';
import { Listing, ListingRequest } from './models/realty.model';

@Component({
  selector: 'app-listing-list',
  standalone: true,
  imports: [CurrencyPipe, TableModule, ButtonModule, TagModule, DialogModule, InputTextModule, InputNumberModule, SelectModule, FormsModule, ToastModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Property Listings</h2>
      <p-button label="New Listing" icon="pi pi-plus" severity="success" (onClick)="openDialog()" />
    </div>

    <p-table [value]="listings" [paginator]="true" [rows]="15" [loading]="loading" styleClass="p-datatable-striped" [globalFilterFields]="['title','propertyType','city','district','status']">
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input pInputText type="text" placeholder="Search listings..." />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Title</th>
          <th>Type</th>
          <th>Price</th>
          <th>Area</th>
          <th>Beds</th>
          <th>City</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-l>
        <tr>
          <td class="font-medium">{{ l.title }}</td>
          <td><p-tag [value]="l.propertyType" severity="info" /></td>
          <td>{{ l.price | currency: l.currency }}</td>
          <td>{{ l.areaSqm }} m²</td>
          <td>{{ l.bedrooms }}</td>
          <td>{{ l.city }}</td>
          <td><p-tag [value]="l.status" [severity]="statusSeverity(l.status)" /></td>
          <td>
            <div class="flex gap-1">
              <p-button icon="pi pi-pencil" severity="info" size="small" (onClick)="edit(l)" />
              <p-button icon="pi pi-trash" severity="danger" size="small" (onClick)="delete(l)" />
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="8" class="text-center text-gray-500 py-4">No listings found.</td></tr>
      </ng-template>
    </p-table>

    <p-dialog [(visible)]="showDialog" [header]="editMode ? 'Edit Listing' : 'New Listing'" [modal]="true" [style]="{ width: '600px' }">
      <div class="flex flex-column gap-3">
        <div><label class="text-sm font-medium block mb-1">Title *</label><input pInputText [(ngModel)]="form.title" class="w-full" /></div>
        <div class="grid">
          <div class="col-6">
            <label class="text-sm font-medium block mb-1">Property Type *</label>
            <p-select [(ngModel)]="form.propertyType" [options]="propertyTypes" optionLabel="label" optionValue="value" class="w-full" />
          </div>
          <div class="col-6"><label class="text-sm font-medium block mb-1">Price</label><p-inputNumber [(ngModel)]="form.price" mode="currency" currency="USD" class="w-full" /></div>
        </div>
        <div class="grid">
          <div class="col-4"><label class="text-sm font-medium block mb-1">Area (m²)</label><p-inputNumber [(ngModel)]="form.areaSqm" class="w-full" /></div>
          <div class="col-4"><label class="text-sm font-medium block mb-1">Bedrooms</label><p-inputNumber [(ngModel)]="form.bedrooms" class="w-full" /></div>
          <div class="col-4"><label class="text-sm font-medium block mb-1">Bathrooms</label><p-inputNumber [(ngModel)]="form.bathrooms" class="w-full" /></div>
        </div>
        <div><label class="text-sm font-medium block mb-1">Address</label><input pInputText [(ngModel)]="form.address" class="w-full" /></div>
        <div class="grid">
          <div class="col-4"><label class="text-sm font-medium block mb-1">City</label><input pInputText [(ngModel)]="form.city" class="w-full" /></div>
          <div class="col-4"><label class="text-sm font-medium block mb-1">District</label><input pInputText [(ngModel)]="form.district" class="w-full" /></div>
        </div>
        <p-button [label]="editMode ? 'Update' : 'Create'" [loading]="saving" (onClick)="save()" />
      </div>
    </p-dialog>
  `,
})
export class ListingListComponent implements OnInit {
  listings: Listing[] = [];
  loading = false;
  saving = false;
  showDialog = false;
  editMode = false;
  editId = '';
  propertyTypes = [
    { label: 'Apartment', value: 'APARTMENT' },
    { label: 'Villa', value: 'VILLA' },
    { label: 'Condo', value: 'CONDO' },
    { label: 'Townhouse', value: 'TOWNHOUSE' },
    { label: 'Commercial', value: 'COMMERCIAL' },
    { label: 'Land', value: 'LAND' },
  ];
  form: ListingRequest = { title: '', propertyType: 'APARTMENT' };

  constructor(private api: RealtyApiService, private message: MessageService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.api.getListings().subscribe({ next: (res) => { this.listings = res; this.loading = false; }, error: () => { this.loading = false; } });
  }

  openDialog() { this.editMode = false; this.form = { title: '', propertyType: 'APARTMENT' }; this.showDialog = true; }

  edit(l: Listing) {
    this.editMode = true; this.editId = l.id;
    this.form = { title: l.title, propertyType: l.propertyType, price: l.price, areaSqm: l.areaSqm, bedrooms: l.bedrooms, bathrooms: l.bathrooms, address: l.address, city: l.city, district: l.district };
    this.showDialog = true;
  }

  save() {
    if (!this.form.title || !this.form.propertyType) return;
    this.saving = true;
    const req = this.editMode ? this.api.updateListing(this.editId, this.form as any) : this.api.createListing(this.form);
    req.subscribe({ next: () => { this.message.add({ severity: 'success', summary: this.editMode ? 'Updated' : 'Created' }); this.showDialog = false; this.load(); this.saving = false; }, error: () => { this.saving = false; } });
  }

  delete(l: Listing) {
    if (!confirm(`Delete "${l.title}"?`)) return;
    this.api.deleteListing(l.id).subscribe({ next: () => { this.message.add({ severity: 'success', summary: 'Deleted' }); this.load(); } });
  }

  statusSeverity(s: string) { return ({ ACTIVE: 'success', SOLD: 'info', RENTED: 'warn', WITHDRAWN: 'danger', EXPIRED: 'danger' } as Record<string, string>)[s] || 'info'; }
}

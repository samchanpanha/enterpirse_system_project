import { Component, OnInit } from '@angular/core';
import { CurrencyPipe, SlicePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';
import { DeliveryApiService } from './services/delivery-api.service';
import { Delivery, DeliveryRequest } from './models/delivery.model';

@Component({
  selector: 'app-delivery-list',
  standalone: true,
  imports: [SlicePipe, TableModule, ButtonModule, TagModule, DialogModule, InputTextModule, SelectModule, FormsModule, ToastModule, TooltipModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Dispatch</h2>
      <div class="flex gap-2">
        <p-select [options]="statusFilter" optionLabel="label" optionValue="value" [(ngModel)]="selectedStatus" placeholder="All Status" class="w-10rem" (onChange)="load()" [showClear]="true" />
        <p-button label="New Delivery" icon="pi pi-plus" severity="success" (onClick)="showDialog = true" />
      </div>
    </div>

    <p-table [value]="deliveries" [paginator]="true" [rows]="15" [loading]="loading" styleClass="p-datatable-striped" [globalFilterFields]="['customerName','deliveryAddress','status']">
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input pInputText type="text" (input)="onSearch($event)" placeholder="Search deliveries..." />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>ID</th>
          <th>Customer</th>
          <th>Phone</th>
          <th>Pickup</th>
          <th>Delivery Address</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-d>
        <tr>
          <td class="font-medium text-sm">{{ d.id | slice:0:8 }}</td>
          <td>{{ d.customerName }}</td>
          <td>{{ d.customerPhone }}</td>
          <td>{{ d.pickupAddress }}</td>
          <td>{{ d.deliveryAddress }}</td>
          <td><p-tag [value]="d.status" [severity]="statusSeverity(d.status)" /></td>
          <td>
            <div class="flex gap-1">
              @if (d.status === 'PENDING') {
                <p-button icon="pi pi-check" severity="success" size="small" (onClick)="updateStatus(d, 'ASSIGNED')" pTooltip="Assign" />
              }
              @if (d.status === 'ASSIGNED') {
                <p-button icon="pi pi-arrow-up" severity="info" size="small" (onClick)="updateStatus(d, 'PICKED_UP')" pTooltip="Picked Up" />
              }
              @if (d.status === 'PICKED_UP') {
                <p-button icon="pi pi-send" severity="warn" size="small" (onClick)="updateStatus(d, 'IN_TRANSIT')" pTooltip="In Transit" />
              }
              @if (d.status === 'IN_TRANSIT') {
                <p-button icon="pi pi-check-circle" severity="success" size="small" (onClick)="updateStatus(d, 'DELIVERED')" pTooltip="Delivered" />
              }
              @if (d.status !== 'DELIVERED' && d.status !== 'CANCELLED') {
                <p-button icon="pi pi-times" severity="danger" size="small" (onClick)="updateStatus(d, 'CANCELLED')" pTooltip="Cancel" />
              }
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="7" class="text-center text-gray-500 py-4">No deliveries found.</td></tr>
      </ng-template>
    </p-table>

    <p-dialog [(visible)]="showDialog" header="New Delivery" [modal]="true" [style]="{ width: '500px' }">
      <div class="flex flex-column gap-3">
        <div><label class="text-sm font-medium block mb-1">Customer Name *</label><input pInputText [(ngModel)]="form.customerName" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Customer Phone *</label><input pInputText [(ngModel)]="form.customerPhone" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Pickup Address *</label><input pInputText [(ngModel)]="form.pickupAddress" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Delivery Address *</label><input pInputText [(ngModel)]="form.deliveryAddress" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Notes</label><input pInputText [(ngModel)]="form.notes" class="w-full" /></div>
        <p-button label="Create Delivery" [loading]="saving" (onClick)="create()" />
      </div>
    </p-dialog>
  `,
})
export class DeliveryListComponent implements OnInit {
  deliveries: Delivery[] = [];
  loading = false;
  saving = false;
  showDialog = false;
  selectedStatus = '';
  statusFilter = [
    { label: 'Pending', value: 'PENDING' },
    { label: 'Assigned', value: 'ASSIGNED' },
    { label: 'In Transit', value: 'IN_TRANSIT' },
    { label: 'Delivered', value: 'DELIVERED' },
    { label: 'Cancelled', value: 'CANCELLED' },
  ];
  form: DeliveryRequest = { customerName: '', customerPhone: '', pickupAddress: '', deliveryAddress: '' };

  constructor(private api: DeliveryApiService, private message: MessageService) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.api.getDeliveries().subscribe({
      next: (res) => { this.deliveries = this.selectedStatus ? res.filter(d => d.status === this.selectedStatus) : res; this.loading = false; },
      error: () => { this.loading = false; this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load deliveries' }); },
    });
  }

  create() {
    if (!this.form.customerName || !this.form.deliveryAddress) return;
    this.saving = true;
    this.api.createDelivery(this.form).subscribe({
      next: () => { this.message.add({ severity: 'success', summary: 'Created', detail: 'Delivery created' }); this.showDialog = false; this.form = { customerName: '', customerPhone: '', pickupAddress: '', deliveryAddress: '' }; this.load(); this.saving = false; },
      error: () => { this.saving = false; this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to create delivery' }); },
    });
  }

  updateStatus(d: Delivery, status: string) {
    this.api.updateDeliveryStatus(d.id, status).subscribe({
      next: () => { this.message.add({ severity: 'success', summary: 'Updated', detail: `Delivery ${status.toLowerCase()}` }); this.load(); },
      error: () => { this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to update status' }); },
    });
  }

  statusSeverity(status: string) {
    const m: Record<string, string> = { PENDING: 'warn', ASSIGNED: 'info', PICKED_UP: 'info', IN_TRANSIT: 'warn', DELIVERED: 'success', CANCELLED: 'danger' };
    return m[status] || 'info';
  }

  onSearch(event: Event) {}
}

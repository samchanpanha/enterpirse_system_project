import { Component, OnInit } from '@angular/core';
import { DatePipe, SlicePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { CardModule } from 'primeng/card';
import { DeliveryApiService } from './services/delivery-api.service';
import { Delivery } from './models/delivery.model';

@Component({
  selector: 'app-delivery-tracking',
  standalone: true,
  imports: [DatePipe, SlicePipe, TableModule, TagModule, CardModule],
  template: `
    <h2 class="text-xl font-bold mb-3">Live Tracking</h2>

    <div class="grid mb-4">
      <div class="col-12 md:col-3">
        <div class="card p-3 surface-card border-round shadow-1 text-center">
          <div class="text-2xl font-bold text-orange-500">{{ activeCount }}</div>
          <div class="text-sm text-gray-500">Active Deliveries</div>
        </div>
      </div>
      <div class="col-12 md:col-3">
        <div class="card p-3 surface-card border-round shadow-1 text-center">
          <div class="text-2xl font-bold text-blue-500">{{ transitCount }}</div>
          <div class="text-sm text-gray-500">In Transit</div>
        </div>
      </div>
      <div class="col-12 md:col-3">
        <div class="card p-3 surface-card border-round shadow-1 text-center">
          <div class="text-2xl font-bold text-green-500">{{ deliveredToday }}</div>
          <div class="text-sm text-gray-500">Delivered Today</div>
        </div>
      </div>
      <div class="col-12 md:col-3">
        <div class="card p-3 surface-card border-round shadow-1 text-center">
          <div class="text-2xl font-bold text-red-500">{{ cancelledCount }}</div>
          <div class="text-sm text-gray-500">Cancelled</div>
        </div>
      </div>
    </div>

    <p-table [value]="activeDeliveries" [loading]="loading" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>ID</th>
          <th>Customer</th>
          <th>Pickup</th>
          <th>Delivery Address</th>
          <th>Status</th>
          <th>Created</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-d>
        <tr>
          <td class="font-medium text-sm">{{ d.id | slice:0:8 }}</td>
          <td>{{ d.customerName }}</td>
          <td>{{ d.pickupAddress }}</td>
          <td>{{ d.deliveryAddress }}</td>
          <td><p-tag [value]="d.status" [severity]="severity(d.status)" /></td>
          <td>{{ d.createdAt | date:'short' }}</td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="6" class="text-center text-gray-500 py-4">No active deliveries.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class DeliveryTrackingComponent implements OnInit {
  allDeliveries: Delivery[] = [];
  activeDeliveries: Delivery[] = [];
  loading = false;
  activeCount = 0;
  transitCount = 0;
  deliveredToday = 0;
  cancelledCount = 0;

  constructor(private api: DeliveryApiService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.api.getDeliveries().subscribe({
      next: (res) => {
        this.allDeliveries = res;
        this.activeDeliveries = res.filter(d => ['PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT'].includes(d.status));
        this.activeCount = res.filter(d => !['DELIVERED', 'CANCELLED'].includes(d.status)).length;
        this.transitCount = res.filter(d => d.status === 'IN_TRANSIT').length;
        this.deliveredToday = res.filter(d => d.status === 'DELIVERED').length;
        this.cancelledCount = res.filter(d => d.status === 'CANCELLED').length;
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  severity(s: string) { return ({ PENDING: 'warn', ASSIGNED: 'info', PICKED_UP: 'info', IN_TRANSIT: 'warn', DELIVERED: 'success', CANCELLED: 'danger' } as Record<string, string>)[s] || 'info'; }
}

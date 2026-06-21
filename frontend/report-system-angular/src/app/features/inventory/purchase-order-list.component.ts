import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { InventoryApiService } from './services/inventory-api.service';
import { PurchaseOrder } from './models/purchase-order.model';

@Component({
  selector: 'app-purchase-order-list',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, TableModule, ButtonModule, TagModule, ToastModule],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Purchase Orders</h2>
    </div>

    <p-table
      [value]="orders"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['poNumber', 'status']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="header">
        <tr>
          <th>PO #</th>
          <th>Status</th>
          <th>Order Date</th>
          <th>Expected</th>
          <th>Total</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-po>
        <tr>
          <td class="font-medium">{{ po.poNumber }}</td>
          <td>
            <p-tag
              [value]="po.status"
              [severity]="po.status === 'RECEIVED' ? 'success' : po.status === 'CANCELLED' ? 'danger' : 'info'"
            />
          </td>
          <td>{{ po.orderDate | date }}</td>
          <td>{{ po.expectedDate | date }}</td>
          <td>{{ po.total | currency }}</td>
          <td>
            <div class="flex gap-1">
              <p-button label="View" size="small" severity="info" (onClick)="router.navigate(['inventory', 'purchase-orders', po.id])" />
              @if (po.status === 'PENDING') {
                <p-button label="Receive" size="small" severity="success" (onClick)="receive(po)" />
                <p-button label="Cancel" size="small" severity="danger" (onClick)="cancel(po)" />
              }
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="6" class="text-center text-gray-500 py-4">No purchase orders found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class PurchaseOrderListComponent implements OnInit {
  orders: PurchaseOrder[] = [];
  loading = false;

  constructor(
    protected router: Router,
    private api: InventoryApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.api.getPurchaseOrders().subscribe({
      next: (res) => { this.orders = res; this.loading = false; },
      error: () => { this.loading = false; this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load purchase orders' }); },
    });
  }

  receive(po: PurchaseOrder) {
    this.api.receivePurchaseOrder(po.id).subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Received', detail: `${po.poNumber} marked as received` });
        this.load();
      },
    });
  }

  cancel(po: PurchaseOrder) {
    this.api.cancelPurchaseOrder(po.id).subscribe({
      next: () => {
        this.message.add({ severity: 'info', summary: 'Cancelled', detail: `${po.poNumber} cancelled` });
        this.load();
      },
    });
  }
}

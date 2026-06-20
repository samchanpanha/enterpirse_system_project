import { Component, OnInit } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { InventoryApiService } from './services/inventory-api.service';
import { StockTransfer } from './models/stock-transfer.model';

@Component({
  selector: 'app-stock-transfer-list',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, TableModule, ButtonModule, TagModule, ToastModule],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Stock Transfers</h2>
    </div>

    <p-table
      [value]="transfers"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="header">
        <tr>
          <th>Transfer #</th>
          <th>Status</th>
          <th>Items</th>
          <th>Total Value</th>
          <th>Created</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-t>
        <tr>
          <td class="font-medium">{{ t.transferNumber }}</td>
          <td>
            <p-tag
              [value]="t.status"
              [severity]="t.status === 'RECEIVED' ? 'success' : t.status === 'SHIPPED' ? 'warn' : t.status === 'DRAFT' ? 'info' : 'danger'"
            />
          </td>
          <td>{{ t.totalItems }}</td>
          <td>{{ t.totalValue | currency }}</td>
          <td>{{ t.createdAt | date }}</td>
          <td>
            <div class="flex gap-1">
              @if (t.status === 'DRAFT') {
                <p-button label="Ship" size="small" severity="warn" (onClick)="ship(t)" />
                <p-button label="Cancel" size="small" severity="danger" (onClick)="cancel(t)" />
              }
              @if (t.status === 'SHIPPED') {
                <p-button label="Receive" size="small" severity="success" (onClick)="receive(t)" />
              }
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="6" class="text-center text-gray-500 py-4">No transfers found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class StockTransferListComponent implements OnInit {
  transfers: StockTransfer[] = [];
  loading = false;

  constructor(
    private api: InventoryApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.api.getTransfers().subscribe({
      next: (res) => { this.transfers = res; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  ship(t: StockTransfer) {
    this.api.shipTransfer(t.id).subscribe({
      next: () => { this.message.add({ severity: 'success', summary: 'Shipped', detail: 'Transfer shipped' }); this.load(); },
    });
  }

  receive(t: StockTransfer) {
    this.api.receiveTransfer(t.id).subscribe({
      next: () => { this.message.add({ severity: 'success', summary: 'Received', detail: 'Transfer received' }); this.load(); },
    });
  }

  cancel(t: StockTransfer) {
    this.api.cancelTransfer(t.id).subscribe({
      next: () => { this.message.add({ severity: 'info', summary: 'Cancelled', detail: 'Transfer cancelled' }); this.load(); },
    });
  }
}

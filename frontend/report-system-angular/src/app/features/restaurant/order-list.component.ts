import { Component, Input, OnInit } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { RestaurantApiService } from './services/restaurant-api.service';
import { Order } from './models/order.model';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [
    FormsModule,
    CurrencyPipe,
    DatePipe,
    TableModule,
    ButtonModule,
    TagModule,
    DialogModule,
    InputTextModule,
    SelectModule,
    ToastModule,
    ConfirmDialogModule,
  ],
  providers: [ConfirmationService],
  template: `
    <p-toast />
    <p-confirmdialog />

    <div class="flex justify-content-between align-items-center mb-2">
      <p-select
        [options]="statusOptions"
        [(ngModel)]="selectedStatus"
        class="w-10rem"
        (onChange)="loadOrders()"
      />
    </div>

    <p-table [value]="orders" [loading]="loading" size="small" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>#</th>
          <th>Type</th>
          <th>Items</th>
          <th>Total</th>
          <th>Payment</th>
          <th>Status</th>
          <th>Date</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-o>
        <tr>
          <td class="font-medium">{{ o.orderNumber }}</td>
          <td>{{ o.type }}</td>
          <td>{{ o.subtotal | currency }}</td>
          <td>{{ o.total | currency }}</td>
          <td>
            <p-tag
              [value]="o.paymentStatus"
              [severity]="o.paymentStatus === 'paid' ? 'success' : 'warn'"
            />
          </td>
          <td>
            <p-tag
              [value]="o.status"
              [severity]="o.status === 'completed' ? 'success' : o.status === 'cancelled' ? 'danger' : 'info'"
            />
          </td>
          <td>{{ o.createdAt | date:'short' }}</td>
          <td>
            @if (o.status === 'pending') {
              <p-button
                label="Complete"
                size="small"
                severity="success"
                (onClick)="completeOrder(o)"
              />
            }
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="8" class="text-center text-gray-500 py-3">No orders found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class OrderListComponent implements OnInit {
  @Input() outletId = '';

  orders: Order[] = [];
  loading = false;
  selectedStatus = 'pending';
  statusOptions = ['pending', 'in_progress', 'completed', 'cancelled', 'all'];

  constructor(
    private api: RestaurantApiService,
    private message: MessageService,
    private confirmation: ConfirmationService,
  ) {}

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    this.loading = true;
    const status = this.selectedStatus === 'all' ? 'pending' : this.selectedStatus;
    this.api.getOrdersByOutlet(this.outletId, status).subscribe({
      next: (res) => {
        this.orders = res;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  completeOrder(o: Order) {
    this.confirmation.confirm({
      header: 'Complete Order',
      message: `Complete order ${o.orderNumber}?`,
      accept: () => {
        this.api.completeOrder(o.id, {}).subscribe({
          next: () => {
            this.message.add({ severity: 'success', summary: 'Completed', detail: `Order ${o.orderNumber} completed` });
            this.loadOrders();
          },
        });
      },
    });
  }
}

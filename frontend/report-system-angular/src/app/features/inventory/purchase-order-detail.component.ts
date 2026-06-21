import { Component, OnInit } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { InventoryApiService } from './services/inventory-api.service';
import { PurchaseOrder, PurchaseOrderItem } from './models/purchase-order.model';

@Component({
  selector: 'app-purchase-order-detail',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, TableModule, ButtonModule, TagModule, ToastModule],
  template: `
    <p-toast />

    <div class="flex align-items-center gap-2 mb-4">
      <button class="p-2 border-none bg-transparent cursor-pointer text-gray-500 hover:text-gray-700"
        (click)="router.navigate(['/inventory'])"><i class="pi pi-arrow-left text-xl"></i></button>
      <h2 class="text-xl font-bold m-0">Purchase Order {{ po?.poNumber }}</h2>
      @if (po) { <p-tag [value]="po.status" [severity]="severity(po.status)" /> }
    </div>

    @if (po) {
      <div class="grid">
        <div class="col-12 md:col-6">
          <div class="card p-3 surface-card border-round shadow-1">
            <h3 class="text-sm font-bold text-gray-500 mb-2">Details</h3>
            <div class="flex flex-column gap-2 text-sm">
              <div class="flex justify-content-between"><span class="text-gray-500">PO #:</span><span class="font-medium">{{ po.poNumber }}</span></div>
              <div class="flex justify-content-between"><span class="text-gray-500">Status:</span><p-tag [value]="po.status" [severity]="severity(po.status)" /></div>
              <div class="flex justify-content-between"><span class="text-gray-500">Order Date:</span><span>{{ po.orderDate | date }}</span></div>
              <div class="flex justify-content-between"><span class="text-gray-500">Expected:</span><span>{{ po.expectedDate | date }}</span></div>
              <div class="flex justify-content-between"><span class="text-gray-500">Received:</span><span>{{ po.receivedDate ? (po.receivedDate | date) : '—' }}</span></div>
              <div class="flex justify-content-between"><span class="text-gray-500">Supplier ID:</span><span class="font-mono text-sm">{{ po.supplierId }}</span></div>
            </div>
          </div>
        </div>
        <div class="col-12 md:col-6">
          <div class="card p-3 surface-card border-round shadow-1">
            <h3 class="text-sm font-bold text-gray-500 mb-2">Totals</h3>
            <div class="flex flex-column gap-2 text-sm">
              <div class="flex justify-content-between"><span class="text-gray-500">Subtotal:</span><span>{{ po.subtotal | currency }}</span></div>
              <div class="flex justify-content-between"><span class="text-gray-500">Tax:</span><span>{{ po.taxAmount | currency }}</span></div>
              <div class="flex justify-content-between"><span class="text-gray-500">Shipping:</span><span>{{ po.shippingCost | currency }}</span></div>
              <div class="flex justify-content-between font-bold"><span>Total:</span><span>{{ po.total | currency }}</span></div>
            </div>
            @if (po.notes) {
              <div class="mt-3">
                <h4 class="text-xs font-bold text-gray-500 mb-1">Notes</h4>
                <p class="text-sm text-gray-700">{{ po.notes }}</p>
              </div>
            }
          </div>
        </div>
      </div>
    }

    <div class="card p-3 surface-card border-round shadow-1 mt-3">
      <h3 class="text-sm font-bold text-gray-500 mb-2">Items</h3>
      <p-table [value]="items" styleClass="p-datatable-striped">
        <ng-template pTemplate="header">
          <tr>
            <th>Product ID</th>
            <th>Ordered</th>
            <th>Received</th>
            <th>Unit Cost</th>
            <th>Total</th>
          </tr>
        </ng-template>
        <ng-template pTemplate="body" let-item>
          <tr>
            <td class="font-mono text-sm">{{ item.productId }}</td>
            <td>{{ item.quantityOrdered }}</td>
            <td>{{ item.quantityReceived }}</td>
            <td>{{ item.unitCost | currency }}</td>
            <td class="font-medium">{{ item.subtotal | currency }}</td>
          </tr>
        </ng-template>
        <ng-template pTemplate="emptymessage">
          <tr><td colspan="5" class="text-center text-gray-500 py-4">No items loaded.</td></tr>
        </ng-template>
      </p-table>
    </div>
  `,
})
export class PurchaseOrderDetailComponent implements OnInit {
  po?: PurchaseOrder;
  items: PurchaseOrderItem[] = [];
  loading = false;

  constructor(
    private route: ActivatedRoute,
    protected router: Router,
    private api: InventoryApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.load(id);
  }

  load(id: string) {
    this.loading = true;
    this.api.getPurchaseOrder(id).subscribe({
      next: (res) => {
        this.po = res;
        this.api.getPurchaseOrderItems(id).subscribe({
          next: (items) => {
            this.items = items;
            this.loading = false;
          },
          error: () => { this.loading = false; },
        });
      },
      error: () => {
        this.loading = false;
        this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load purchase order' });
      },
    });
  }

  severity(status: string) {
    switch (status) {
      case 'RECEIVED': return 'success';
      case 'CANCELLED': return 'danger';
      case 'PENDING': return 'info';
      default: return 'warn';
    }
  }
}

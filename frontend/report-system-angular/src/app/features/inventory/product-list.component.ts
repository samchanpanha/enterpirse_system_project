import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { InventoryApiService } from './services/inventory-api.service';
import { Product } from './models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CurrencyPipe, TableModule, ButtonModule, InputTextModule, TagModule, ToastModule],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Products</h2>
      <p-button
        label="Add Product"
        icon="pi pi-plus"
        severity="success"
        (onClick)="router.navigate(['inventory', 'products', 'new'])"
      />
    </div>

    <p-table
      [value]="products"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['name', 'sku', 'unit']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input pInputText type="text" placeholder="Search products..." />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>SKU</th>
          <th>Unit</th>
          <th>Unit Price</th>
          <th>Cost Price</th>
          <th>Stock</th>
          <th>Status</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-p>
        <tr>
          <td class="font-medium">{{ p.name }}</td>
          <td>{{ p.sku }}</td>
          <td>{{ p.unit }}</td>
          <td>{{ p.unitPrice | currency }}</td>
          <td>{{ p.costPrice | currency }}</td>
          <td>{{ p.minStock }} - {{ p.maxStock }}</td>
          <td>
            <p-tag
              [value]="p.active ? 'ACTIVE' : 'INACTIVE'"
              [severity]="p.active ? 'success' : 'danger'"
            />
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="7" class="text-center text-gray-500 py-4">No products found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
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
    this.api.getProducts().subscribe({
      next: (res) => {
        this.products = res;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load products' });
      },
    });
  }
}

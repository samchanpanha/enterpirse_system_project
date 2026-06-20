import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputSwitchModule } from 'primeng/inputswitch';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { InventoryApiService } from './services/inventory-api.service';
import { ProductRequest } from './models/product.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [FormsModule, InputTextModule, InputNumberModule, InputSwitchModule, ButtonModule, ToastModule],
  template: `
    <p-toast />
    <div class="max-w-3xl mx-auto">
      <div class="flex align-items-center gap-2 mb-4">
        <button class="p-2 border-none bg-transparent cursor-pointer text-gray-500 hover:text-gray-700"
          (click)="back()"><i class="pi pi-arrow-left text-xl"></i></button>
        <h2 class="text-xl font-bold m-0">{{ isEdit ? 'Edit Product' : 'Add Product' }}</h2>
      </div>

      <form #f="ngForm" (ngSubmit)="onSubmit()" class="flex flex-column gap-3">
        <div class="grid">
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">Name *</label>
            <input pInputText [(ngModel)]="model.name" name="name" required class="w-full" />
          </div>
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">SKU *</label>
            <input pInputText [(ngModel)]="model.sku" name="sku" required class="w-full" />
          </div>
          <div class="col-12 md:col-4">
            <label class="text-sm font-medium block mb-1">Unit</label>
            <input pInputText [(ngModel)]="model.unit" name="unit" class="w-full" placeholder="pcs" />
          </div>
          <div class="col-12 md:col-4">
            <label class="text-sm font-medium block mb-1">Unit Price *</label>
            <p-inputNumber [(ngModel)]="model.unitPrice" name="unitPrice" mode="currency" currency="USD" class="w-full" />
          </div>
          <div class="col-12 md:col-4">
            <label class="text-sm font-medium block mb-1">Cost Price</label>
            <p-inputNumber [(ngModel)]="model.costPrice" name="costPrice" mode="currency" currency="USD" class="w-full" />
          </div>
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">Min Stock</label>
            <p-inputNumber [(ngModel)]="model.minStock" name="minStock" class="w-full" />
          </div>
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">Max Stock</label>
            <p-inputNumber [(ngModel)]="model.maxStock" name="maxStock" class="w-full" />
          </div>
          <div class="col-12">
            <p-inputSwitch [(ngModel)]="model.tracked" name="tracked" inputId="tracked" />
            <label for="tracked" class="ml-2">Track inventory</label>
          </div>
        </div>

        <div class="flex gap-2 justify-content-end">
          <p-button label="Cancel" severity="secondary" (onClick)="back()" />
          <p-button type="submit" label="Save" [loading]="saving" />
        </div>
      </form>
    </div>
  `,
})
export class ProductFormComponent implements OnInit {
  isEdit = false;
  productId?: string;
  saving = false;

  model: ProductRequest = {
    name: '',
    sku: '',
    unit: 'pcs',
    unitPrice: 0,
    costPrice: 0,
    minStock: 0,
    maxStock: 0,
    tracked: true,
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: InventoryApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.productId = id;
      this.api.getProduct(id).subscribe({
        next: (p) => {
          this.model = {
            name: p.name,
            sku: p.sku,
            unit: p.unit,
            unitPrice: p.unitPrice,
            costPrice: p.costPrice,
            minStock: p.minStock,
            maxStock: p.maxStock,
            tracked: p.tracked,
          };
        },
      });
    }
  }

  onSubmit() {
    if (!this.model.name || !this.model.sku) return;
    this.saving = true;
    const op = this.isEdit
      ? this.api.updateProduct(this.productId!, this.model)
      : this.api.createProduct(this.model);
    op.subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Saved', detail: `Product ${this.isEdit ? 'updated' : 'created'}` });
        this.back();
      },
      error: () => {
        this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to save product' });
        this.saving = false;
      },
    });
  }

  back() {
    this.router.navigate(['/inventory']);
  }
}

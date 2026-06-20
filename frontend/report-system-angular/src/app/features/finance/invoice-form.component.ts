import { Component, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { FinanceApiService } from './services/finance-api.service';
import { InvoiceRequest } from './models/invoice.model';

@Component({
  selector: 'app-invoice-form',
  standalone: true,
  imports: [CurrencyPipe, FormsModule, InputTextModule, InputNumberModule, SelectModule, ButtonModule, ToastModule],
  template: `
    <p-toast />
    <div class="max-w-3xl mx-auto">
      <div class="flex align-items-center gap-2 mb-4">
        <button class="p-2 border-none bg-transparent cursor-pointer text-gray-500 hover:text-gray-700"
          (click)="back()"><i class="pi pi-arrow-left text-xl"></i></button>
        <h2 class="text-xl font-bold m-0">New Invoice</h2>
      </div>

      <form #f="ngForm" (ngSubmit)="onSubmit()" class="flex flex-column gap-3">
        <div class="grid">
          <div class="col-12 md:col-4">
            <label class="text-sm font-medium block mb-1">Type *</label>
            <p-select
              [(ngModel)]="model.type"
              name="type"
              [options]="[{ label: 'Sale', value: 'SALE' }, { label: 'Purchase', value: 'PURCHASE' }]"
              optionLabel="label"
              optionValue="value"
              class="w-full"
            />
          </div>
          <div class="col-12 md:col-4">
            <label class="text-sm font-medium block mb-1">Invoice Date *</label>
            <input pInputText type="date" [(ngModel)]="model.invoiceDate" name="date" required class="w-full" />
          </div>
          <div class="col-12 md:col-4">
            <label class="text-sm font-medium block mb-1">Due Date *</label>
            <input pInputText type="date" [(ngModel)]="model.dueDate" name="due" required class="w-full" />
          </div>
          <div class="col-12">
            <label class="text-sm font-medium block mb-1">{{ model.type === 'SALE' ? 'Customer Name' : 'Vendor Name' }}</label>
            <input pInputText [(ngModel)]="model.customerName" name="customer" class="w-full" />
          </div>
          <div class="col-12">
            <label class="text-sm font-medium block mb-1">Notes</label>
            <input pInputText [(ngModel)]="model.notes" name="notes" class="w-full" />
          </div>
        </div>

        <div class="flex align-items-center justify-content-between">
          <h4 class="text-sm font-bold m-0">Line Items</h4>
          <p-button label="Add Item" icon="pi pi-plus" size="small" (onClick)="addItem()" />
        </div>

        @for (item of model.items; track $index; let i = $index) {
          <div class="grid align-items-end border-1 border-round p-2 surface-ground">
            <div class="col-4">
              <label class="text-xs block mb-1">Description</label>
              <input pInputText [(ngModel)]="item.description" name="desc_{{$index}}" class="w-full" />
            </div>
            <div class="col-2">
              <label class="text-xs block mb-1">Qty</label>
              <p-inputNumber [(ngModel)]="item.quantity" name="qty_{{$index}}" class="w-full" />
            </div>
            <div class="col-2">
              <label class="text-xs block mb-1">Unit Price</label>
              <p-inputNumber [(ngModel)]="item.unitPrice" name="price_{{$index}}" mode="currency" currency="USD" class="w-full" />
            </div>
            <div class="col-2">
              <label class="text-xs block mb-1">Tax %</label>
              <p-inputNumber [(ngModel)]="item.taxRate" name="tax_{{$index}}" class="w-full" />
            </div>
            <div class="col-1">
              <p-button icon="pi pi-trash" severity="danger" size="small" (onClick)="removeItem(i)" />
            </div>
            <div class="col-1 text-sm font-medium pt-3">
              {{ item.quantity * item.unitPrice | currency }}
            </div>
          </div>
        }

        <div class="flex gap-2 justify-content-end">
          <p-button label="Cancel" severity="secondary" (onClick)="back()" />
          <p-button type="submit" label="Create Invoice" [loading]="saving" />
        </div>
      </form>
    </div>
  `,
})
export class InvoiceFormComponent {
  saving = false;

  model: InvoiceRequest = {
    invoiceDate: new Date().toISOString().split('T')[0],
    dueDate: '',
    type: 'SALE',
    customerName: '',
    items: [{ description: '', quantity: 1, unitPrice: 0 }],
  };

  constructor(
    private router: Router,
    private api: FinanceApiService,
    private message: MessageService,
  ) {}

  addItem() {
    this.model.items.push({ description: '', quantity: 1, unitPrice: 0 });
  }

  removeItem(index: number) {
    this.model.items.splice(index, 1);
  }

  onSubmit() {
    if (!this.model.invoiceDate || !this.model.dueDate || this.model.items.length === 0) return;
    this.saving = true;
    this.api.createInvoice(this.model).subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Created', detail: 'Invoice created' });
        this.back();
      },
      error: () => {
        this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to create invoice' });
        this.saving = false;
      },
    });
  }

  back() {
    this.router.navigate(['/finance']);
  }
}

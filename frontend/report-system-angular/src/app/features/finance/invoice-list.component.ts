import { Component, OnInit } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { FinanceApiService } from './services/finance-api.service';
import { Invoice } from './models/invoice.model';

@Component({
  selector: 'app-invoice-list',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, FormsModule, TableModule, SelectModule, ButtonModule, InputTextModule, TagModule, ToastModule],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Invoices</h2>
      <div class="flex gap-2">
        <p-select
          [(ngModel)]="typeFilter"
          [options]="typeOptions"
          optionLabel="label"
          optionValue="value"
          placeholder="All Types"
          (onChange)="load()"
          class="w-8rem"
        />
        <p-button label="New Invoice" icon="pi pi-plus" severity="success" (onClick)="router.navigate(['finance', 'invoices', 'new'])" />
      </div>
    </div>

    <p-table
      [value]="invoices"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['invoiceNumber', 'customerName', 'vendorName']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input pInputText type="text" placeholder="Search invoices..." />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Invoice #</th>
          <th>Date</th>
          <th>Type</th>
          <th>Customer/Vendor</th>
          <th>Total</th>
          <th>Paid</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-inv>
        <tr>
          <td class="font-medium">{{ inv.invoiceNumber }}</td>
          <td>{{ inv.invoiceDate | date }}</td>
          <td>
            <p-tag [value]="inv.type" [severity]="inv.type === 'SALE' ? 'success' : 'info'" />
          </td>
          <td>{{ inv.customerName || inv.vendorName || '—' }}</td>
          <td class="font-mono text-sm">{{ inv.total | currency }}</td>
          <td class="font-mono text-sm">{{ inv.paidAmount | currency }}</td>
          <td>
            <p-tag
              [value]="inv.status"
              [severity]="inv.status === 'PAID' ? 'success' : inv.status === 'OVERDUE' ? 'danger' : inv.status === 'CANCELLED' ? 'warn' : 'info'"
            />
          </td>
          <td>
            <p-button label="View" size="small" severity="info" (onClick)="router.navigate(['finance', 'invoices', inv.id])" />
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="8" class="text-center text-gray-500 py-4">No invoices found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class InvoiceListComponent implements OnInit {
  invoices: Invoice[] = [];
  loading = false;
  typeFilter = '';
  typeOptions = [
    { label: 'All Types', value: '' },
    { label: 'Sale', value: 'SALE' },
    { label: 'Purchase', value: 'PURCHASE' },
  ];

  constructor(
    protected router: Router,
    private api: FinanceApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.api.getInvoices(this.typeFilter || undefined).subscribe({
      next: (res) => { this.invoices = res; this.loading = false; },
      error: () => { this.loading = false; this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load invoices' }); },
    });
  }
}

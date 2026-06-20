import { Component, OnInit } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { PaymentApiService } from './services/payment-api.service';
import { PaymentTransaction, ReconciliationRequest } from './models/payment.model';

@Component({
  selector: 'app-payment-list',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, FormsModule, TableModule, SelectModule, ButtonModule, InputTextModule, TagModule, DialogModule, ToastModule],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Payment Transactions</h2>
      <div class="flex gap-2">
        <p-select
          [(ngModel)]="statusFilter"
          [options]="statusOptions"
          optionLabel="label"
          optionValue="value"
          placeholder="All Status"
          (onChange)="load()"
          class="w-8rem"
        />
        <p-button label="Reconcile" icon="pi pi-check-circle" severity="info" (onClick)="showReconcile()" />
      </div>
    </div>

    @if (reconcileDialog) {
      <p-dialog
        [(visible)]="reconcileDialog"
        header="Reconcile Transactions"
        [modal]="true"
        [style]="{ width: '400px' }"
        (onHide)="reconcileDialog = false"
      >
        <div class="flex flex-column gap-3">
          <div>
            <label class="text-sm font-medium block mb-1">Start Date *</label>
            <input pInputText type="date" [(ngModel)]="reconForm.startDate" class="w-full" />
          </div>
          <div>
            <label class="text-sm font-medium block mb-1">End Date *</label>
            <input pInputText type="date" [(ngModel)]="reconForm.endDate" class="w-full" />
          </div>
          <div>
            <label class="text-sm font-medium block mb-1">Notes</label>
            <input pInputText [(ngModel)]="reconForm.notes" class="w-full" />
          </div>
          <p-button label="Run Reconciliation" [loading]="reconciling" (onClick)="runReconciliation()" />
        </div>
      </p-dialog>
    }

    <p-table
      [value]="transactions"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['transactionNumber', 'referenceNumber', 'description']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input pInputText type="text" placeholder="Search transactions..." />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Transaction #</th>
          <th>Date</th>
          <th>Type</th>
          <th>Method</th>
          <th>Amount</th>
          <th>Fee</th>
          <th>Net</th>
          <th>Status</th>
          <th>Reconciled</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-t>
        <tr>
          <td class="font-medium">{{ t.transactionNumber }}</td>
          <td>{{ t.transactionDate | date }}</td>
          <td><p-tag [value]="t.type" severity="info" /></td>
          <td><p-tag [value]="t.method" severity="warn" /></td>
          <td class="font-mono text-sm">{{ t.amount | currency:t.currency }}</td>
          <td class="font-mono text-sm">{{ t.feeAmount | currency }}</td>
          <td class="font-mono text-sm">{{ t.netAmount | currency }}</td>
          <td>
            <p-tag
              [value]="t.status"
              [severity]="t.status === 'COMPLETED' ? 'success' : t.status === 'FAILED' || t.status === 'CANCELLED' ? 'danger' : t.status === 'REFUNDED' ? 'warn' : 'info'"
            />
          </td>
          <td>
            <p-tag [value]="t.reconciled ? 'YES' : 'NO'" [severity]="t.reconciled ? 'success' : 'danger'" />
          </td>
          <td>
            <div class="flex gap-1">
              @if (t.status === 'COMPLETED') {
                <p-button label="Refund" size="small" severity="warn" (onClick)="refund(t)" />
              }
              @if (t.status !== 'CANCELLED' && t.status !== 'REFUNDED') {
                <p-button label="Cancel" size="small" severity="danger" (onClick)="cancel(t)" />
              }
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="10" class="text-center text-gray-500 py-4">No transactions found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class PaymentListComponent implements OnInit {
  transactions: PaymentTransaction[] = [];
  loading = false;
  statusFilter = '';
  reconcileDialog = false;
  reconciling = false;

  reconForm: ReconciliationRequest = { startDate: '', endDate: '' };

  statusOptions = [
    { label: 'All Status', value: '' },
    { label: 'Completed', value: 'COMPLETED' },
    { label: 'Pending', value: 'PENDING' },
    { label: 'Failed', value: 'FAILED' },
    { label: 'Refunded', value: 'REFUNDED' },
    { label: 'Cancelled', value: 'CANCELLED' },
  ];

  constructor(
    private api: PaymentApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.api.getTransactions(this.statusFilter || undefined).subscribe({
      next: (res) => { this.transactions = res; this.loading = false; },
      error: () => { this.loading = false; this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load transactions' }); },
    });
  }

  showReconcile() {
    this.reconForm = { startDate: '', endDate: '' };
    this.reconcileDialog = true;
  }

  runReconciliation() {
    if (!this.reconForm.startDate || !this.reconForm.endDate) return;
    this.reconciling = true;
    this.api.reconcile(this.reconForm).subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Reconciled', detail: 'Reconciliation complete' });
        this.reconcileDialog = false;
        this.load();
        this.reconciling = false;
      },
      error: () => { this.reconciling = false; },
    });
  }

  refund(t: PaymentTransaction) {
    this.api.refundTransaction(t.id).subscribe({
      next: () => { this.message.add({ severity: 'success', summary: 'Refunded' }); this.load(); },
    });
  }

  cancel(t: PaymentTransaction) {
    this.api.cancelTransaction(t.id).subscribe({
      next: () => { this.message.add({ severity: 'info', summary: 'Cancelled' }); this.load(); },
    });
  }
}

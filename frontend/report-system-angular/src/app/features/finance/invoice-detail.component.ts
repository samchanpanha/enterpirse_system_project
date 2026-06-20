import { Component, OnInit } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { FinanceApiService } from './services/finance-api.service';
import { Invoice, InvoicePaymentRequest } from './models/invoice.model';

@Component({
  selector: 'app-invoice-detail',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, FormsModule, TableModule, ButtonModule, TagModule, DialogModule, InputTextModule, InputNumberModule, ToastModule],
  template: `
    <p-toast />

    <div class="flex align-items-center gap-2 mb-4">
      <button class="p-2 border-none bg-transparent cursor-pointer text-gray-500 hover:text-gray-700"
        (click)="router.navigate(['/finance'])"><i class="pi pi-arrow-left text-xl"></i></button>
      <h2 class="text-xl font-bold m-0">Invoice {{ invoice?.invoiceNumber }}</h2>
      <p-tag *ngIf="invoice" [value]="invoice!.status" [severity]="severity(invoice!.status)" />
    </div>

    @if (invoice) {
      <div class="grid">
        <div class="col-12 md:col-6">
          <div class="card p-3 surface-card border-round shadow-1">
            <h3 class="text-sm font-bold text-gray-500 mb-2">Details</h3>
            <div class="flex flex-column gap-2 text-sm">
              <div class="flex justify-content-between"><span class="text-gray-500">Date:</span><span>{{ invoice.invoiceDate | date }}</span></div>
              <div class="flex justify-content-between"><span class="text-gray-500">Due:</span><span>{{ invoice.dueDate | date }}</span></div>
              <div class="flex justify-content-between"><span class="text-gray-500">Type:</span><span>{{ invoice.type }}</span></div>
              <div class="flex justify-content-between"><span class="text-gray-500">{{ invoice.type === 'SALE' ? 'Customer' : 'Vendor' }}:</span><span>{{ invoice.customerName || invoice.vendorName }}</span></div>
            </div>
          </div>
        </div>
        <div class="col-12 md:col-6">
          <div class="card p-3 surface-card border-round shadow-1">
            <h3 class="text-sm font-bold text-gray-500 mb-2">Summary</h3>
            <div class="flex flex-column gap-2 text-sm">
              <div class="flex justify-content-between"><span class="text-gray-500">Subtotal:</span><span>{{ invoice.subtotal | currency }}</span></div>
              <div class="flex justify-content-between"><span class="text-gray-500">Tax:</span><span>{{ invoice.taxTotal | currency }}</span></div>
              <div class="flex justify-content-between font-bold"><span>Total:</span><span>{{ invoice.total | currency }}</span></div>
              <div class="flex justify-content-between"><span class="text-gray-500">Paid:</span><span class="text-green-600">{{ invoice.paidAmount | currency }}</span></div>
              <div class="flex justify-content-between font-bold" [class.text-red-600]="invoice.total - invoice.paidAmount > 0">
                <span>Balance:</span><span>{{ invoice.total - invoice.paidAmount | currency }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="col-12">
          <div class="card p-3 surface-card border-round shadow-1">
            <div class="flex align-items-center justify-content-between mb-2">
              <h3 class="text-sm font-bold m-0">Line Items</h3>
              @if (invoice.status === 'SENT' || invoice.status === 'OVERDUE') {
                <p-button label="Record Payment" icon="pi pi-dollar" size="small" severity="success" (onClick)="showPaymentDialog()" />
              }
              @if (invoice.status !== 'PAID' && invoice.status !== 'CANCELLED') {
                <p-button label="Cancel Invoice" size="small" severity="danger" (onClick)="cancel()" />
              }
            </div>
            <p-table [value]="invoice.items" styleClass="p-datatable-striped">
              <ng-template pTemplate="header">
                <tr>
                  <th>Description</th>
                  <th>Qty</th>
                  <th>Unit Price</th>
                  <th>Tax</th>
                  <th>Total</th>
                </tr>
              </ng-template>
              <ng-template pTemplate="body" let-line>
                <tr>
                  <td>{{ line.description }}</td>
                  <td>{{ line.quantity }}</td>
                  <td>{{ line.unitPrice | currency }}</td>
                  <td>{{ line.taxAmount | currency }}</td>
                  <td class="font-medium">{{ line.total | currency }}</td>
                </tr>
              </ng-template>
            </p-table>
          </div>
        </div>
      </div>
    }

    @if (paymentDialog) {
      <p-dialog
        [(visible)]="paymentDialog"
        header="Record Payment"
        [modal]="true"
        [style]="{ width: '400px' }"
        (onHide)="paymentDialog = false"
      >
        <div class="flex flex-column gap-3">
          <div>
            <label class="text-sm font-medium block mb-1">Amount *</label>
            <p-inputNumber [(ngModel)]="payment.amount" mode="currency" currency="USD" class="w-full" />
          </div>
          <div>
            <label class="text-sm font-medium block mb-1">Payment Date *</label>
            <input pInputText type="date" [(ngModel)]="payment.paymentDate" class="w-full" />
          </div>
          <div>
            <label class="text-sm font-medium block mb-1">Notes</label>
            <input pInputText [(ngModel)]="payment.notes" class="w-full" />
          </div>
          <p-button label="Record Payment" [loading]="saving" (onClick)="recordPayment()" />
        </div>
      </p-dialog>
    }
  `,
})
export class InvoiceDetailComponent implements OnInit {
  invoice?: Invoice;
  loading = false;
  paymentDialog = false;
  saving = false;

  payment: InvoicePaymentRequest = { amount: 0, paymentDate: '' };

  constructor(
    private route: ActivatedRoute,
    protected router: Router,
    private api: FinanceApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadInvoice(id);
  }

  loadInvoice(id: string) {
    this.loading = true;
    this.api.getInvoice(id).subscribe({
      next: (res) => { this.invoice = res; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  severity(status: string) {
    switch (status) {
      case 'PAID': return 'success';
      case 'OVERDUE': return 'danger';
      case 'CANCELLED': return 'warn';
      case 'SENT': return 'info';
      default: return 'info';
    }
  }

  showPaymentDialog() {
    this.payment = { amount: this.invoice ? this.invoice.total - this.invoice.paidAmount : 0, paymentDate: new Date().toISOString().split('T')[0] };
    this.paymentDialog = true;
  }

  recordPayment() {
    if (!this.invoice || !this.payment.amount || !this.payment.paymentDate) return;
    this.saving = true;
    this.api.recordInvoicePayment(this.invoice.id, this.payment).subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Payment recorded' });
        this.paymentDialog = false;
        this.loadInvoice(this.invoice!.id);
        this.saving = false;
      },
      error: () => { this.saving = false; },
    });
  }

  cancel() {
    if (!this.invoice) return;
    this.api.cancelInvoice(this.invoice.id).subscribe({
      next: () => {
        this.message.add({ severity: 'info', summary: 'Cancelled', detail: 'Invoice cancelled' });
        this.loadInvoice(this.invoice!.id);
      },
    });
  }
}

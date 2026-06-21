import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { FinanceApiService } from './services/finance-api.service';
import { Tax, TaxRequest } from './models/tax.model';

@Component({
  selector: 'app-tax-list',
  standalone: true,
  imports: [FormsModule, TableModule, ButtonModule, InputTextModule, InputNumberModule, SelectModule, TagModule, DialogModule, ToastModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Tax Rates</h2>
      <p-button label="Add Tax" icon="pi pi-plus" severity="success" (onClick)="showForm()" />
    </div>

      <p-dialog
        [(visible)]="showDialog"
        [header]="editingTax ? 'Edit Tax' : 'Add Tax'"
        [modal]="true"
        [style]="{ width: '450px' }"
        (onHide)="showDialog = false"
      >
        <div class="flex flex-column gap-3">
          <input pInputText [(ngModel)]="form.taxName" placeholder="Tax Name *" />
          <div class="grid">
            <div class="col-6">
              <label class="text-xs block mb-1">Rate (%)</label>
              <p-inputNumber [(ngModel)]="form.taxRate" suffix="%" class="w-full" />
            </div>
            <div class="col-6">
              <label class="text-xs block mb-1">Type</label>
              <p-select
                [(ngModel)]="form.taxType"
                [options]="taxTypes"
                optionLabel="label"
                optionValue="value"
                class="w-full"
              />
            </div>
          </div>
          <input pInputText [(ngModel)]="form.description" placeholder="Description" />
          <p-button label="Save" [loading]="saving" (onClick)="save()" />
        </div>
      </p-dialog>

    <p-table
      [value]="taxes"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Rate</th>
          <th>Type</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-t>
        <tr>
          <td class="font-medium">{{ t.taxName }}</td>
          <td class="font-mono text-sm">{{ t.taxRate }}%</td>
          <td>
            <p-tag [value]="t.taxType" severity="info" />
          </td>
          <td>
            <p-tag [value]="t.active ? 'ACTIVE' : 'INACTIVE'" [severity]="t.active ? 'success' : 'danger'" />
          </td>
          <td>
            <p-button label="Edit" size="small" severity="info" (onClick)="edit(t)" />
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="5" class="text-center text-gray-500 py-4">No tax rates found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class TaxListComponent implements OnInit {
  taxes: Tax[] = [];
  loading = false;
  showDialog = false;
  editingTax?: Tax;
  saving = false;

  taxTypes = [
    { label: 'Sales', value: 'SALES' },
    { label: 'VAT', value: 'VAT' },
    { label: 'Withholding', value: 'WITHHOLDING' },
    { label: 'Other', value: 'OTHER' },
  ];

  form: TaxRequest = { taxName: '', taxRate: 0, taxType: 'SALES' };

  constructor(
    private api: FinanceApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.api.getTaxes().subscribe({
      next: (res) => { this.taxes = res; this.loading = false; },
      error: () => { this.loading = false; this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load taxes' }); },
    });
  }

  showForm() {
    this.editingTax = undefined;
    this.form = { taxName: '', taxRate: 0, taxType: 'SALES' };
    this.showDialog = true;
  }

  edit(t: Tax) {
    this.editingTax = t;
    this.form = { taxName: t.taxName, taxRate: t.taxRate, taxType: t.taxType, description: t.description };
    this.showDialog = true;
  }

  save() {
    if (!this.form.taxName) return;
    this.saving = true;
    const op = this.editingTax
      ? this.api.updateTax(this.editingTax.id, this.form)
      : this.api.createTax(this.form);
    op.subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Saved', detail: 'Tax rate saved' });
        this.showDialog = false;
        this.load();
        this.saving = false;
      },
      error: () => { this.saving = false; },
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { FinanceApiService } from './services/finance-api.service';
import { Payroll, PayrollRequest } from './models/payroll.model';
import { Employee } from './models/employee.model';

type EmployeeOption = Employee & { fullName: string };

@Component({
  selector: 'app-payroll-list',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, FormsModule, TableModule, ButtonModule, InputTextModule, InputNumberModule, SelectModule, TagModule, DialogModule, ToastModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Payroll</h2>
      <p-button label="New Payroll" icon="pi pi-plus" severity="success" (onClick)="showNewForm()" />
    </div>

      <p-dialog
        [(visible)]="showDialog"
        header="New Payroll Run"
        [modal]="true"
        [style]="{ width: '700px' }"
        (onHide)="showDialog = false"
      >
        <div class="flex flex-column gap-3">
          <div class="grid">
            <div class="col-4">
              <label class="text-xs block mb-1">Period Start</label>
              <input pInputText type="date" [(ngModel)]="form.payPeriodStart" class="w-full" />
            </div>
            <div class="col-4">
              <label class="text-xs block mb-1">Period End</label>
              <input pInputText type="date" [(ngModel)]="form.payPeriodEnd" class="w-full" />
            </div>
            <div class="col-4">
              <label class="text-xs block mb-1">Pay Date</label>
              <input pInputText type="date" [(ngModel)]="form.payDate" class="w-full" />
            </div>
          </div>

          <h4 class="text-sm font-bold m-0">Employee Entries</h4>
          @for (entry of form.entries; track $index; let i = $index) {
            <div class="grid align-items-end border-1 border-round p-2 surface-ground">
              <div class="col-5">
                <label class="text-xs block mb-1">Employee</label>
                <p-select
                  [(ngModel)]="entry.employeeId"
                  [options]="employees"
                  optionLabel="fullName"
                  optionValue="id"
                  class="w-full"
                  placeholder="Select..."
                >
                  <ng-template let-e pTemplate="item">
                    {{ e.firstName }} {{ e.lastName }} ({{ e.employeeCode }})
                  </ng-template>
                </p-select>
              </div>
              <div class="col-3">
                <label class="text-xs block mb-1">Gross Pay</label>
                <p-inputNumber [(ngModel)]="entry.grossPay" mode="currency" currency="USD" class="w-full" />
              </div>
              <div class="col-3">
                <label class="text-xs block mb-1">Deductions</label>
                <p-inputNumber [(ngModel)]="entry.deductions" mode="currency" currency="USD" class="w-full" />
              </div>
              <div class="col-1">
                <p-button icon="pi pi-trash" severity="danger" size="small" (onClick)="form.entries.splice(i, 1)" />
              </div>
            </div>
          }
          <p-button label="Add Employee" icon="pi pi-plus" size="small" (onClick)="form.entries.push({ employeeId: '', grossPay: 0, deductions: 0 })" />

          <div class="flex gap-2 justify-content-end">
            <p-button label="Cancel" severity="secondary" (onClick)="showDialog = false" />
            <p-button label="Create Payroll" [loading]="saving" (onClick)="save()" />
          </div>
        </div>
      </p-dialog>

    <p-table
      [value]="payrolls"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="header">
        <tr>
          <th>Payroll #</th>
          <th>Period</th>
          <th>Pay Date</th>
          <th>Employees</th>
          <th>Gross</th>
          <th>Net</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-p>
        <tr>
          <td class="font-medium">{{ p.payrollNumber }}</td>
          <td>{{ p.payPeriodStart | date }} - {{ p.payPeriodEnd | date }}</td>
          <td>{{ p.payDate | date }}</td>
          <td>{{ p.employeeCount }}</td>
          <td class="font-mono text-sm">{{ p.totalGross | currency }}</td>
          <td class="font-mono text-sm">{{ p.totalNet | currency }}</td>
          <td>
            <p-tag
              [value]="p.status"
              [severity]="p.status === 'PAID' ? 'success' : p.status === 'PROCESSED' ? 'info' : p.status === 'DRAFT' ? 'warn' : 'danger'"
            />
          </td>
          <td>
            <div class="flex gap-1">
              @if (p.status === 'DRAFT') {
                <p-button label="Process" size="small" severity="info" (onClick)="process(p)" />
                <p-button label="Cancel" size="small" severity="danger" (onClick)="cancelPayroll(p)" />
              }
              @if (p.status === 'PROCESSED') {
                <p-button label="Pay" size="small" severity="success" (onClick)="pay(p)" />
              }
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="8" class="text-center text-gray-500 py-4">No payroll runs found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class PayrollListComponent implements OnInit {
  payrolls: Payroll[] = [];
  employees: EmployeeOption[] = [];
  loading = false;
  showDialog = false;
  saving = false;

  form: PayrollRequest = {
    payPeriodStart: '',
    payPeriodEnd: '',
    payDate: '',
    entries: [],
  };

  constructor(
    private api: FinanceApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
    this.api.getEmployees().subscribe((res) => {
      this.employees = res.map((e) => ({ ...e, fullName: `${e.firstName} ${e.lastName}` }));
    });
  }

  load() {
    this.loading = true;
    this.api.getPayrolls().subscribe({
      next: (res) => { this.payrolls = res; this.loading = false; },
      error: () => { this.loading = false; this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load payroll' }); },
    });
  }

  showNewForm() {
    this.form = { payPeriodStart: '', payPeriodEnd: '', payDate: '', entries: [] };
    this.showDialog = true;
  }

  save() {
    this.saving = true;
    this.api.createPayroll(this.form).subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Created', detail: 'Payroll created' });
        this.showDialog = false;
        this.load();
        this.saving = false;
      },
      error: () => { this.saving = false; },
    });
  }

  process(p: Payroll) {
    this.api.processPayroll(p.id).subscribe({
      next: () => { this.message.add({ severity: 'success', summary: 'Processed' }); this.load(); },
    });
  }

  pay(p: Payroll) {
    this.api.payPayroll(p.id).subscribe({
      next: () => { this.message.add({ severity: 'success', summary: 'Paid', detail: 'Payroll disbursed' }); this.load(); },
    });
  }

  cancelPayroll(p: Payroll) {
    this.api.cancelPayroll(p.id).subscribe({
      next: () => { this.message.add({ severity: 'info', summary: 'Cancelled' }); this.load(); },
    });
  }
}

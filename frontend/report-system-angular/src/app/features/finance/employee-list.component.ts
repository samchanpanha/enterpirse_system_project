import { Component, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { FormsModule } from '@angular/forms';
import { InputNumberModule } from 'primeng/inputnumber';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { FinanceApiService } from './services/finance-api.service';
import { Employee, EmployeeRequest } from './models/employee.model';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CurrencyPipe, FormsModule, TableModule, ButtonModule, InputTextModule, InputNumberModule, TagModule, DialogModule, ToastModule],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Employees</h2>
      <p-button label="Add Employee" icon="pi pi-plus" severity="success" (onClick)="showForm()" />
    </div>

    @if (showDialog) {
      <p-dialog
        [(visible)]="showDialog"
        [header]="editingEmployee ? 'Edit Employee' : 'Add Employee'"
        [modal]="true"
        [style]="{ width: '600px' }"
        (onHide)="showDialog = false"
      >
        <div class="flex flex-column gap-3">
          <div class="grid">
            <div class="col-6">
              <label class="text-xs block mb-1">Employee Code *</label>
              <input pInputText [(ngModel)]="form.employeeCode" class="w-full" />
            </div>
            <div class="col-6">
              <label class="text-xs block mb-1">Email *</label>
              <input pInputText [(ngModel)]="form.email" class="w-full" />
            </div>
            <div class="col-6">
              <label class="text-xs block mb-1">First Name *</label>
              <input pInputText [(ngModel)]="form.firstName" class="w-full" />
            </div>
            <div class="col-6">
              <label class="text-xs block mb-1">Last Name *</label>
              <input pInputText [(ngModel)]="form.lastName" class="w-full" />
            </div>
            <div class="col-6">
              <label class="text-xs block mb-1">Position *</label>
              <input pInputText [(ngModel)]="form.position" class="w-full" />
            </div>
            <div class="col-6">
              <label class="text-xs block mb-1">Department *</label>
              <input pInputText [(ngModel)]="form.department" class="w-full" />
            </div>
            <div class="col-6">
              <label class="text-xs block mb-1">Phone</label>
              <input pInputText [(ngModel)]="form.phone" class="w-full" />
            </div>
            <div class="col-6">
              <label class="text-xs block mb-1">Hire Date *</label>
              <input pInputText type="date" [(ngModel)]="form.hireDate" class="w-full" />
            </div>
            <div class="col-6">
              <label class="text-xs block mb-1">Salary *</label>
              <p-inputNumber [(ngModel)]="form.salary" mode="currency" currency="USD" class="w-full" />
            </div>
          </div>
          <p-button label="Save" [loading]="saving" (onClick)="save()" />
        </div>
      </p-dialog>
    }

    <p-table
      [value]="employees"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['employeeCode', 'firstName', 'lastName', 'email', 'department']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input pInputText type="text" placeholder="Search employees..." />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Code</th>
          <th>Name</th>
          <th>Email</th>
          <th>Position</th>
          <th>Department</th>
          <th>Salary</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-e>
        <tr>
          <td class="font-mono text-sm">{{ e.employeeCode }}</td>
          <td class="font-medium">{{ e.firstName }} {{ e.lastName }}</td>
          <td>{{ e.email }}</td>
          <td>{{ e.position }}</td>
          <td>{{ e.department }}</td>
          <td class="font-mono text-sm">{{ e.salary | currency }}</td>
          <td>
            <p-tag [value]="e.status" [severity]="e.status === 'ACTIVE' ? 'success' : e.status === 'ON_LEAVE' ? 'warn' : 'danger'" />
          </td>
          <td>
            <p-button label="Edit" size="small" severity="info" (onClick)="edit(e)" />
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="8" class="text-center text-gray-500 py-4">No employees found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class EmployeeListComponent implements OnInit {
  employees: Employee[] = [];
  loading = false;
  showDialog = false;
  editingEmployee?: Employee;
  saving = false;

  form: EmployeeRequest = {
    employeeCode: '', firstName: '', lastName: '', email: '',
    position: '', department: '', hireDate: '', salary: 0,
  };

  constructor(
    private api: FinanceApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.api.getEmployees().subscribe({
      next: (res) => { this.employees = res; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  showForm() {
    this.editingEmployee = undefined;
    this.form = { employeeCode: '', firstName: '', lastName: '', email: '', position: '', department: '', hireDate: '', salary: 0 };
    this.showDialog = true;
  }

  edit(e: Employee) {
    this.editingEmployee = e;
    this.form = {
      employeeCode: e.employeeCode, firstName: e.firstName, lastName: e.lastName,
      email: e.email, phone: e.phone, position: e.position, department: e.department,
      hireDate: e.hireDate, salary: e.salary,
    };
    this.showDialog = true;
  }

  save() {
    if (!this.form.employeeCode || !this.form.firstName || !this.form.lastName || !this.form.email) return;
    this.saving = true;
    const op = this.editingEmployee
      ? this.api.updateEmployee(this.editingEmployee.id, this.form)
      : this.api.createEmployee(this.form);
    op.subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Saved', detail: 'Employee saved' });
        this.showDialog = false;
        this.load();
        this.saving = false;
      },
      error: () => { this.saving = false; },
    });
  }
}

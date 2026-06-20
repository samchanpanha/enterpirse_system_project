import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { InventoryApiService } from './services/inventory-api.service';
import { Supplier, SupplierRequest } from './models/supplier.model';

@Component({
  selector: 'app-supplier-list',
  standalone: true,
  imports: [FormsModule, TableModule, ButtonModule, InputTextModule, TagModule, ToastModule, DialogModule, ConfirmDialogModule],
  providers: [ConfirmationService],
  template: `
    <p-toast />
    <p-confirmdialog />

    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Suppliers</h2>
      <p-button label="Add Supplier" icon="pi pi-plus" severity="success" (onClick)="showForm()" />
    </div>

    @if (showDialog) {
      <p-dialog
        [(visible)]="showDialog"
        [header]="editingSupplier ? 'Edit Supplier' : 'Add Supplier'"
        [modal]="true"
        [style]="{ width: '500px' }"
        (onHide)="showDialog = false"
      >
        <div class="flex flex-column gap-3">
          <input pInputText [(ngModel)]="form.name" placeholder="Name *" />
          <input pInputText [(ngModel)]="form.phone" placeholder="Phone *" />
          <input pInputText [(ngModel)]="form.contactPerson" placeholder="Contact person" />
          <input pInputText [(ngModel)]="form.email" placeholder="Email" />
          <input pInputText [(ngModel)]="form.address" placeholder="Address" />
          <p-button label="Save" [loading]="saving" (onClick)="save()" />
        </div>
      </p-dialog>
    }

    <p-table
      [value]="suppliers"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['name', 'phone', 'email']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input pInputText type="text" placeholder="Search suppliers..." />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Contact</th>
          <th>Phone</th>
          <th>Email</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-s>
        <tr>
          <td class="font-medium">{{ s.name }}</td>
          <td>{{ s.contactPerson || '—' }}</td>
          <td>{{ s.phone }}</td>
          <td>{{ s.email || '—' }}</td>
          <td>
            <p-tag [value]="s.active ? 'ACTIVE' : 'INACTIVE'" [severity]="s.active ? 'success' : 'danger'" />
          </td>
          <td>
            <div class="flex gap-1">
              <p-button icon="pi pi-pencil" severity="info" size="small" (onClick)="edit(s)" />
              <p-button icon="pi pi-trash" severity="danger" size="small" (onClick)="delete(s)" />
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="6" class="text-center text-gray-500 py-4">No suppliers found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class SupplierListComponent implements OnInit {
  suppliers: Supplier[] = [];
  loading = false;
  showDialog = false;
  editingSupplier?: Supplier;
  saving = false;

  form: SupplierRequest = { name: '', phone: '' };

  constructor(
    private api: InventoryApiService,
    private message: MessageService,
    private confirmation: ConfirmationService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.api.getSuppliers().subscribe({
      next: (res) => { this.suppliers = res; this.loading = false; },
      error: () => { this.loading = false; this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load suppliers' }); },
    });
  }

  showForm() {
    this.editingSupplier = undefined;
    this.form = { name: '', phone: '' };
    this.showDialog = true;
  }

  edit(s: Supplier) {
    this.editingSupplier = s;
    this.form = { name: s.name, phone: s.phone, contactPerson: s.contactPerson, email: s.email, address: s.address };
    this.showDialog = true;
  }

  save() {
    if (!this.form.name || !this.form.phone) return;
    this.saving = true;
    const op = this.editingSupplier
      ? this.api.updateSupplier(this.editingSupplier.id, this.form)
      : this.api.createSupplier(this.form);
    op.subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Saved', detail: 'Supplier saved' });
        this.showDialog = false;
        this.load();
        this.saving = false;
      },
      error: () => { this.saving = false; this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to save' }); },
    });
  }

  delete(s: Supplier) {
    this.confirmation.confirm({
      header: 'Delete Supplier',
      message: `Delete "${s.name}"?`,
      accept: () => {
        this.api.deleteSupplier(s.id).subscribe({
          next: () => { this.message.add({ severity: 'success', summary: 'Deleted', detail: 'Supplier deleted' }); this.load(); },
        });
      },
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MessageService } from 'primeng/api';

interface Tenant {
  id: string;
  name: string;
  code: string;
  email: string;
  phone: string;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  userCount: number;
}

@Component({
  selector: 'app-tenant-list',
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    TagModule,
    ConfirmDialogModule,
    ToastModule,
  ],
  providers: [ConfirmationService, MessageService],
  template: `
    <p-toast />
    <p-confirmdialog />

    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Tenant Management</h2>
      <p-button
        label="Add Tenant"
        icon="pi pi-plus"
        severity="success"
        (onClick)="add()"
      />
    </div>

    <p-table
      [value]="filteredTenants"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['name', 'code', 'email', 'phone']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input
            pInputText
            type="text"
            placeholder="Search tenants..."
            (input)="onSearch($event)"
          />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Code</th>
          <th>Email</th>
          <th>Phone</th>
          <th>Status</th>
          <th>Users</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-t>
        <tr>
          <td class="font-medium">{{ t.name }}</td>
          <td><code>{{ t.code }}</code></td>
          <td>{{ t.email }}</td>
          <td>{{ t.phone }}</td>
          <td>
            <p-tag
              [value]="t.status"
              [severity]="t.status === 'ACTIVE' ? 'success' : t.status === 'INACTIVE' ? 'warn' : 'danger'"
            />
          </td>
          <td>{{ t.userCount }}</td>
          <td>
            <div class="flex gap-1">
              <p-button
                icon="pi pi-pencil"
                severity="info"
                size="small"
                (onClick)="edit(t)"
              />
              <p-button
                icon="pi pi-trash"
                severity="danger"
                size="small"
                (onClick)="delete(t)"
              />
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr>
          <td colspan="7" class="text-center text-gray-500 py-4">
            No tenants found.
          </td>
        </tr>
      </ng-template>
    </p-table>
  `,
})
export class TenantListComponent implements OnInit {
  tenants: Tenant[] = [];
  filteredTenants: Tenant[] = [];
  loading = false;
  searchQuery = '';

  private readonly mockTenants: Tenant[] = [
    { id: '1', name: 'Demo Corp', code: 'demo-corp', email: 'admin@demo.com', phone: '+855 23 888 100', status: 'ACTIVE', userCount: 12 },
    { id: '2', name: 'ACME Properties', code: 'acme-prop', email: 'info@acme.com', phone: '+855 23 888 101', status: 'ACTIVE', userCount: 5 },
    { id: '3', name: 'Mekong Food Chain', code: 'mekong-fc', email: 'contact@mekongfc.com', phone: '+855 23 888 102', status: 'ACTIVE', userCount: 8 },
    { id: '4', name: 'Royal Hotel Group', code: 'royal-hotel', email: 'info@royalhotel.com', phone: '+855 23 888 103', status: 'INACTIVE', userCount: 3 },
    { id: '5', name: 'RetailPlus Cambodia', code: 'retailplus', email: 'hello@retailplus.com', phone: '+855 23 888 104', status: 'ACTIVE', userCount: 7 },
  ];

  constructor(
    private confirmation: ConfirmationService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    setTimeout(() => {
      this.tenants = [...this.mockTenants];
      this.applyFilter();
      this.loading = false;
    }, 300);
  }

  onSearch(event: Event) {
    const input = event.target as HTMLInputElement;
    this.searchQuery = input.value.toLowerCase();
    this.applyFilter();
  }

  private applyFilter() {
    if (!this.searchQuery) {
      this.filteredTenants = [...this.tenants];
    } else {
      this.filteredTenants = this.tenants.filter(
        (t) =>
          t.name.toLowerCase().includes(this.searchQuery) ||
          t.code.toLowerCase().includes(this.searchQuery) ||
          t.email.toLowerCase().includes(this.searchQuery) ||
          t.phone.toLowerCase().includes(this.searchQuery),
      );
    }
  }

  add() {
    this.message.add({
      severity: 'info',
      summary: 'Coming Soon',
      detail: 'Add tenant form will be implemented in a future phase.',
    });
  }

  edit(t: Tenant) {
    this.message.add({
      severity: 'info',
      summary: 'Edit Tenant',
      detail: `Edit "${t.name}" — coming soon.`,
    });
  }

  delete(t: Tenant) {
    this.confirmation.confirm({
      header: 'Delete Tenant',
      message: `Are you sure you want to delete "${t.name}"?`,
      accept: () => {
        this.tenants = this.tenants.filter((x) => x.id !== t.id);
        this.applyFilter();
        this.message.add({
          severity: 'success',
          summary: 'Deleted',
          detail: `${t.name} deleted.`,
        });
      },
    });
  }
}

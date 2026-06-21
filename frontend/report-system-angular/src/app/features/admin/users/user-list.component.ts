import { Component, OnInit } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MessageService } from 'primeng/api';

interface AdminUser {
  id: string;
  name: string;
  email: string;
  phone: string;
  roles: string[];
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
}

@Component({
  selector: 'app-user-list',
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
      <h2 class="text-xl font-bold m-0">User Management</h2>
      <p-button
        label="Add User"
        icon="pi pi-plus"
        severity="success"
        (onClick)="add()"
      />
    </div>

    <p-table
      [value]="filteredUsers"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['name', 'email', 'phone']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input
            pInputText
            type="text"
            placeholder="Search users..."
            (input)="onSearch($event)"
          />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Phone</th>
          <th>Roles</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-u>
        <tr>
          <td class="font-medium">{{ u.name }}</td>
          <td>{{ u.email }}</td>
          <td>{{ u.phone }}</td>
          <td>
            @for (role of u.roles; track role; let last = $last) {
              <p-tag [value]="role" severity="info" styleClass="mr-1" />
            }
          </td>
          <td>
            <p-tag
              [value]="u.status"
              [severity]="u.status === 'ACTIVE' ? 'success' : u.status === 'INACTIVE' ? 'warn' : 'danger'"
            />
          </td>
          <td>
            <div class="flex gap-1">
              <p-button
                icon="pi pi-pencil"
                severity="info"
                size="small"
                (onClick)="edit(u)"
              />
              <p-button
                icon="pi pi-trash"
                severity="danger"
                size="small"
                (onClick)="delete(u)"
              />
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr>
          <td colspan="6" class="text-center text-gray-500 py-4">
            No users found.
          </td>
        </tr>
      </ng-template>
    </p-table>
  `,
})
export class UserListComponent implements OnInit {
  users: AdminUser[] = [];
  filteredUsers: AdminUser[] = [];
  loading = false;
  searchQuery = '';

  private readonly mockUsers: AdminUser[] = [
    { id: '1', name: 'Admin User', email: 'admin@demo.com', phone: '+855 12 345 678', roles: ['ADMIN'], status: 'ACTIVE' },
    { id: '2', name: 'Sophea Kim', email: 'sophea@demo.com', phone: '+855 12 345 679', roles: ['MANAGER'], status: 'ACTIVE' },
    { id: '3', name: 'Vannak Chen', email: 'vannak@demo.com', phone: '+855 12 345 680', roles: ['STAFF'], status: 'ACTIVE' },
    { id: '4', name: 'Borey Sun', email: 'borey@demo.com', phone: '+855 12 345 681', roles: ['STAFF', 'ACCOUNTANT'], status: 'ACTIVE' },
    { id: '5', name: 'Dara Lim', email: 'dara@demo.com', phone: '+855 12 345 682', roles: ['MANAGER'], status: 'INACTIVE' },
    { id: '6', name: 'Rithy Phan', email: 'rithy@demo.com', phone: '+855 12 345 683', roles: ['STAFF'], status: 'ACTIVE' },
    { id: '7', name: 'Sreyneath Mao', email: 'sreyneath@demo.com', phone: '+855 12 345 684', roles: ['ACCOUNTANT'], status: 'SUSPENDED' },
    { id: '8', name: 'Visal Heng', email: 'visal@demo.com', phone: '+855 12 345 685', roles: ['STAFF'], status: 'ACTIVE' },
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
      this.users = [...this.mockUsers];
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
      this.filteredUsers = [...this.users];
    } else {
      this.filteredUsers = this.users.filter(
        (u) =>
          u.name.toLowerCase().includes(this.searchQuery) ||
          u.email.toLowerCase().includes(this.searchQuery) ||
          u.phone.toLowerCase().includes(this.searchQuery),
      );
    }
  }

  add() {
    this.message.add({
      severity: 'info',
      summary: 'Coming Soon',
      detail: 'Add user form will be implemented in a future phase.',
    });
  }

  edit(u: AdminUser) {
    this.message.add({
      severity: 'info',
      summary: 'Edit User',
      detail: `Edit "${u.name}" — coming soon.`,
    });
  }

  delete(u: AdminUser) {
    this.confirmation.confirm({
      header: 'Delete User',
      message: `Are you sure you want to delete "${u.name}"?`,
      accept: () => {
        this.users = this.users.filter((x) => x.id !== u.id);
        this.applyFilter();
        this.message.add({
          severity: 'success',
          summary: 'Deleted',
          detail: `${u.name} deleted.`,
        });
      },
    });
  }
}

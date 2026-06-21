import { Component, OnInit } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmationService, MessageService } from 'primeng/api';

interface Branch {
  id: string;
  name: string;
  code: string;
  type: string;
  city: string;
  phone: string;
  status: 'ACTIVE' | 'INACTIVE';
}

@Component({
  selector: 'app-branch-list',
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
      <h2 class="text-xl font-bold m-0">Branch Management</h2>
      <p-button
        label="Add Branch"
        icon="pi pi-plus"
        severity="success"
        (onClick)="add()"
      />
    </div>

    <p-table
      [value]="filteredBranches"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['name', 'code', 'type', 'city', 'phone']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input
            pInputText
            type="text"
            placeholder="Search branches..."
            (input)="onSearch($event)"
          />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Code</th>
          <th>Type</th>
          <th>City</th>
          <th>Phone</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-b>
        <tr>
          <td class="font-medium">{{ b.name }}</td>
          <td><code>{{ b.code }}</code></td>
          <td>{{ b.type }}</td>
          <td>{{ b.city }}</td>
          <td>{{ b.phone }}</td>
          <td>
            <p-tag
              [value]="b.status"
              [severity]="b.status === 'ACTIVE' ? 'success' : 'danger'"
            />
          </td>
          <td>
            <div class="flex gap-1">
              <p-button
                icon="pi pi-pencil"
                severity="info"
                size="small"
                (onClick)="edit(b)"
              />
              <p-button
                icon="pi pi-trash"
                severity="danger"
                size="small"
                (onClick)="delete(b)"
              />
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr>
          <td colspan="7" class="text-center text-gray-500 py-4">
            No branches found.
          </td>
        </tr>
      </ng-template>
    </p-table>
  `,
})
export class BranchListComponent implements OnInit {
  branches: Branch[] = [];
  filteredBranches: Branch[] = [];
  loading = false;
  searchQuery = '';

  private readonly mockBranches: Branch[] = [
    { id: '1', name: 'Headquarters', code: 'HQ-001', type: 'HEAD_OFFICE', city: 'Phnom Penh', phone: '+855 23 888 100', status: 'ACTIVE' },
    { id: '2', name: 'BKK1 Branch', code: 'BKK-001', type: 'BRANCH', city: 'Phnom Penh', phone: '+855 23 888 110', status: 'ACTIVE' },
    { id: '3', name: 'Siem Reap Central', code: 'SR-001', type: 'BRANCH', city: 'Siem Reap', phone: '+855 63 888 200', status: 'ACTIVE' },
    { id: '4', name: 'Sihanoukville Port', code: 'SHV-001', type: 'WAREHOUSE', city: 'Sihanoukville', phone: '+855 34 888 300', status: 'ACTIVE' },
    { id: '5', name: 'Battambang Branch', code: 'BTB-001', type: 'BRANCH', city: 'Battambang', phone: '+855 53 888 400', status: 'INACTIVE' },
    { id: '6', name: 'Central Storage', code: 'STO-001', type: 'WAREHOUSE', city: 'Phnom Penh', phone: '+855 23 888 120', status: 'ACTIVE' },
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
      this.branches = [...this.mockBranches];
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
      this.filteredBranches = [...this.branches];
    } else {
      this.filteredBranches = this.branches.filter(
        (b) =>
          b.name.toLowerCase().includes(this.searchQuery) ||
          b.code.toLowerCase().includes(this.searchQuery) ||
          b.type.toLowerCase().includes(this.searchQuery) ||
          b.city.toLowerCase().includes(this.searchQuery) ||
          b.phone.toLowerCase().includes(this.searchQuery),
      );
    }
  }

  add() {
    this.message.add({
      severity: 'info',
      summary: 'Coming Soon',
      detail: 'Add branch form will be implemented in a future phase.',
    });
  }

  edit(b: Branch) {
    this.message.add({
      severity: 'info',
      summary: 'Edit Branch',
      detail: `Edit "${b.name}" — coming soon.`,
    });
  }

  delete(b: Branch) {
    this.confirmation.confirm({
      header: 'Delete Branch',
      message: `Are you sure you want to delete "${b.name}"?`,
      accept: () => {
        this.branches = this.branches.filter((x) => x.id !== b.id);
        this.applyFilter();
        this.message.add({
          severity: 'success',
          summary: 'Deleted',
          detail: `${b.name} deleted.`,
        });
      },
    });
  }
}

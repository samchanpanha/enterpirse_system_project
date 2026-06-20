import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { FinanceApiService } from './services/finance-api.service';
import { Account } from './models/account.model';

@Component({
  selector: 'app-chart-of-accounts-list',
  standalone: true,
  imports: [TableModule, ButtonModule, InputTextModule, TagModule, ToastModule],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Chart of Accounts</h2>
      <p-button label="Add Account" icon="pi pi-plus" severity="success" (onClick)="router.navigate(['finance', 'accounts', 'new'])" />
    </div>

    <p-table
      [value]="accounts"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['accountCode', 'accountName', 'accountType']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input pInputText type="text" placeholder="Search accounts..." />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Code</th>
          <th>Name</th>
          <th>Type</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-a>
        <tr>
          <td class="font-mono text-sm">{{ a.accountCode }}</td>
          <td class="font-medium">{{ a.accountName }}</td>
          <td>
            <p-tag
              [value]="a.accountType"
              [severity]="a.accountType === 'ASSET' ? 'info' : a.accountType === 'LIABILITY' ? 'warn' : a.accountType === 'EQUITY' ? 'success' : a.accountType === 'REVENUE' ? 'primary' : 'danger'"
            />
          </td>
          <td>
            <p-tag [value]="a.active ? 'ACTIVE' : 'INACTIVE'" [severity]="a.active ? 'success' : 'danger'" />
          </td>
          <td>
            <p-button label="Edit" size="small" severity="info" (onClick)="router.navigate(['finance', 'accounts', a.id, 'edit'])" />
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="5" class="text-center text-gray-500 py-4">No accounts found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class ChartOfAccountsListComponent implements OnInit {
  accounts: Account[] = [];
  loading = false;

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
    this.api.getAccounts().subscribe({
      next: (res) => { this.accounts = res; this.loading = false; },
      error: () => { this.loading = false; this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load accounts' }); },
    });
  }
}

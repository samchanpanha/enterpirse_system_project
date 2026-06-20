import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { FinanceApiService } from './services/finance-api.service';
import { AccountRequest } from './models/account.model';

@Component({
  selector: 'app-account-form',
  standalone: true,
  imports: [FormsModule, InputTextModule, SelectModule, ButtonModule, ToastModule],
  template: `
    <p-toast />
    <div class="max-w-2xl mx-auto">
      <div class="flex align-items-center gap-2 mb-4">
        <button class="p-2 border-none bg-transparent cursor-pointer text-gray-500 hover:text-gray-700"
          (click)="back()"><i class="pi pi-arrow-left text-xl"></i></button>
        <h2 class="text-xl font-bold m-0">{{ isEdit ? 'Edit Account' : 'Add Account' }}</h2>
      </div>

      <form #f="ngForm" (ngSubmit)="onSubmit()" class="flex flex-column gap-3">
        <div class="grid">
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">Account Code *</label>
            <input pInputText [(ngModel)]="model.accountCode" name="code" required class="w-full" />
          </div>
          <div class="col-12 md:col-6">
            <label class="text-sm font-medium block mb-1">Account Name *</label>
            <input pInputText [(ngModel)]="model.accountName" name="name" required class="w-full" />
          </div>
          <div class="col-12">
            <label class="text-sm font-medium block mb-1">Account Type *</label>
            <p-select
              [(ngModel)]="model.accountType"
              name="type"
              [options]="accountTypes"
              optionLabel="label"
              optionValue="value"
              class="w-full"
              placeholder="Select type"
            />
          </div>
          <div class="col-12">
            <label class="text-sm font-medium block mb-1">Description</label>
            <input pInputText [(ngModel)]="model.description" name="desc" class="w-full" />
          </div>
        </div>

        <div class="flex gap-2 justify-content-end">
          <p-button label="Cancel" severity="secondary" (onClick)="back()" />
          <p-button type="submit" label="Save" [loading]="saving" />
        </div>
      </form>
    </div>
  `,
})
export class AccountFormComponent implements OnInit {
  isEdit = false;
  accountId?: string;
  saving = false;

  accountTypes = [
    { label: 'Asset', value: 'ASSET' },
    { label: 'Liability', value: 'LIABILITY' },
    { label: 'Equity', value: 'EQUITY' },
    { label: 'Revenue', value: 'REVENUE' },
    { label: 'Expense', value: 'EXPENSE' },
  ];

  model: AccountRequest = { accountCode: '', accountName: '', accountType: 'EXPENSE' };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: FinanceApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.accountId = id;
      this.api.getAccount(id).subscribe({
        next: (a) => {
          this.model = {
            accountCode: a.accountCode,
            accountName: a.accountName,
            accountType: a.accountType,
            description: a.description,
          };
        },
      });
    }
  }

  onSubmit() {
    if (!this.model.accountCode || !this.model.accountName) return;
    this.saving = true;
    const op = this.isEdit
      ? this.api.updateAccount(this.accountId!, this.model)
      : this.api.createAccount(this.model);
    op.subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Saved', detail: `Account ${this.isEdit ? 'updated' : 'created'}` });
        this.back();
      },
      error: () => {
        this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to save account' });
        this.saving = false;
      },
    });
  }

  back() {
    this.router.navigate(['/finance']);
  }
}

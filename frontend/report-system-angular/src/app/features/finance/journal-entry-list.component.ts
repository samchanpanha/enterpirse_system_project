import { Component, OnInit } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { MessageService } from 'primeng/api';
import { FinanceApiService } from './services/finance-api.service';
import { JournalEntry, JournalEntryRequest } from './models/journal-entry.model';
import { Account } from './models/account.model';

@Component({
  selector: 'app-journal-entry-list',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, FormsModule, TableModule, SelectModule, ButtonModule, TagModule, DialogModule, ToastModule, InputTextModule, InputNumberModule],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Journal Entries</h2>
      <p-button label="New Entry" icon="pi pi-plus" severity="success" (onClick)="showNewForm()" />
    </div>

    @if (showDialog) {
      <p-dialog
        [(visible)]="showDialog"
        header="New Journal Entry"
        [modal]="true"
        [style]="{ width: '650px' }"
        (onHide)="showDialog = false"
      >
        <div class="flex flex-column gap-3">
          <div class="grid">
            <div class="col-6">
              <label class="text-sm font-medium block mb-1">Date *</label>
              <input pInputText type="date" [(ngModel)]="entryForm.entryDate" class="w-full" />
            </div>
            <div class="col-12">
              <label class="text-sm font-medium block mb-1">Description *</label>
              <input pInputText [(ngModel)]="entryForm.description" class="w-full" />
            </div>
          </div>

          <div class="flex align-items-center justify-content-between">
            <h4 class="text-sm font-bold m-0">Lines</h4>
            <p-button label="Add Line" icon="pi pi-plus" size="small" (onClick)="addLine()" />
          </div>

          @for (line of entryForm.lines; track $index; let i = $index) {
            <div class="grid align-items-end border-1 border-round p-2 surface-ground">
              <div class="col-5">
                <label class="text-xs block mb-1">Account</label>
                <p-select
                  [(ngModel)]="line.accountId"
                  [options]="accounts"
                  optionLabel="accountName"
                  optionValue="id"
                  class="w-full"
                  placeholder="Account"
                />
              </div>
              <div class="col-3">
                <label class="text-xs block mb-1">Debit</label>
                <p-inputNumber [(ngModel)]="line.debit" mode="currency" currency="USD" class="w-full" />
              </div>
              <div class="col-3">
                <label class="text-xs block mb-1">Credit</label>
                <p-inputNumber [(ngModel)]="line.credit" mode="currency" currency="USD" class="w-full" />
              </div>
              <div class="col-1">
                <p-button icon="pi pi-trash" severity="danger" size="small" (onClick)="removeLine(i)" />
              </div>
            </div>
          }

          <div class="flex justify-content-between text-sm font-bold">
            <span>Total Debit: {{ totalDebit | currency }}</span>
            <span>Total Credit: {{ totalCredit | currency }}</span>
            <span [class.text-green-600]="totalDebit === totalCredit" [class.text-red-600]="totalDebit !== totalCredit">
              {{ totalDebit === totalCredit ? 'Balanced' : 'Unbalanced' }}
            </span>
          </div>

          <div class="flex gap-2 justify-content-end">
            <p-button label="Cancel" severity="secondary" (onClick)="showDialog = false" />
            <p-button label="Save as Draft" severity="info" [loading]="saving" (onClick)="saveEntry()" />
            <p-button label="Post" severity="success" [loading]="saving" (onClick)="saveAndPost()" />
          </div>
        </div>
      </p-dialog>
    }

    <p-table
      [value]="entries"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="header">
        <tr>
          <th>Entry #</th>
          <th>Date</th>
          <th>Description</th>
          <th>Debit</th>
          <th>Credit</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-e>
        <tr>
          <td class="font-medium">{{ e.entryNumber }}</td>
          <td>{{ e.entryDate | date }}</td>
          <td>{{ e.description }}</td>
          <td class="font-mono text-sm">{{ e.totalDebit | currency }}</td>
          <td class="font-mono text-sm">{{ e.totalCredit | currency }}</td>
          <td>
            <p-tag
              [value]="e.status"
              [severity]="e.status === 'POSTED' ? 'success' : e.status === 'DRAFT' ? 'info' : 'danger'"
            />
          </td>
          <td>
            <div class="flex gap-1">
              @if (e.status === 'DRAFT') {
                <p-button label="Post" size="small" severity="success" (onClick)="postEntry(e)" />
                <p-button label="Reverse" size="small" severity="danger" (onClick)="reverseEntry(e)" />
              }
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="7" class="text-center text-gray-500 py-4">No journal entries found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class JournalEntryListComponent implements OnInit {
  entries: JournalEntry[] = [];
  accounts: Account[] = [];
  loading = false;
  showDialog = false;
  saving = false;

  entryForm: JournalEntryRequest = { entryDate: '', description: '', lines: [] };

  constructor(
    private api: FinanceApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
    this.api.getAccounts().subscribe((res) => (this.accounts = res));
  }

  get totalDebit() {
    return this.entryForm.lines.reduce((s, l) => s + (l.debit || 0), 0);
  }

  get totalCredit() {
    return this.entryForm.lines.reduce((s, l) => s + (l.credit || 0), 0);
  }

  load() {
    this.loading = true;
    this.api.getJournalEntries().subscribe({
      next: (res) => { this.entries = res; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  showNewForm() {
    this.entryForm = { entryDate: new Date().toISOString().split('T')[0], description: '', lines: [] };
    this.showDialog = true;
  }

  addLine() {
    this.entryForm.lines.push({ accountId: '', debit: 0, credit: 0 });
  }

  removeLine(index: number) {
    this.entryForm.lines.splice(index, 1);
  }

  saveEntry() {
    this.saving = true;
    this.api.createJournalEntry(this.entryForm).subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Saved', detail: 'Journal entry saved as draft' });
        this.showDialog = false;
        this.load();
        this.saving = false;
      },
      error: () => { this.saving = false; },
    });
  }

  saveAndPost() {
    this.saving = true;
    this.api.createJournalEntry(this.entryForm).subscribe({
      next: (entry) => {
        this.api.postJournalEntry(entry.id).subscribe({
          next: () => {
            this.message.add({ severity: 'success', summary: 'Posted', detail: 'Journal entry posted' });
            this.showDialog = false;
            this.load();
            this.saving = false;
          },
          error: () => { this.saving = false; },
        });
      },
      error: () => { this.saving = false; },
    });
  }

  postEntry(e: JournalEntry) {
    this.api.postJournalEntry(e.id).subscribe({
      next: () => { this.message.add({ severity: 'success', summary: 'Posted', detail: 'Entry posted' }); this.load(); },
    });
  }

  reverseEntry(e: JournalEntry) {
    this.api.reverseJournalEntry(e.id).subscribe({
      next: () => { this.message.add({ severity: 'info', summary: 'Reversed', detail: 'Entry reversed' }); this.load(); },
    });
  }
}

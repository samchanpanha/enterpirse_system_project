import { Component, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { RealtyApiService } from './services/realty-api.service';
import { Lead, LeadRequest } from './models/realty.model';

@Component({
  selector: 'app-lead-list',
  standalone: true,
  imports: [DecimalPipe, TableModule, ButtonModule, TagModule, DialogModule, InputTextModule, InputNumberModule, FormsModule, ToastModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Leads</h2>
      <p-button label="Add Lead" icon="pi pi-plus" severity="success" (onClick)="openDialog()" />
    </div>

    <p-table [value]="leads" [paginator]="true" [rows]="15" [loading]="loading" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Phone</th>
          <th>Source</th>
          <th>Budget</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-l>
        <tr>
          <td class="font-medium">{{ l.firstName }} {{ l.lastName }}</td>
          <td>{{ l.email }}</td>
          <td>{{ l.phone }}</td>
          <td>{{ l.source }}</td>
          <td>{{ l.budget | number }}</td>
          <td><p-tag [value]="l.status" [severity]="statusSeverity(l.status)" /></td>
          <td><p-button icon="pi pi-pencil" severity="info" size="small" (onClick)="edit(l)" /></td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="7" class="text-center text-gray-500 py-4">No leads found.</td></tr>
      </ng-template>
    </p-table>

    <p-dialog [(visible)]="showDialog" [header]="editMode ? 'Edit Lead' : 'New Lead'" [modal]="true" [style]="{ width: '500px' }">
      <div class="flex flex-column gap-3">
        <div class="grid">
          <div class="col-6"><label class="text-sm font-medium block mb-1">First Name *</label><input pInputText [(ngModel)]="form.firstName" class="w-full" /></div>
          <div class="col-6"><label class="text-sm font-medium block mb-1">Last Name *</label><input pInputText [(ngModel)]="form.lastName" class="w-full" /></div>
        </div>
        <div><label class="text-sm font-medium block mb-1">Email *</label><input pInputText [(ngModel)]="form.email" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Phone *</label><input pInputText [(ngModel)]="form.phone" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Source</label><input pInputText [(ngModel)]="form.source" class="w-full" placeholder="e.g. Website, Referral, Walk-in" /></div>
        <div><label class="text-sm font-medium block mb-1">Budget</label><p-inputNumber [(ngModel)]="form.budget" mode="currency" currency="USD" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Notes</label><input pInputText [(ngModel)]="form.notes" class="w-full" /></div>
        <p-button [label]="editMode ? 'Update' : 'Create'" [loading]="saving" (onClick)="save()" />
      </div>
    </p-dialog>
  `,
})
export class LeadListComponent implements OnInit {
  leads: Lead[] = [];
  loading = false;
  saving = false;
  showDialog = false;
  editMode = false;
  editId = '';
  form: LeadRequest = { firstName: '', lastName: '', email: '', phone: '', source: '' };

  constructor(private api: RealtyApiService, private message: MessageService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.api.getLeads().subscribe({ next: (res) => { this.leads = res; this.loading = false; }, error: () => { this.loading = false; } });
  }

  openDialog() { this.editMode = false; this.form = { firstName: '', lastName: '', email: '', phone: '', source: '' }; this.showDialog = true; }

  edit(l: Lead) {
    this.editMode = true; this.editId = l.id;
    this.form = { firstName: l.firstName, lastName: l.lastName, email: l.email, phone: l.phone, source: l.source, budget: l.budget, notes: l.notes };
    this.showDialog = true;
  }

  save() {
    if (!this.form.firstName || !this.form.lastName) return;
    this.saving = true;
    const req = this.editMode ? this.api.updateLead(this.editId, this.form) : this.api.createLead(this.form);
    req.subscribe({ next: () => { this.message.add({ severity: 'success', summary: this.editMode ? 'Updated' : 'Created' }); this.showDialog = false; this.load(); this.saving = false; }, error: () => { this.saving = false; } });
  }

  statusSeverity(s: string) { return ({ NEW: 'info', CONTACTED: 'warn', QUALIFIED: 'success', PROPOSAL: 'info', NEGOTIATION: 'warn', CLOSED_WON: 'success', CLOSED_LOST: 'danger' } as Record<string, string>)[s] || 'info'; }
}

import { Component, OnInit } from '@angular/core';
import { DatePipe, SlicePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { PropertyApiService } from './services/property-api.service';
import { MaintenanceTicket } from './models/maintenance.model';

@Component({
  selector: 'app-maintenance-list',
  standalone: true,
  imports: [DatePipe, SlicePipe, TableModule, ButtonModule, TagModule, DialogModule, InputTextModule, SelectModule, FormsModule, ToastModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Maintenance</h2>
      <p-button label="New Ticket" icon="pi pi-plus" severity="success" (onClick)="openDialog()" />
    </div>

    <p-table [value]="tickets" [paginator]="true" [rows]="15" [loading]="loading" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Unit ID</th>
          <th>Category</th>
          <th>Title</th>
          <th>Priority</th>
          <th>Status</th>
          <th>Created</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-t>
        <tr>
          <td>{{ t.unitId | slice:0:8 }}</td>
          <td>{{ t.category || '-' }}</td>
          <td class="font-medium">{{ t.title }}</td>
          <td><p-tag [value]="t.priority" [severity]="prioritySeverity(t.priority)" /></td>
          <td><p-tag [value]="t.status" [severity]="statusSeverity(t.status)" /></td>
          <td>{{ t.createdAt | date:'short' }}</td>
          <td>
            <div class="flex gap-1">
              @if (t.status === 'OPEN') {
                <p-button icon="pi pi-play" severity="warn" size="small" (onClick)="updateStatus(t, 'IN_PROGRESS')" pTooltip="Start" />
              }
              @if (t.status === 'IN_PROGRESS') {
                <p-button icon="pi pi-check" severity="success" size="small" (onClick)="updateStatus(t, 'RESOLVED')" pTooltip="Resolve" />
              }
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="7" class="text-center text-gray-500 py-4">No maintenance tickets found.</td></tr>
      </ng-template>
    </p-table>

    <p-dialog [(visible)]="showDialog" header="New Maintenance Ticket" [modal]="true" [style]="{ width: '500px' }">
      <div class="flex flex-column gap-3">
        <div><label class="text-sm font-medium block mb-1">Unit ID *</label><input pInputText [(ngModel)]="form.unitId" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Category</label><input pInputText [(ngModel)]="form.category" class="w-full" placeholder="e.g. Plumbing, Electrical" /></div>
        <div><label class="text-sm font-medium block mb-1">Title *</label><input pInputText [(ngModel)]="form.title" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Description</label><input pInputText [(ngModel)]="form.description" class="w-full" /></div>
        <div>
          <label class="text-sm font-medium block mb-1">Priority</label>
          <p-select [(ngModel)]="form.priority" [options]="priorities" optionLabel="label" optionValue="value" class="w-full" />
        </div>
        <p-button label="Create Ticket" [loading]="saving" (onClick)="create()" />
      </div>
    </p-dialog>
  `,
})
export class MaintenanceListComponent implements OnInit {
  tickets: MaintenanceTicket[] = [];
  loading = false;
  saving = false;
  showDialog = false;
  priorities = [
    { label: 'Low', value: 'LOW' },
    { label: 'Medium', value: 'MEDIUM' },
    { label: 'High', value: 'HIGH' },
    { label: 'Emergency', value: 'EMERGENCY' },
  ];
  form = { unitId: '', category: '', title: '', description: '', priority: 'MEDIUM' };

  constructor(private api: PropertyApiService, private message: MessageService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.api.getMaintenanceTickets().subscribe({ next: (res) => { this.tickets = res; this.loading = false; }, error: () => { this.loading = false; } });
  }

  openDialog() { this.form = { unitId: '', category: '', title: '', description: '', priority: 'MEDIUM' }; this.showDialog = true; }

  create() {
    if (!this.form.title || !this.form.unitId) return;
    this.saving = true;
    this.api.createMaintenanceTicket(this.form).subscribe({
      next: () => { this.message.add({ severity: 'success', summary: 'Created' }); this.showDialog = false; this.load(); this.saving = false; },
      error: () => { this.saving = false; },
    });
  }

  updateStatus(t: MaintenanceTicket, status: string) {
    this.api.updateMaintenanceStatus(t.id, status).subscribe({
      next: () => { this.message.add({ severity: 'success', summary: 'Updated' }); this.load(); },
      error: () => { this.message.add({ severity: 'error', summary: 'Error' }); },
    });
  }

  prioritySeverity(s: string) { return ({ LOW: 'info', MEDIUM: 'warn', HIGH: 'danger', EMERGENCY: 'danger' } as Record<string, string>)[s] || 'info'; }
  statusSeverity(s: string) { return ({ OPEN: 'warn', IN_PROGRESS: 'info', RESOLVED: 'success', CLOSED: 'danger' } as Record<string, string>)[s] || 'info'; }
}

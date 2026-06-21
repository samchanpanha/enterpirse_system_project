import { Component, OnInit } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { RealtyApiService } from './services/realty-api.service';
import { Agent, AgentRequest } from './models/realty.model';

@Component({
  selector: 'app-agent-list',
  standalone: true,
  imports: [TableModule, ButtonModule, TagModule, DialogModule, InputTextModule, FormsModule, ToastModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Real Estate Agents</h2>
      <p-button label="Add Agent" icon="pi pi-plus" severity="success" (onClick)="openDialog()" />
    </div>

    <p-table [value]="agents" [paginator]="true" [rows]="15" [loading]="loading" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Phone</th>
          <th>License</th>
          <th>Specialization</th>
          <th>Listings</th>
          <th>Sales</th>
          <th>Rating</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-a>
        <tr>
          <td class="font-medium">{{ a.firstName }} {{ a.lastName }}</td>
          <td>{{ a.email }}</td>
          <td>{{ a.phone }}</td>
          <td>{{ a.licenseNumber }}</td>
          <td>{{ a.specialization }}</td>
          <td>{{ a.totalListings }}</td>
          <td>{{ a.totalSales }}</td>
          <td><i class="pi pi-star-fill text-yellow-500 mr-1"></i>{{ a.rating }}</td>
          <td><p-tag [value]="a.status" [severity]="a.status === 'ACTIVE' ? 'success' : 'danger'" /></td>
          <td><p-button icon="pi pi-pencil" severity="info" size="small" (onClick)="edit(a)" /></td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="10" class="text-center text-gray-500 py-4">No agents found.</td></tr>
      </ng-template>
    </p-table>

    <p-dialog [(visible)]="showDialog" [header]="editMode ? 'Edit Agent' : 'New Agent'" [modal]="true" [style]="{ width: '500px' }">
      <div class="flex flex-column gap-3">
        <div class="grid">
          <div class="col-6"><label class="text-sm font-medium block mb-1">First Name *</label><input pInputText [(ngModel)]="form.firstName" class="w-full" /></div>
          <div class="col-6"><label class="text-sm font-medium block mb-1">Last Name *</label><input pInputText [(ngModel)]="form.lastName" class="w-full" /></div>
        </div>
        <div><label class="text-sm font-medium block mb-1">Email *</label><input pInputText [(ngModel)]="form.email" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Phone *</label><input pInputText [(ngModel)]="form.phone" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">License Number</label><input pInputText [(ngModel)]="form.licenseNumber" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Specialization</label><input pInputText [(ngModel)]="form.specialization" class="w-full" placeholder="e.g. Residential, Commercial" /></div>
        <p-button [label]="editMode ? 'Update' : 'Create'" [loading]="saving" (onClick)="save()" />
      </div>
    </p-dialog>
  `,
})
export class AgentListComponent implements OnInit {
  agents: Agent[] = [];
  loading = false;
  saving = false;
  showDialog = false;
  editMode = false;
  editId = '';
  form: AgentRequest = { firstName: '', lastName: '', email: '', phone: '', licenseNumber: '', specialization: '' };

  constructor(private api: RealtyApiService, private message: MessageService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.api.getAgents().subscribe({ next: (res) => { this.agents = res; this.loading = false; }, error: () => { this.loading = false; } });
  }

  openDialog() { this.editMode = false; this.form = { firstName: '', lastName: '', email: '', phone: '', licenseNumber: '', specialization: '' }; this.showDialog = true; }

  edit(a: Agent) {
    this.editMode = true; this.editId = a.id;
    this.form = { firstName: a.firstName, lastName: a.lastName, email: a.email, phone: a.phone, licenseNumber: a.licenseNumber, specialization: a.specialization };
    this.showDialog = true;
  }

  save() {
    if (!this.form.firstName || !this.form.lastName) return;
    this.saving = true;
    const req = this.editMode ? this.api.updateAgent(this.editId, this.form) : this.api.createAgent(this.form);
    req.subscribe({ next: () => { this.message.add({ severity: 'success', summary: this.editMode ? 'Updated' : 'Created' }); this.showDialog = false; this.load(); this.saving = false; }, error: () => { this.saving = false; } });
  }
}

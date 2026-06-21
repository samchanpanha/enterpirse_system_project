import { Component, OnInit } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { DeliveryApiService } from './services/delivery-api.service';
import { Driver, DriverRequest } from './models/delivery.model';

@Component({
  selector: 'app-driver-list',
  standalone: true,
  imports: [TableModule, ButtonModule, TagModule, DialogModule, InputTextModule, InputNumberModule, SelectModule, FormsModule, ToastModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Drivers</h2>
      <p-button label="Add Driver" icon="pi pi-plus" severity="success" (onClick)="openDialog()" />
    </div>

    <p-table [value]="drivers" [paginator]="true" [rows]="15" [loading]="loading" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Phone</th>
          <th>Email</th>
          <th>License</th>
          <th>Vehicle Type</th>
          <th>Rating</th>
          <th>Deliveries</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-d>
        <tr>
          <td class="font-medium">{{ d.firstName }} {{ d.lastName }}</td>
          <td>{{ d.phone }}</td>
          <td>{{ d.email }}</td>
          <td>{{ d.licenseNumber }}</td>
          <td>{{ d.vehicleType }}</td>
          <td><i class="pi pi-star-fill text-yellow-500 mr-1"></i>{{ d.rating }}</td>
          <td>{{ d.totalDeliveries }}</td>
          <td><p-tag [value]="d.status" [severity]="statusSeverity(d.status)" /></td>
          <td>
            <div class="flex gap-1">
              <p-button icon="pi pi-pencil" severity="info" size="small" (onClick)="edit(d)" />
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="9" class="text-center text-gray-500 py-4">No drivers found.</td></tr>
      </ng-template>
    </p-table>

    <p-dialog [(visible)]="showDialog" [header]="editMode ? 'Edit Driver' : 'New Driver'" [modal]="true" [style]="{ width: '500px' }">
      <div class="flex flex-column gap-3">
        <div class="grid">
          <div class="col-6"><label class="text-sm font-medium block mb-1">First Name *</label><input pInputText [(ngModel)]="form.firstName" class="w-full" /></div>
          <div class="col-6"><label class="text-sm font-medium block mb-1">Last Name *</label><input pInputText [(ngModel)]="form.lastName" class="w-full" /></div>
        </div>
        <div><label class="text-sm font-medium block mb-1">Email *</label><input pInputText [(ngModel)]="form.email" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Phone *</label><input pInputText [(ngModel)]="form.phone" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">License Number *</label><input pInputText [(ngModel)]="form.licenseNumber" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Vehicle Type *</label><input pInputText [(ngModel)]="form.vehicleType" class="w-full" placeholder="e.g. Motorcycle, Car, Van" /></div>
        <p-button [label]="editMode ? 'Update' : 'Create'" [loading]="saving" (onClick)="save()" />
      </div>
    </p-dialog>
  `,
})
export class DriverListComponent implements OnInit {
  drivers: Driver[] = [];
  loading = false;
  saving = false;
  showDialog = false;
  editMode = false;
  editId = '';
  form: DriverRequest = { firstName: '', lastName: '', email: '', phone: '', licenseNumber: '', vehicleType: '' };

  constructor(private api: DeliveryApiService, private message: MessageService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.api.getDrivers().subscribe({ next: (res) => { this.drivers = res; this.loading = false; }, error: () => { this.loading = false; } });
  }

  openDialog() { this.editMode = false; this.form = { firstName: '', lastName: '', email: '', phone: '', licenseNumber: '', vehicleType: '' }; this.showDialog = true; }

  edit(d: Driver) {
    this.editMode = true; this.editId = d.id;
    this.form = { firstName: d.firstName, lastName: d.lastName, email: d.email, phone: d.phone, licenseNumber: d.licenseNumber, vehicleType: d.vehicleType };
    this.showDialog = true;
  }

  save() {
    if (!this.form.firstName || !this.form.lastName) return;
    this.saving = true;
    const req = this.editMode ? this.api.updateDriver(this.editId, this.form) : this.api.createDriver(this.form);
    req.subscribe({ next: () => { this.message.add({ severity: 'success', summary: this.editMode ? 'Updated' : 'Created' }); this.showDialog = false; this.load(); this.saving = false; }, error: () => { this.saving = false; } });
  }

  statusSeverity(s: string) { return ({ ACTIVE: 'success', INACTIVE: 'danger', ON_DELIVERY: 'warn', OFF_DUTY: 'info' } as Record<string, string>)[s] || 'info'; }
}

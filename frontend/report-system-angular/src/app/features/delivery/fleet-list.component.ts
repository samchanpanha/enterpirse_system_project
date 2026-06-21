import { Component, OnInit } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { DeliveryApiService } from './services/delivery-api.service';
import { FleetVehicle, FleetRequest } from './models/delivery.model';

@Component({
  selector: 'app-fleet-list',
  standalone: true,
  imports: [TableModule, ButtonModule, TagModule, DialogModule, InputTextModule, InputNumberModule, FormsModule, ToastModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Fleet Management</h2>
      <p-button label="Add Vehicle" icon="pi pi-plus" severity="success" (onClick)="openDialog()" />
    </div>

    <p-table [value]="vehicles" [paginator]="true" [rows]="15" [loading]="loading" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Plate Number</th>
          <th>Type</th>
          <th>Make</th>
          <th>Model</th>
          <th>Year</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-v>
        <tr>
          <td class="font-medium">{{ v.plateNumber }}</td>
          <td>{{ v.vehicleType }}</td>
          <td>{{ v.make }}</td>
          <td>{{ v.model }}</td>
          <td>{{ v.year }}</td>
          <td><p-tag [value]="v.status" [severity]="v.status === 'ACTIVE' ? 'success' : v.status === 'MAINTENANCE' ? 'warn' : 'danger'" /></td>
          <td>
            <div class="flex gap-1">
              <p-button icon="pi pi-pencil" severity="info" size="small" (onClick)="edit(v)" />
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="7" class="text-center text-gray-500 py-4">No vehicles found.</td></tr>
      </ng-template>
    </p-table>

    <p-dialog [(visible)]="showDialog" [header]="editMode ? 'Edit Vehicle' : 'New Vehicle'" [modal]="true" [style]="{ width: '500px' }">
      <div class="flex flex-column gap-3">
        <div><label class="text-sm font-medium block mb-1">Plate Number *</label><input pInputText [(ngModel)]="form.plateNumber" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Vehicle Type *</label><input pInputText [(ngModel)]="form.vehicleType" class="w-full" placeholder="e.g. Motorcycle, Car, Van" /></div>
        <div class="grid">
          <div class="col-4"><label class="text-sm font-medium block mb-1">Make</label><input pInputText [(ngModel)]="form.make" class="w-full" /></div>
          <div class="col-4"><label class="text-sm font-medium block mb-1">Model</label><input pInputText [(ngModel)]="form.model" class="w-full" /></div>
          <div class="col-4"><label class="text-sm font-medium block mb-1">Year</label><p-inputNumber [(ngModel)]="form.year" class="w-full" /></div>
        </div>
        <p-button [label]="editMode ? 'Update' : 'Create'" [loading]="saving" (onClick)="save()" />
      </div>
    </p-dialog>
  `,
})
export class FleetListComponent implements OnInit {
  vehicles: FleetVehicle[] = [];
  loading = false;
  saving = false;
  showDialog = false;
  editMode = false;
  editId = '';
  form: FleetRequest = { vehicleType: '', plateNumber: '', make: '', model: '', year: new Date().getFullYear() };

  constructor(private api: DeliveryApiService, private message: MessageService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.api.getFleet().subscribe({ next: (res) => { this.vehicles = res; this.loading = false; }, error: () => { this.loading = false; } });
  }

  openDialog() { this.editMode = false; this.form = { vehicleType: '', plateNumber: '', make: '', model: '', year: new Date().getFullYear() }; this.showDialog = true; }

  edit(v: FleetVehicle) {
    this.editMode = true; this.editId = v.id;
    this.form = { vehicleType: v.vehicleType, plateNumber: v.plateNumber, make: v.make, model: v.model, year: v.year };
    this.showDialog = true;
  }

  save() {
    if (!this.form.plateNumber || !this.form.vehicleType) return;
    this.saving = true;
    const req = this.editMode ? this.api.updateVehicle(this.editId, this.form) : this.api.createVehicle(this.form);
    req.subscribe({ next: () => { this.message.add({ severity: 'success', summary: this.editMode ? 'Updated' : 'Created' }); this.showDialog = false; this.load(); this.saving = false; }, error: () => { this.saving = false; } });
  }
}

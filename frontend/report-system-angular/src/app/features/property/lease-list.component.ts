import { Component, Input, OnInit } from '@angular/core';
import { CurrencyPipe, DatePipe, SlicePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { PropertyApiService } from './services/property-api.service';
import { Lease } from './models/lease.model';
import { LeaseFormDialogComponent } from './lease-form-dialog.component';

@Component({
  selector: 'app-lease-list',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    SlicePipe,
    TableModule,
    ButtonModule,
    TagModule,
    DialogModule,
    ConfirmDialogModule,
    ToastModule,
    LeaseFormDialogComponent,
  ],
  providers: [ConfirmationService],
  template: `
    <p-toast />
    <p-confirmdialog />

    <div class="flex justify-content-end mb-2">
      <p-button
        label="New Lease"
        icon="pi pi-plus"
        size="small"
        severity="success"
        (onClick)="showDialog()"
      />
    </div>

    <p-table
      [value]="leases"
      [loading]="loading"
      size="small"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="header">
        <tr>
          <th>Tenant</th>
          <th>Unit</th>
          <th>Start</th>
          <th>End</th>
          <th>Rent</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-l>
        <tr>
          <td class="font-medium">{{ l.tenantName }}</td>
          <td>{{ l.unitId | slice:0:8 }}...</td>
          <td>{{ l.startDate | date }}</td>
          <td>{{ l.endDate | date }}</td>
          <td>{{ l.rentAmount | currency }}</td>
          <td>
            <p-tag
              [value]="l.status"
              [severity]="l.status === 'ACTIVE' ? 'success' : l.status === 'EXPIRED' ? 'danger' : 'warn'"
            />
          </td>
          <td>
            <div class="flex gap-1">
              @if (l.status === 'ACTIVE') {
                <p-button
                  label="Terminate"
                  severity="danger"
                  size="small"
                  (onClick)="terminate(l)"
                />
                <p-button
                  label="Renew"
                  severity="info"
                  size="small"
                  (onClick)="renew(l)"
                />
              }
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr>
          <td colspan="7" class="text-center text-gray-500 py-3">
            No leases for this property.
          </td>
        </tr>
      </ng-template>
    </p-table>

    <app-lease-form-dialog
      [(visible)]="dialogVisible"
      [propertyId]="propertyId"
      (saved)="onLeaseSaved()"
    />
  `,
})
export class LeaseListComponent implements OnInit {
  @Input() propertyId = '';

  leases: Lease[] = [];
  loading = false;
  dialogVisible = false;

  constructor(
    private api: PropertyApiService,
    private message: MessageService,
    private confirmation: ConfirmationService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    if (!this.propertyId) return;
    this.loading = true;
    this.api.getUnitsByProperty(this.propertyId).subscribe({
      next: (units) => {
        if (units.length === 0) {
          this.leases = [];
          this.loading = false;
          return;
        }
        const unitIds = units.map((u) => u.id);
        Promise.all(
          unitIds.map((id) => this.api.getLeasesByUnit(id).toPromise()),
        ).then((results) => {
          this.leases = results.flat().filter(Boolean) as Lease[];
          this.loading = false;
        });
      },
      error: () => (this.loading = false),
    });
  }

  showDialog() {
    this.dialogVisible = true;
  }

  terminate(l: Lease) {
    this.confirmation.confirm({
      header: 'Terminate Lease',
      message: `Terminate lease for ${l.tenantName}?`,
      accept: () => {
        this.api.terminateLease(l.id).subscribe({
          next: () => {
            this.message.add({
              severity: 'success',
              summary: 'Terminated',
              detail: `Lease for ${l.tenantName} terminated`,
            });
            this.load();
          },
        });
      },
    });
  }

  renew(l: Lease) {
    const newEnd = prompt('New end date (YYYY-MM-DD):', l.endDate);
    if (!newEnd) return;
    this.api.renewLease(l.id, { newEndDate: newEnd }).subscribe({
      next: () => {
        this.message.add({
          severity: 'success',
          summary: 'Renewed',
          detail: `Lease for ${l.tenantName} renewed`,
        });
        this.load();
      },
    });
  }

  onLeaseSaved() {
    this.dialogVisible = false;
    this.load();
  }
}

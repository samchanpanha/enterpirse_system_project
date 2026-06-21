import { Component, Input, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { PropertyApiService } from './services/property-api.service';
import { Unit } from './models/unit.model';
import { UnitFormDialogComponent } from './unit-form-dialog.component';

@Component({
  selector: 'app-unit-list',
  standalone: true,
  imports: [
    CurrencyPipe,
    TableModule,
    ButtonModule,
    TagModule,
    DialogModule,
    ConfirmDialogModule,
    ToastModule,
    UnitFormDialogComponent,
  ],
  providers: [ConfirmationService],
  template: `
    <p-toast />
    <p-confirmdialog />

    <div class="flex justify-content-end mb-2">
      <p-button
        label="Add Unit"
        icon="pi pi-plus"
        size="small"
        severity="success"
        (onClick)="showDialog()"
      />
    </div>

    <p-table
      [value]="units"
      [loading]="loading"
      size="small"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="header">
        <tr>
          <th>Label</th>
          <th>Floor</th>
          <th>Bedrooms</th>
          <th>Bathrooms</th>
          <th>Rent</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-u>
        <tr>
          <td class="font-medium">{{ u.label }}</td>
          <td>{{ u.floor }}</td>
          <td>{{ u.bedrooms }}</td>
          <td>{{ u.bathrooms }}</td>
          <td>{{ u.rentAmount | currency }}</td>
          <td>
            <p-tag
              [value]="u.status"
              [severity]="u.status === 'VACANT' ? 'info' : u.status === 'OCCUPIED' ? 'success' : 'warn'"
            />
          </td>
          <td>
            <div class="flex gap-1">
              <p-button
                icon="pi pi-pencil"
                severity="info"
                size="small"
                (onClick)="edit(u)"
              />
              <p-button
                icon="pi pi-trash"
                severity="danger"
                size="small"
                (onClick)="delete(u)"
              />
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr>
          <td colspan="7" class="text-center text-gray-500 py-3">
            No units for this property.
          </td>
        </tr>
      </ng-template>
    </p-table>

    <app-unit-form-dialog
      [(visible)]="dialogVisible"
      [unit]="selectedUnit"
      [propertyId]="propertyId"
      (saved)="onUnitSaved()"
    />
  `,
})
export class UnitListComponent implements OnInit {
  @Input() propertyId = '';

  units: Unit[] = [];
  loading = false;
  dialogVisible = false;
  selectedUnit?: Unit;

  constructor(
    private api: PropertyApiService,
    private confirmation: ConfirmationService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    if (!this.propertyId) return;
    this.loading = true;
    this.api.getUnitsByProperty(this.propertyId).subscribe({
      next: (res) => {
        this.units = res;
        this.loading = false;
      },
      error: () => { this.loading = false; this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load units' }); },
    });
  }

  showDialog() {
    this.selectedUnit = undefined;
    this.dialogVisible = true;
  }

  edit(u: Unit) {
    this.selectedUnit = u;
    this.dialogVisible = true;
  }

  delete(u: Unit) {
    this.confirmation.confirm({
      header: 'Delete Unit',
      message: `Delete unit "${u.label}"?`,
      accept: () => {
        this.api.deleteUnit(u.id).subscribe({
          next: () => {
            this.message.add({
              severity: 'success',
              summary: 'Deleted',
              detail: `Unit ${u.label} deleted`,
            });
            this.load();
          },
        });
      },
    });
  }

  onUnitSaved() {
    this.dialogVisible = false;
    this.load();
  }
}

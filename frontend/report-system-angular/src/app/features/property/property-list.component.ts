import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { PropertyApiService } from './services/property-api.service';
import { Property } from './models/property.model';

@Component({
  selector: 'app-property-list',
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    InputTextModule,
    TagModule,
    ConfirmDialogModule,
    ToastModule,
  ],
  providers: [ConfirmationService, MessageService],
  template: `
    <p-toast />
    <p-confirmdialog />

    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Properties</h2>
      <p-button
        label="Add Property"
        icon="pi pi-plus"
        severity="success"
        (onClick)="router.navigate(['properties', 'new'])"
      />
    </div>

    <p-table
      [value]="properties"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['name', 'type', 'city', 'district', 'status']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input
            pInputText
            type="text"
            placeholder="Search properties..."
            (input)="onSearch($event)"
          />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Type</th>
          <th>City</th>
          <th>District</th>
          <th>Units</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-p>
        <tr class="cursor-pointer" (click)="router.navigate(['properties', p.id])">
          <td class="font-medium">{{ p.name }}</td>
          <td>{{ p.type }}</td>
          <td>{{ p.city }}</td>
          <td>{{ p.district }}</td>
          <td>{{ p.totalUnits }}</td>
          <td>
            <p-tag
              [value]="p.status"
              [severity]="p.status === 'ACTIVE' ? 'success' : p.status === 'UNDER_MAINTENANCE' ? 'warn' : 'danger'"
            />
          </td>
          <td>
            <div class="flex gap-1">
              <p-button
                icon="pi pi-pencil"
                severity="info"
                size="small"
                (onClick)="edit($event, p)"
              />
              <p-button
                icon="pi pi-trash"
                severity="danger"
                size="small"
                (onClick)="delete($event, p)"
              />
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr>
          <td colspan="7" class="text-center text-gray-500 py-4">
            No properties found.
          </td>
        </tr>
      </ng-template>
    </p-table>
  `,
})
export class PropertyListComponent implements OnInit {
  properties: Property[] = [];
  loading = false;

  constructor(
    protected router: Router,
    private api: PropertyApiService,
    private confirmation: ConfirmationService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.api.getProperties().subscribe({
      next: (res) => {
        this.properties = res;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onSearch(event: Event) {
    const input = event.target as HTMLInputElement;
    // table filtering is handled by globalFilterFields
  }

  edit(event: Event, p: Property) {
    event.stopPropagation();
    this.router.navigate(['properties', p.id, 'edit']);
  }

  delete(event: Event, p: Property) {
    event.stopPropagation();
    this.confirmation.confirm({
      header: 'Delete Property',
      message: `Are you sure you want to delete "${p.name}"?`,
      accept: () => {
        this.api.deleteProperty(p.id).subscribe({
          next: () => {
            this.message.add({
              severity: 'success',
              summary: 'Deleted',
              detail: `${p.name} deleted`,
            });
            this.load();
          },
          error: () => {
            this.message.add({
              severity: 'error',
              summary: 'Error',
              detail: 'Failed to delete property',
            });
          },
        });
      },
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { RestaurantApiService } from './services/restaurant-api.service';
import { Outlet } from './models/outlet.model';

@Component({
  selector: 'app-outlet-list',
  standalone: true,
  imports: [TableModule, ButtonModule, InputTextModule, TagModule, ToastModule],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Restaurant Outlets</h2>
    </div>

    <p-table
      [value]="outlets"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['name', 'type', 'phone']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input pInputText type="text" placeholder="Search outlets..." />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Type</th>
          <th>Phone</th>
          <th>Currency</th>
          <th>Status</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-o>
        <tr class="cursor-pointer" (click)="router.navigate(['restaurant', o.id])">
          <td class="font-medium">{{ o.name }}</td>
          <td>{{ o.type || '—' }}</td>
          <td>{{ o.phone || '—' }}</td>
          <td>{{ o.currency }}</td>
          <td>
            <p-tag
              [value]="o.active ? 'ACTIVE' : 'INACTIVE'"
              [severity]="o.active ? 'success' : 'danger'"
            />
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="5" class="text-center text-gray-500 py-4">No outlets found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class OutletListComponent implements OnInit {
  outlets: Outlet[] = [];
  loading = false;

  constructor(
    protected router: Router,
    private api: RestaurantApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.api.getOutlets().subscribe({
      next: (res) => {
        this.outlets = res;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load outlets' });
      },
    });
  }
}

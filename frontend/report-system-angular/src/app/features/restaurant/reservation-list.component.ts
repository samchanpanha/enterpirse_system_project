import { Component, Input, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { RestaurantApiService } from './services/restaurant-api.service';
import { Reservation } from './models/reservation.model';

@Component({
  selector: 'app-reservation-list',
  standalone: true,
  imports: [DatePipe, TableModule, ButtonModule, TagModule, ToastModule],
  template: `
    <p-toast />
    <div class="flex justify-content-end mb-2">
      <p-button label="Refresh" icon="pi pi-refresh" size="small" severity="info" (onClick)="load()" />
    </div>

    <p-table [value]="reservations" [loading]="loading" size="small" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Guest</th>
          <th>Phone</th>
          <th>Guests</th>
          <th>Time</th>
          <th>Duration</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-r>
        <tr>
          <td class="font-medium">{{ r.guestName }}</td>
          <td>{{ r.guestPhone }}</td>
          <td>{{ r.guestCount }}</td>
          <td>{{ r.reservationTime | date:'short' }}</td>
          <td>{{ r.durationMinutes }} min</td>
          <td>
            <p-tag
              [value]="r.status"
              [severity]="r.status === 'confirmed' ? 'success' : r.status === 'cancelled' ? 'danger' : 'warn'"
            />
          </td>
          <td>
            @if (r.status === 'confirmed') {
              <p-button
                label="Cancel"
                size="small"
                severity="danger"
                (onClick)="cancel(r)"
              />
            }
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="7" class="text-center text-gray-500 py-3">No reservations found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class ReservationListComponent implements OnInit {
  @Input() outletId = '';

  reservations: Reservation[] = [];
  loading = false;

  constructor(
    private api: RestaurantApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
  }

  load() {
    if (!this.outletId) return;
    this.loading = true;
    this.api.getReservationsByOutlet(this.outletId).subscribe({
      next: (res) => {
        this.reservations = res;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  cancel(r: Reservation) {
    this.api.cancelReservation(r.id).subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Cancelled', detail: `Reservation for ${r.guestName} cancelled` });
        this.load();
      },
    });
  }
}

import { Component, Input, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { RestaurantApiService } from './services/restaurant-api.service';
import { Reservation } from './models/reservation.model';

@Component({
  selector: 'app-reservation-list',
  standalone: true,
  imports: [DatePipe, TableModule, ButtonModule, TagModule, DialogModule, InputTextModule, InputNumberModule, FormsModule, ToastModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Reservations</h2>
      <p-button label="New Reservation" icon="pi pi-plus" severity="success" (onClick)="openDialog()" />
    </div>

    <p-table [value]="reservations" [paginator]="true" [rows]="15" [loading]="loading" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Guest</th>
          <th>Phone</th>
          <th>Date/Time</th>
          <th>Guests</th>
          <th>Duration</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-r>
        <tr>
          <td class="font-medium">{{ r.guestName }}</td>
          <td>{{ r.guestPhone }}</td>
          <td>{{ r.reservationTime | date:'medium' }}</td>
          <td>{{ r.guestCount }}</td>
          <td>{{ r.durationMinutes }} min</td>
          <td><p-tag [value]="r.status" [severity]="statusSeverity(r.status)" /></td>
          <td>
            @if (r.status === 'CONFIRMED') {
              <p-button label="Cancel" size="small" severity="danger" (onClick)="cancel(r)" />
            }
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="7" class="text-center text-gray-500 py-4">No reservations found.</td></tr>
      </ng-template>
    </p-table>

    <p-dialog [(visible)]="showDialog" header="New Reservation" [modal]="true" [style]="{ width: '500px' }">
      <div class="flex flex-column gap-3">
        <div><label class="text-sm font-medium block mb-1">Guest Name *</label><input pInputText [(ngModel)]="form.guestName" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Phone *</label><input pInputText [(ngModel)]="form.guestPhone" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Date/Time *</label><input type="datetime-local" [(ngModel)]="form.reservationTime" class="w-full" /></div>
        <div class="grid">
          <div class="col-6"><label class="text-sm font-medium block mb-1">Guest Count *</label><p-inputNumber [(ngModel)]="form.guestCount" [min]="1" [max]="50" class="w-full" /></div>
          <div class="col-6"><label class="text-sm font-medium block mb-1">Duration (min)</label><p-inputNumber [(ngModel)]="form.durationMinutes" [min]="15" [max]="300" class="w-full" /></div>
        </div>
        <div><label class="text-sm font-medium block mb-1">Notes</label><input pInputText [(ngModel)]="form.notes" class="w-full" /></div>
        <p-button label="Create Reservation" [loading]="saving" (onClick)="create()" />
      </div>
    </p-dialog>
  `,
})
export class ReservationListComponent implements OnInit {
  @Input() outletId = '';

  reservations: Reservation[] = [];
  loading = false;
  saving = false;
  showDialog = false;
  form = { guestName: '', guestPhone: '', reservationTime: '', guestCount: 2, durationMinutes: 60, notes: '' };

  constructor(private api: RestaurantApiService, private message: MessageService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    if (this.outletId) {
      this.api.getReservationsByOutlet(this.outletId).subscribe({ next: (res) => { this.reservations = res; this.loading = false; }, error: () => { this.loading = false; } });
    } else {
      this.api.getReservations().subscribe({ next: (res) => { this.reservations = res; this.loading = false; }, error: () => { this.loading = false; } });
    }
  }

  openDialog() { this.form = { guestName: '', guestPhone: '', reservationTime: '', guestCount: 2, durationMinutes: 60, notes: '' }; this.showDialog = true; }

  create() {
    if (!this.form.guestName || !this.form.reservationTime) return;
    this.saving = true;
    const data = { ...this.form, outletId: this.outletId } as any;
    this.api.createReservation(data).subscribe({
      next: () => { this.message.add({ severity: 'success', summary: 'Created' }); this.showDialog = false; this.load(); this.saving = false; },
      error: () => { this.saving = false; },
    });
  }

  cancel(r: Reservation) {
    this.api.cancelReservation(r.id).subscribe({
      next: () => { this.message.add({ severity: 'info', summary: 'Cancelled' }); this.load(); },
    });
  }

  statusSeverity(s: string) { return ({ CONFIRMED: 'success', PENDING: 'warn', CANCELLED: 'danger', COMPLETED: 'info' } as Record<string, string>)[s] || 'info'; }
}

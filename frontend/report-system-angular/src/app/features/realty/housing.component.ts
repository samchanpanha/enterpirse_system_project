import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { CardModule } from 'primeng/card';
import { RealtyApiService } from './services/realty-api.service';
import { Resident } from './models/realty.model';

@Component({
  selector: 'app-housing',
  standalone: true,
  imports: [DatePipe, TableModule, ButtonModule, TagModule, CardModule],
  template: `
    <h2 class="text-xl font-bold mb-3">Housing & Residents</h2>

    <div class="grid mb-4">
      <div class="col-12 md:col-3">
        <div class="card p-3 surface-card border-round shadow-1 text-center">
          <div class="text-2xl font-bold text-blue-500">{{ totalResidents }}</div>
          <div class="text-sm text-gray-500">Total Residents</div>
        </div>
      </div>
      <div class="col-12 md:col-3">
        <div class="card p-3 surface-card border-round shadow-1 text-center">
          <div class="text-2xl font-bold text-green-500">{{ activeCount }}</div>
          <div class="text-sm text-gray-500">Active</div>
        </div>
      </div>
      <div class="col-12 md:col-3">
        <div class="card p-3 surface-card border-round shadow-1 text-center">
          <div class="text-2xl font-bold text-orange-500">{{ moveOutCount }}</div>
          <div class="text-sm text-gray-500">Move Out</div>
        </div>
      </div>
      <div class="col-12 md:col-3">
        <div class="card p-3 surface-card border-round shadow-1 text-center">
          <div class="text-2xl font-bold text-red-500">{{ evictedCount }}</div>
          <div class="text-sm text-gray-500">Evicted</div>
        </div>
      </div>
    </div>

    <p-table [value]="residents" [paginator]="true" [rows]="15" [loading]="loading" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Phone</th>
          <th>Unit</th>
          <th>Move In</th>
          <th>Lease End</th>
          <th>Status</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-r>
        <tr>
          <td class="font-medium">{{ r.firstName }} {{ r.lastName }}</td>
          <td>{{ r.email }}</td>
          <td>{{ r.phone }}</td>
          <td>{{ r.unitNumber }}</td>
          <td>{{ r.moveInDate | date }}</td>
          <td>{{ r.leaseEnd | date }}</td>
          <td><p-tag [value]="r.status" [severity]="r.status === 'ACTIVE' ? 'success' : r.status === 'MOVE_OUT' ? 'warn' : 'danger'" /></td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="7" class="text-center text-gray-500 py-4">No residents found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class HousingComponent implements OnInit {
  residents: Resident[] = [];
  loading = false;
  totalResidents = 0;
  activeCount = 0;
  moveOutCount = 0;
  evictedCount = 0;

  constructor(private api: RealtyApiService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.api.getResidents().subscribe({
      next: (res) => {
        this.residents = res;
        this.totalResidents = res.length;
        this.activeCount = res.filter(r => r.status === 'ACTIVE').length;
        this.moveOutCount = res.filter(r => r.status === 'MOVE_OUT').length;
        this.evictedCount = res.filter(r => r.status === 'EVICTED').length;
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }
}

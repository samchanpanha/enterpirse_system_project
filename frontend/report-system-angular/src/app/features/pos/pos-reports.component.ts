import { Component, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { CardModule } from 'primeng/card';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { PosApiService } from './services/pos-api.service';
import { RestaurantApiService } from '../restaurant/services/restaurant-api.service';
import { PosReport } from './models/pos.model';
import { Outlet } from '../restaurant/models/outlet.model';

@Component({
  selector: 'app-pos-reports',
  standalone: true,
  imports: [DecimalPipe, TableModule, ButtonModule, TagModule, CardModule, SelectModule, FormsModule],
  template: `
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">POS Daily Reports</h2>
      <div class="flex gap-2 align-items-center">
        <p-select [options]="outlets" optionLabel="name" optionValue="id" [(ngModel)]="selectedOutlet" placeholder="Select outlet" class="w-15rem" (onChange)="loadReport()" />
        <input type="date" [(ngModel)]="selectedDate" (change)="loadReport()" class="p-inputtext p-component" />
      </div>
    </div>

    @if (report) {
      <div class="grid mb-4">
        <div class="col-12 md:col-3">
          <div class="card p-3 surface-card border-round shadow-1 text-center">
            <div class="text-2xl font-bold text-primary">{{ report.totalOrders }}</div>
            <div class="text-sm text-gray-500">Total Orders</div>
          </div>
        </div>
        <div class="col-12 md:col-3">
          <div class="card p-3 surface-card border-round shadow-1 text-center">
            <div class="text-2xl font-bold text-green-600">\${{ report.totalRevenue | number:'1.2-2' }}</div>
            <div class="text-sm text-gray-500">Total Revenue</div>
          </div>
        </div>
        <div class="col-12 md:col-3">
          <div class="card p-3 surface-card border-round shadow-1 text-center">
            <div class="text-2xl font-bold text-blue-600">\${{ report.averageOrderValue | number:'1.2-2' }}</div>
            <div class="text-sm text-gray-500">Avg Order Value</div>
          </div>
        </div>
        <div class="col-12 md:col-3">
          <div class="card p-3 surface-card border-round shadow-1 text-center">
            <div class="text-2xl font-bold text-orange-600">{{ report.topItems?.length || 0 }}</div>
            <div class="text-sm text-gray-500">Unique Items Sold</div>
          </div>
        </div>
      </div>

      <div class="grid">
        <div class="col-12 md:col-6">
          <div class="card p-3 surface-card border-round shadow-1">
            <h3 class="text-sm font-bold text-gray-500 mb-2">Top Selling Items</h3>
            <p-table [value]="report.topItems || []" size="small" styleClass="p-datatable-striped">
              <ng-template pTemplate="header">
                <tr><th>Item</th><th>Qty Sold</th><th>Revenue</th></tr>
              </ng-template>
              <ng-template pTemplate="body" let-item>
                <tr>
                  <td class="font-medium">{{ item.name }}</td>
                  <td>{{ item.count }}</td>
                  <td>\${{ item.revenue | number:'1.2-2' }}</td>
                </tr>
              </ng-template>
            </p-table>
          </div>
        </div>
        <div class="col-12 md:col-6">
          <div class="card p-3 surface-card border-round shadow-1">
            <h3 class="text-sm font-bold text-gray-500 mb-2">Payment Breakdown</h3>
            <p-table [value]="report.paymentBreakdown || []" size="small" styleClass="p-datatable-striped">
              <ng-template pTemplate="header">
                <tr><th>Method</th><th>Count</th><th>Total</th></tr>
              </ng-template>
              <ng-template pTemplate="body" let-p>
                <tr>
                  <td><p-tag [value]="p.method" severity="info" /></td>
                  <td>{{ p.count }}</td>
                  <td>\${{ p.total | number:'1.2-2' }}</td>
                </tr>
              </ng-template>
            </p-table>
          </div>
        </div>
      </div>
    } @else {
      <div class="text-center text-gray-500 py-8">
        <i class="pi pi-chart-bar text-4xl mb-3"></i>
        <div>Select an outlet and date to view report</div>
      </div>
    }
  `,
})
export class PosReportsComponent implements OnInit {
  outlets: Outlet[] = [];
  selectedOutlet = '';
  selectedDate = new Date().toISOString().split('T')[0];
  report: PosReport | null = null;

  constructor(private posApi: PosApiService, private restApi: RestaurantApiService) {}

  ngOnInit() {
    this.restApi.getOutlets().subscribe({
      next: (res) => { this.outlets = res; if (res.length) { this.selectedOutlet = res[0].id; this.loadReport(); } },
    });
  }

  loadReport() {
    if (!this.selectedOutlet || !this.selectedDate) return;
    this.posApi.getDailyReport(this.selectedOutlet, this.selectedDate).subscribe({
      next: (res) => { this.report = res; },
      error: () => { this.report = null; },
    });
  }
}

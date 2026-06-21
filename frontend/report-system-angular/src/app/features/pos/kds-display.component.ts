import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';
import { PosApiService } from './services/pos-api.service';
import { RestaurantApiService } from '../restaurant/services/restaurant-api.service';
import { KdsOrder } from './models/pos.model';
import { Outlet } from '../restaurant/models/outlet.model';

@Component({
  selector: 'app-kds-display',
  standalone: true,
  imports: [DatePipe, TableModule, ButtonModule, TagModule, SelectModule, FormsModule, ToastModule, TooltipModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Kitchen Display System</h2>
      <div class="flex gap-2 align-items-center">
        <p-select [options]="outlets" optionLabel="name" optionValue="id" [(ngModel)]="selectedOutlet" placeholder="Select outlet" class="w-15rem" (onChange)="loadOrders()" />
        <p-button icon="pi pi-refresh" severity="secondary" (onClick)="loadOrders()" [loading]="loading" />
      </div>
    </div>

    <div class="grid">
      @for (order of orders; track order.id) {
        <div class="col-12 md:col-6 lg:col-4">
          <div class="card surface-card border-round shadow-1" [class.border-2.border-red-500]="order.priority === 'RUSH'" [class.border-2.border-yellow-500]="order.priority === 'VIP'">
            <div class="flex align-items-center justify-content-between mb-2">
              <div class="flex align-items-center gap-2">
                <span class="text-lg font-bold">T{{ order.tableNumber || '?' }}</span>
                @if (order.priority === 'RUSH') { <p-tag value="RUSH" severity="danger" /> }
                @if (order.priority === 'VIP') { <p-tag value="VIP" severity="warn" /> }
              </div>
              <span class="text-xs text-gray-500">{{ order.createdAt | date:'shortTime' }}</span>
            </div>
            <div class="border-top-1 surface-border pt-2">
              @for (item of order.items; track $index) {
                <div class="flex align-items-center justify-content-between mb-1" [class.text-green-600]="item.status === 'READY'" [class.line-through]="item.status === 'READY'">
                  <div class="flex align-items-center gap-2">
                    <span class="font-medium">{{ item.quantity }}x</span>
                    <span>{{ item.name }}</span>
                    @if (item.modifiers) { <span class="text-xs text-gray-500">({{ item.modifiers }})</span> }
                  </div>
                  <div class="flex gap-1">
                    @if (item.status === 'NEW') {
                      <p-button icon="pi pi-play" size="small" [rounded]="true" severity="warn" (onClick)="updateItemStatus(order.id, $index, 'IN_PROGRESS')" pTooltip="Start" />
                    }
                    @if (item.status === 'IN_PROGRESS') {
                      <p-button icon="pi pi-check" size="small" [rounded]="true" severity="success" (onClick)="updateItemStatus(order.id, $index, 'READY')" pTooltip="Ready" />
                    }
                    @if (item.status === 'READY') {
                      <i class="pi pi-check-circle text-green-500"></i>
                    }
                  </div>
                </div>
              }
            </div>
          </div>
        </div>
      } @empty {
        <div class="col-12 text-center text-gray-500 py-8">
          <i class="pi pi-inbox text-4xl mb-3"></i>
          <div>No pending orders</div>
        </div>
      }
    </div>
  `,
})
export class KdsDisplayComponent implements OnInit {
  outlets: Outlet[] = [];
  selectedOutlet = '';
  orders: KdsOrder[] = [];
  loading = false;

  constructor(private posApi: PosApiService, private restApi: RestaurantApiService, private message: MessageService) {}

  ngOnInit() {
    this.restApi.getOutlets().subscribe({
      next: (res) => { this.outlets = res; if (res.length) { this.selectedOutlet = res[0].id; this.loadOrders(); } },
    });
  }

  loadOrders() {
    if (!this.selectedOutlet) return;
    this.loading = true;
    this.posApi.getKdsOrders(this.selectedOutlet).subscribe({
      next: (res) => { this.orders = res; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  updateItemStatus(orderId: string, itemIndex: number, status: string) {
    this.posApi.updateKdsItemStatus(orderId, itemIndex, status).subscribe({
      next: () => { this.loadOrders(); },
      error: () => { this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to update status' }); },
    });
  }
}

import { Component } from '@angular/core';
import { CardModule } from 'primeng/card';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CardModule],
  template: `
    <div class="grid">
      <div class="col-12 md:col-6 lg:col-3">
        <p-card styleClass="shadow-2 border-round">
          <ng-template pTemplate="title">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-building text-2xl text-indigo-500"></i>
              <span class="text-lg font-semibold">Properties</span>
            </div>
          </ng-template>
          <ng-template pTemplate="content">
            <p class="text-3xl font-bold text-gray-800 m-0">--</p>
            <p class="text-sm text-gray-500 mt-2">Total properties</p>
          </ng-template>
        </p-card>
      </div>
      <div class="col-12 md:col-6 lg:col-3">
        <p-card styleClass="shadow-2 border-round">
          <ng-template pTemplate="title">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-box text-2xl text-green-500"></i>
              <span class="text-lg font-semibold">Inventory</span>
            </div>
          </ng-template>
          <ng-template pTemplate="content">
            <p class="text-3xl font-bold text-gray-800 m-0">--</p>
            <p class="text-sm text-gray-500 mt-2">Total products</p>
          </ng-template>
        </p-card>
      </div>
      <div class="col-12 md:col-6 lg:col-3">
        <p-card styleClass="shadow-2 border-round">
          <ng-template pTemplate="title">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-wallet text-2xl text-amber-500"></i>
              <span class="text-lg font-semibold">Finance</span>
            </div>
          </ng-template>
          <ng-template pTemplate="content">
            <p class="text-3xl font-bold text-gray-800 m-0">--</p>
            <p class="text-sm text-gray-500 mt-2">Total revenue</p>
          </ng-template>
        </p-card>
      </div>
      <div class="col-12 md:col-6 lg:col-3">
        <p-card styleClass="shadow-2 border-round">
          <ng-template pTemplate="title">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-credit-card text-2xl text-purple-500"></i>
              <span class="text-lg font-semibold">Payments</span>
            </div>
          </ng-template>
          <ng-template pTemplate="content">
            <p class="text-3xl font-bold text-gray-800 m-0">--</p>
            <p class="text-sm text-gray-500 mt-2">Pending payments</p>
          </ng-template>
        </p-card>
      </div>
    </div>
  `,
})
export class DashboardComponent {}

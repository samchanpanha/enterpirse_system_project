import { Component, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { forkJoin } from 'rxjs';
import { DashboardApiService } from './services/dashboard-api.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CurrencyPipe, RouterLink, CardModule, ButtonModule],
  template: `
    <div class="mb-4">
      <h1 class="text-2xl font-bold m-0">Dashboard</h1>
      <p class="text-gray-500 mt-1 mb-0">Overview of all your modules</p>
    </div>

    <!-- Row 1: Core Business Metrics -->
    <div class="grid mb-4">
      <div class="col-12 md:col-6 lg:col-3">
        <p-card styleClass="shadow-2 border-round cursor-pointer hover:shadow-4 transition-duration-200" (click)="navigate('/properties')">
          <ng-template pTemplate="title">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-building text-2xl text-indigo-500"></i>
              <span class="text-lg font-semibold">Properties</span>
            </div>
          </ng-template>
          <ng-template pTemplate="content">
            @if (loading) {
              <p class="text-3xl font-bold text-gray-800 m-0"><i class="pi pi-spin pi-spinner"></i></p>
            } @else {
              <p class="text-3xl font-bold text-gray-800 m-0">{{ propertyCount }}</p>
            }
            <p class="text-sm text-gray-500 mt-2">Total properties</p>
            <p class="text-sm text-indigo-500 mt-1">{{ leaseCount }} active leases</p>
          </ng-template>
        </p-card>
      </div>
      <div class="col-12 md:col-6 lg:col-3">
        <p-card styleClass="shadow-2 border-round cursor-pointer hover:shadow-4 transition-duration-200" (click)="navigate('/inventory')">
          <ng-template pTemplate="title">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-box text-2xl text-green-500"></i>
              <span class="text-lg font-semibold">Inventory</span>
            </div>
          </ng-template>
          <ng-template pTemplate="content">
            @if (loading) {
              <p class="text-3xl font-bold text-gray-800 m-0"><i class="pi pi-spin pi-spinner"></i></p>
            } @else {
              <p class="text-3xl font-bold text-gray-800 m-0">{{ inventoryCount }}</p>
            }
            <p class="text-sm text-gray-500 mt-2">Total products</p>
          </ng-template>
        </p-card>
      </div>
      <div class="col-12 md:col-6 lg:col-3">
        <p-card styleClass="shadow-2 border-round cursor-pointer hover:shadow-4 transition-duration-200" (click)="navigate('/finance')">
          <ng-template pTemplate="title">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-wallet text-2xl text-amber-500"></i>
              <span class="text-lg font-semibold">Finance</span>
            </div>
          </ng-template>
          <ng-template pTemplate="content">
            @if (loading) {
              <p class="text-3xl font-bold text-gray-800 m-0"><i class="pi pi-spin pi-spinner"></i></p>
            } @else {
              <p class="text-3xl font-bold text-gray-800 m-0">{{ financeRevenue | currency: 'USD' }}</p>
            }
            <p class="text-sm text-gray-500 mt-2">Total revenue</p>
            <p class="text-sm text-amber-500 mt-1">{{ invoiceCount }} invoices</p>
          </ng-template>
        </p-card>
      </div>
      <div class="col-12 md:col-6 lg:col-3">
        <p-card styleClass="shadow-2 border-round cursor-pointer hover:shadow-4 transition-duration-200" (click)="navigate('/payment')">
          <ng-template pTemplate="title">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-credit-card text-2xl text-purple-500"></i>
              <span class="text-lg font-semibold">Payments</span>
            </div>
          </ng-template>
          <ng-template pTemplate="content">
            @if (loading) {
              <p class="text-3xl font-bold text-gray-800 m-0"><i class="pi pi-spin pi-spinner"></i></p>
            } @else {
              <p class="text-3xl font-bold text-gray-800 m-0">{{ paymentPending }}</p>
            }
            <p class="text-sm text-gray-500 mt-2">Pending payments</p>
          </ng-template>
        </p-card>
      </div>
    </div>

    <!-- Row 2: Restaurant, Delivery, Realty, Reports -->
    <div class="grid mb-4">
      <div class="col-12 md:col-6 lg:col-3">
        <p-card styleClass="shadow-2 border-round cursor-pointer hover:shadow-4 transition-duration-200" (click)="navigate('/restaurant')">
          <ng-template pTemplate="title">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-shop text-2xl text-orange-500"></i>
              <span class="text-lg font-semibold">Restaurant</span>
            </div>
          </ng-template>
          <ng-template pTemplate="content">
            @if (loading) {
              <p class="text-3xl font-bold text-gray-800 m-0"><i class="pi pi-spin pi-spinner"></i></p>
            } @else {
              <p class="text-3xl font-bold text-gray-800 m-0">{{ restaurantOrderCount }}</p>
            }
            <p class="text-sm text-gray-500 mt-2">Active orders</p>
          </ng-template>
        </p-card>
      </div>
      <div class="col-12 md:col-6 lg:col-3">
        <p-card styleClass="shadow-2 border-round cursor-pointer hover:shadow-4 transition-duration-200" (click)="navigate('/delivery')">
          <ng-template pTemplate="title">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-truck text-2xl text-orange-600"></i>
              <span class="text-lg font-semibold">Delivery</span>
            </div>
          </ng-template>
          <ng-template pTemplate="content">
            @if (loading) {
              <p class="text-3xl font-bold text-gray-800 m-0"><i class="pi pi-spin pi-spinner"></i></p>
            } @else {
              <p class="text-3xl font-bold text-gray-800 m-0">{{ deliveryCount }}</p>
            }
            <p class="text-sm text-gray-500 mt-2">Total deliveries</p>
          </ng-template>
        </p-card>
      </div>
      <div class="col-12 md:col-6 lg:col-3">
        <p-card styleClass="shadow-2 border-round cursor-pointer hover:shadow-4 transition-duration-200" (click)="navigate('/realty')">
          <ng-template pTemplate="title">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-map text-2xl text-violet-500"></i>
              <span class="text-lg font-semibold">Real Estate</span>
            </div>
          </ng-template>
          <ng-template pTemplate="content">
            @if (loading) {
              <p class="text-3xl font-bold text-gray-800 m-0"><i class="pi pi-spin pi-spinner"></i></p>
            } @else {
              <p class="text-3xl font-bold text-gray-800 m-0">{{ listingCount }}</p>
            }
            <p class="text-sm text-gray-500 mt-2">Active listings</p>
          </ng-template>
        </p-card>
      </div>
      <div class="col-12 md:col-6 lg:col-3">
        <p-card styleClass="shadow-2 border-round cursor-pointer hover:shadow-4 transition-duration-200" (click)="navigate('/reports')">
          <ng-template pTemplate="title">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-chart-line text-2xl text-teal-500"></i>
              <span class="text-lg font-semibold">Reports</span>
            </div>
          </ng-template>
          <ng-template pTemplate="content">
            @if (loading) {
              <p class="text-3xl font-bold text-gray-800 m-0"><i class="pi pi-spin pi-spinner"></i></p>
            } @else {
              <p class="text-3xl font-bold text-gray-800 m-0">{{ reportCount }}</p>
            }
            <p class="text-sm text-gray-500 mt-2">Generated reports</p>
          </ng-template>
        </p-card>
      </div>
    </div>

    <!-- Row 3: Quick Actions -->
    <div class="grid">
      <div class="col-12">
        <div class="card surface-card border-round shadow-1 p-4">
          <h3 class="text-lg font-bold mb-3">Quick Actions</h3>
          <div class="flex flex-wrap gap-2">
            <p-button label="POS Terminal" icon="pi pi-shopping-cart" severity="success" routerLink="/pos" />
            <p-button label="New Property" icon="pi pi-plus" severity="info" routerLink="/properties/new" />
            <p-button label="New Listing" icon="pi pi-plus" severity="warn" routerLink="/realty" />
            <p-button label="New Delivery" icon="pi pi-truck" severity="secondary" routerLink="/delivery" />
            <p-button label="New Invoice" icon="pi pi-file" severity="danger" routerLink="/finance/invoices" />
            <p-button label="Generate Report" icon="pi pi-chart-line" severity="info" routerLink="/reports" />
          </div>
        </div>
      </div>
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  loading = true;
  propertyCount = 0;
  inventoryCount = 0;
  financeRevenue = 0;
  paymentPending = 0;
  restaurantOrderCount = 0;
  deliveryCount = 0;
  listingCount = 0;
  invoiceCount = 0;
  leaseCount = 0;
  reportCount = 0;

  constructor(private api: DashboardApiService, private router: Router) {}

  ngOnInit() {
    this.loading = true;
    forkJoin([
      this.api.getPropertyCount(),
      this.api.getInventoryCount(),
      this.api.getFinanceRevenue(),
      this.api.getPaymentPending(),
      this.api.getRestaurantOrderCount(),
      this.api.getDeliveryCount(),
      this.api.getListingCount(),
      this.api.getInvoiceCount(),
      this.api.getLeaseCount(),
      this.api.getReportCount(),
    ]).subscribe(([props, inv, fin, pmt, rest, del, list, invc, lease, rep]) => {
      this.propertyCount = props;
      this.inventoryCount = inv;
      this.financeRevenue = fin;
      this.paymentPending = pmt;
      this.restaurantOrderCount = rest;
      this.deliveryCount = del;
      this.listingCount = list;
      this.invoiceCount = invc;
      this.leaseCount = lease;
      this.reportCount = rep;
      this.loading = false;
    });
  }

  navigate(path: string) {
    this.router.navigate([path]);
  }
}

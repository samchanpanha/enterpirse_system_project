import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { PosApiService } from './services/pos-api.service';
import { PosCart, PosCartItem } from './models/pos.model';
import { RestaurantApiService } from '../restaurant/services/restaurant-api.service';
import { MenuItem } from '../restaurant/models/menu.model';
import { Outlet } from '../restaurant/models/outlet.model';
import { DecimalPipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-pos-terminal',
  standalone: true,
  imports: [DecimalPipe, TableModule, ButtonModule, TagModule, DialogModule, InputTextModule, InputNumberModule, SelectModule, FormsModule, ToastModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="grid" style="height: calc(100vh - 120px);">
      <!-- Left: Menu Items -->
      <div class="col-12 md:col-7">
        <div class="flex align-items-center gap-2 mb-3">
          <h2 class="text-xl font-bold m-0">POS Terminal</h2>
          <p-select [options]="outlets" optionLabel="name" optionValue="id" [(ngModel)]="selectedOutlet" placeholder="Select outlet" class="w-15rem" (onChange)="onOutletChange()" />
        </div>

        @if (categories.length > 0) {
          <div class="flex gap-2 mb-3 flex-wrap">
            @for (cat of categories; track cat.id) {
              <p-button [label]="cat.name" [severity]="selectedCategory === cat.id ? 'primary' : 'secondary'" size="small" (onClick)="filterByCategory(cat.id)" />
            }
            <p-button label="All" [severity]="!selectedCategory ? 'primary' : 'secondary'" size="small" (onClick)="selectedCategory = ''; loadMenuItems()" />
          </div>
        }

        <div class="grid">
          @for (item of filteredItems; track item.id) {
            <div class="col-6 md:col-4 lg:col-3">
              <div class="card p-3 surface-card border-round shadow-1 cursor-pointer hover:shadow-3 transition-duration-200" (click)="addToCart(item)">
                <div class="text-center">
                  <i class="pi pi-shopping-cart text-3xl text-primary mb-2"></i>
                  <div class="font-medium text-sm">{{ item.name }}</div>
                  <div class="text-primary font-bold">\${{ item.price }}</div>
                </div>
              </div>
            </div>
          }
        </div>
      </div>

      <!-- Right: Cart -->
      <div class="col-12 md:col-5">
        <div class="card surface-card border-round shadow-1" style="height: 100%; display: flex; flex-direction: column;">
          <div class="p-3 border-bottom-1 surface-border">
            <div class="flex align-items-center justify-content-between">
              <h3 class="text-lg font-bold m-0">Current Order</h3>
              <div class="flex gap-1">
                <input pInputText [(ngModel)]="tableNumber" placeholder="Table #" class="w-6rem" />
              </div>
            </div>
          </div>

          <div class="flex-1 overflow-y-auto p-3">
            @if (cart.items.length === 0) {
              <div class="text-center text-gray-500 py-6">
                <i class="pi pi-shopping-cart text-4xl mb-3"></i>
                <div>Click items to add to order</div>
              </div>
            } @else {
              <p-table [value]="cart.items" size="small" styleClass="p-datatable-striped">
                <ng-template pTemplate="header">
                  <tr>
                    <th>Item</th>
                    <th>Qty</th>
                    <th>Price</th>
                    <th></th>
                  </tr>
                </ng-template>
                <ng-template pTemplate="body" let-item>
                  <tr>
                    <td class="text-sm">{{ item.name }}</td>
                    <td>
                      <div class="flex gap-1 align-items-center">
                        <p-button icon="pi pi-minus" size="small" [rounded]="true" severity="secondary" (onClick)="updateQty(item, -1)" />
                        <span class="text-sm mx-1">{{ item.quantity }}</span>
                        <p-button icon="pi pi-plus" size="small" [rounded]="true" severity="secondary" (onClick)="updateQty(item, 1)" />
                      </div>
                    </td>
                    <td class="text-sm font-medium">\${{ (item.unitPrice * item.quantity) | number:'1.2-2' }}</td>
                    <td><p-button icon="pi pi-trash" size="small" severity="danger" [rounded]="true" (onClick)="removeItem(item)" /></td>
                  </tr>
                </ng-template>
              </p-table>
            }
          </div>

          @if (cart.items.length > 0) {
            <div class="p-3 border-top-1 surface-border">
              <div class="flex justify-content-between mb-2"><span class="text-gray-500">Subtotal</span><span class="font-medium">\${{ subtotal | number:'1.2-2' }}</span></div>
              <div class="flex justify-content-between mb-2"><span class="text-gray-500">Tax (10%)</span><span>\${{ tax | number:'1.2-2' }}</span></div>
              <div class="flex justify-content-between mb-3 text-lg font-bold"><span>Total</span><span class="text-primary">\${{ total | number:'1.2-2' }}</span></div>
              <div class="flex gap-2">
                <p-button label="Cash" icon="pi pi-wallet" severity="success" class="flex-1" (onClick)="submit('CASH')" />
                <p-button label="Card" icon="pi pi-credit-card" severity="info" class="flex-1" (onClick)="submit('CARD')" />
                <p-button label="QR" icon="pi pi-qrcode" severity="warn" class="flex-1" (onClick)="submit('QR_PAY')" />
              </div>
              <p-button label="Cancel Order" severity="danger" class="w-full mt-2" [outlined]="true" (onClick)="cancelOrder()" />
            </div>
          }
        </div>
      </div>
    </div>
  `,
})
export class PosTerminalComponent implements OnInit {
  outlets: Outlet[] = [];
  selectedOutlet = '';
  categories: any[] = [];
  menuItems: MenuItem[] = [];
  filteredItems: MenuItem[] = [];
  selectedCategory = '';
  tableNumber = '';
  cart: PosCart = { id: '', tenantId: '', branchId: '', outletId: '', customerId: '', tableNumber: '', items: [], subtotal: 0, tax: 0, total: 0, status: 'OPEN', createdAt: '', updatedAt: '' };

  get subtotal() { return this.cart.items.reduce((s, i) => s + i.unitPrice * i.quantity, 0); }
  get tax() { return this.subtotal * 0.1; }
  get total() { return this.subtotal + this.tax; }

  constructor(private posApi: PosApiService, private restApi: RestaurantApiService, private message: MessageService, private router: Router) {}

  ngOnInit() {
    this.restApi.getOutlets().subscribe({ next: (res) => { this.outlets = res; if (res.length) { this.selectedOutlet = res[0].id; this.onOutletChange(); } } });
  }

  onOutletChange() {
    if (!this.selectedOutlet) return;
    this.restApi.getCategories(this.selectedOutlet).subscribe({ next: (res) => { this.categories = res; this.loadMenuItems(); } });
  }

  loadMenuItems() {
    if (!this.selectedCategory) {
      const all$ = this.categories.map(c => this.restApi.getMenuItems(c.id));
      if (all$.length === 0) { this.filteredItems = []; return; }
      forkJoin(all$).subscribe((results: any[]) => { this.menuItems = results.flat(); this.filteredItems = [...this.menuItems]; });
    } else {
      this.restApi.getMenuItems(this.selectedCategory).subscribe({ next: (res) => { this.menuItems = res; this.filteredItems = [...this.menuItems]; } });
    }
  }

  filterByCategory(catId: string) { this.selectedCategory = catId; this.filteredItems = this.menuItems.filter(i => i.categoryId === catId); }

  addToCart(item: MenuItem) {
    const existing = this.cart.items.find(i => i.menuItemId === item.id);
    if (existing) { existing.quantity++; } else {
      this.cart.items.push({ id: Math.random().toString(36), menuItemId: item.id, name: item.name, quantity: 1, unitPrice: item.price, modifiers: '', total: item.price });
    }
  }

  updateQty(item: PosCartItem, delta: number) {
    item.quantity += delta;
    if (item.quantity <= 0) this.removeItem(item);
  }

  removeItem(item: PosCartItem) { this.cart.items = this.cart.items.filter(i => i.id !== item.id); }

  submit(method: string) {
    this.message.add({ severity: 'success', summary: 'Order Submitted', detail: `\$$${this.total.toFixed(2)} paid via ${method}` });
    this.cart = { id: '', tenantId: '', branchId: '', outletId: '', customerId: '', tableNumber: '', items: [], subtotal: 0, tax: 0, total: 0, status: 'OPEN', createdAt: '', updatedAt: '' };
    this.tableNumber = '';
  }

  cancelOrder() {
    this.cart.items = [];
    this.tableNumber = '';
    this.message.add({ severity: 'warn', summary: 'Order Cancelled' });
  }
}

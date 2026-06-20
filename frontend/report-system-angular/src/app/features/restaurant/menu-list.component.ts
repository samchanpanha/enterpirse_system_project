import { Component, Input, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
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
import { RestaurantApiService } from './services/restaurant-api.service';
import { Category, MenuItem, CategoryRequest, MenuItemRequest } from './models/menu.model';

@Component({
  selector: 'app-menu-list',
  standalone: true,
  imports: [
    FormsModule,
    CurrencyPipe,
    TableModule,
    ButtonModule,
    TagModule,
    DialogModule,
    InputTextModule,
    InputNumberModule,
    SelectModule,
    ToastModule,
  ],
  template: `
    <p-toast />
    <div class="flex justify-content-between align-items-center mb-2">
      <div class="flex gap-2 align-items-center">
        <p-select
          [options]="categories"
          optionLabel="name"
          optionValue="id"
          [(ngModel)]="selectedCategoryId"
          placeholder="Select category"
          class="w-15rem"
          (onChange)="loadItems()"
        />
        <p-button
          label="New Category"
          icon="pi pi-plus"
          size="small"
          severity="info"
          (onClick)="showCategoryDialog()"
        />
      </div>
      @if (selectedCategoryId) {
        <p-button
          label="Add Item"
          icon="pi pi-plus"
          size="small"
          severity="success"
          (onClick)="openItemDialog()"
        />
      }
    </div>

    @if (showCatDialog) {
      <p-dialog
        [(visible)]="showCatDialog"
        header="New Category"
        [modal]="true"
        [style]="{ width: '400px' }"
        (onHide)="showCatDialog = false"
      >
        <div class="flex flex-column gap-3">
          <input pInputText [(ngModel)]="catModel.name" placeholder="Category name" />
          <p-button label="Create" (onClick)="createCategory()" />
        </div>
      </p-dialog>
    }

    @if (showItemDialog) {
      <p-dialog
        [(visible)]="showItemDialog"
        header="New Menu Item"
        [modal]="true"
        [style]="{ width: '500px' }"
        (onHide)="showItemDialog = false"
      >
        <div class="flex flex-column gap-3">
          <input pInputText [(ngModel)]="itemModel.name" placeholder="Item name" />
          <p-inputNumber
            [(ngModel)]="itemModel.price"
            mode="currency"
            currency="USD"
            placeholder="Price"
            class="w-full"
          />
          <p-inputNumber
            [(ngModel)]="itemModel.taxRate"
            placeholder="Tax rate %"
            [min]="0"
            [max]="100"
            class="w-full"
          />
          <p-button label="Save" [loading]="saving" (onClick)="createItem()" />
        </div>
      </p-dialog>
    }

    <p-table [value]="items" [loading]="loadingItems" size="small" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Price</th>
          <th>Tax</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-m>
        <tr>
          <td class="font-medium">{{ m.name }}</td>
          <td>{{ m.price | currency }}</td>
          <td>{{ m.taxRate }}%</td>
          <td>
            <p-tag
              [value]="m.active ? 'ACTIVE' : 'INACTIVE'"
              [severity]="m.active ? 'success' : 'danger'"
            />
          </td>
          <td>
            <p-button
              icon="pi pi-trash"
              severity="danger"
              size="small"
              (onClick)="deleteItem(m)"
            />
          </td>
        </tr>
      </ng-template>
    </p-table>
  `,
})
export class MenuListComponent implements OnInit {
  @Input() outletId = '';

  categories: Category[] = [];
  items: MenuItem[] = [];
  selectedCategoryId = '';
  loadingItems = false;
  saving = false;

  showCatDialog = false;
  showItemDialog = false;

  catModel: CategoryRequest = { outletId: '', name: '' };
  itemModel: MenuItemRequest = { categoryId: '', name: '', price: 0, taxRate: 10 };

  constructor(
    private api: RestaurantApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.loadCategories();
  }

  loadCategories() {
    this.api.getCategories(this.outletId).subscribe({
      next: (res) => {
        this.categories = res;
        if (res.length > 0 && !this.selectedCategoryId) {
          this.selectedCategoryId = res[0].id;
          this.loadItems();
        }
      },
    });
  }

  loadItems() {
    if (!this.selectedCategoryId) {
      this.items = [];
      return;
    }
    this.loadingItems = true;
    this.api.getMenuItems(this.selectedCategoryId).subscribe({
      next: (res) => {
        this.items = res;
        this.loadingItems = false;
      },
      error: () => (this.loadingItems = false),
    });
  }

  showCategoryDialog() {
    this.catModel = { outletId: this.outletId, name: '' };
    this.showCatDialog = true;
  }

  createCategory() {
    if (!this.catModel.name) return;
    this.api.createCategory(this.catModel).subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Created', detail: 'Category created' });
        this.showCatDialog = false;
        this.loadCategories();
      },
    });
  }

  openItemDialog() {
    this.itemModel = { categoryId: this.selectedCategoryId, name: '', price: 0, taxRate: 10 };
    this.showItemDialog = true;
  }

  createItem() {
    if (!this.itemModel.name || !this.itemModel.price) return;
    this.saving = true;
    this.api.createMenuItem(this.itemModel).subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Created', detail: 'Menu item created' });
        this.showItemDialog = false;
        this.loadItems();
        this.saving = false;
      },
      error: () => (this.saving = false),
    });
  }

  deleteItem(m: MenuItem) {
    this.api.deleteMenuItem(m.id).subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Deleted', detail: `${m.name} deleted` });
        this.loadItems();
      },
    });
  }
}

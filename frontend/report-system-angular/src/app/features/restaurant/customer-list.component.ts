import { Component, Input, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { FormsModule } from '@angular/forms';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { RestaurantApiService } from './services/restaurant-api.service';
import { Customer } from './models/customer.model';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CurrencyPipe, TableModule, ButtonModule, TagModule, DialogModule, InputTextModule, FormsModule, ToastModule],
  providers: [MessageService],
  template: `
    <p-toast />
    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Customers</h2>
      <p-button label="Add Customer" icon="pi pi-plus" severity="success" (onClick)="openDialog()" />
    </div>

    <p-table [value]="customers" [paginator]="true" [rows]="15" [loading]="loading" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Phone</th>
          <th>Total Visits</th>
          <th>Total Spent</th>
          <th>VIP</th>
          <th>Status</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-c>
        <tr>
          <td class="font-medium">{{ c.name }}</td>
          <td>{{ c.email }}</td>
          <td>{{ c.phone }}</td>
          <td>{{ c.totalVisits }}</td>
          <td>{{ c.totalSpent | currency }}</td>
          <td><p-tag [value]="c.vip ? 'VIP' : 'Regular'" [severity]="c.vip ? 'warn' : 'info'" /></td>
          <td><p-tag value="Active" severity="success" /></td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="7" class="text-center text-gray-500 py-4">No customers found.</td></tr>
      </ng-template>
    </p-table>

    <p-dialog [(visible)]="showDialog" header="New Customer" [modal]="true" [style]="{ width: '500px' }">
      <div class="flex flex-column gap-3">
        <div><label class="text-sm font-medium block mb-1">Name *</label><input pInputText [(ngModel)]="form.name" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Phone *</label><input pInputText [(ngModel)]="form.phone" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Email</label><input pInputText [(ngModel)]="form.email" class="w-full" /></div>
        <div><label class="text-sm font-medium block mb-1">Birthday</label><input pInputText type="date" [(ngModel)]="form.birthday" class="w-full" /></div>
        <p-button label="Create" [loading]="saving" (onClick)="create()" />
      </div>
    </p-dialog>
  `,
})
export class CustomerListComponent implements OnInit {
  @Input() outletId = '';

  customers: Customer[] = [];
  loading = false;
  saving = false;
  showDialog = false;
  form = { name: '', phone: '', email: '', birthday: '', vip: false, notes: '' };

  constructor(private api: RestaurantApiService, private message: MessageService) {}
  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    if (this.outletId) {
      this.api.getCustomersByOutlet(this.outletId).subscribe({ next: (res) => { this.customers = res; this.loading = false; }, error: () => { this.loading = false; } });
    } else {
      this.api.getCustomers().subscribe({ next: (res) => { this.customers = res; this.loading = false; }, error: () => { this.loading = false; } });
    }
  }

  openDialog() { this.form = { name: '', phone: '', email: '', birthday: '', vip: false, notes: '' }; this.showDialog = true; }

  create() {
    if (!this.form.name) return;
    this.saving = true;
    const data = { ...this.form, outletId: this.outletId } as any;
    this.api.createCustomer(data).subscribe({
      next: () => { this.message.add({ severity: 'success', summary: 'Created' }); this.showDialog = false; this.load(); this.saving = false; },
      error: () => { this.saving = false; },
    });
  }
}

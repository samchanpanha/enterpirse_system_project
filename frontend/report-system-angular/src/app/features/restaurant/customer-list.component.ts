import { Component, Input, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { RestaurantApiService } from './services/restaurant-api.service';
import { Customer } from './models/customer.model';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CurrencyPipe, TableModule, TagModule],
  template: `
    <p-table [value]="customers" [loading]="loading" size="small" styleClass="p-datatable-striped">
      <ng-template pTemplate="header">
        <tr>
          <th>Name</th>
          <th>Phone</th>
          <th>Email</th>
          <th>Visits</th>
          <th>Spent</th>
          <th>VIP</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-c>
        <tr>
          <td class="font-medium">{{ c.name }}</td>
          <td>{{ c.phone }}</td>
          <td>{{ c.email || '—' }}</td>
          <td>{{ c.totalVisits }}</td>
          <td>{{ c.totalSpent | currency }}</td>
          <td>
            <p-tag [value]="c.vip ? 'VIP' : 'Regular'" [severity]="c.vip ? 'warn' : 'info'" />
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="6" class="text-center text-gray-500 py-3">No customers found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class CustomerListComponent implements OnInit {
  @Input() outletId = '';

  customers: Customer[] = [];
  loading = false;

  constructor(private api: RestaurantApiService) {}

  ngOnInit() {
    this.load();
  }

  load() {
    if (!this.outletId) return;
    this.loading = true;
    this.api.getCustomersByOutlet(this.outletId).subscribe({
      next: (res) => {
        this.customers = res;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }
}

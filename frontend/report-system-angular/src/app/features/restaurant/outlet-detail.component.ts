import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TabViewModule } from 'primeng/tabview';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { RestaurantApiService } from './services/restaurant-api.service';
import { Outlet } from './models/outlet.model';
import { MenuListComponent } from './menu-list.component';
import { OrderListComponent } from './order-list.component';
import { CustomerListComponent } from './customer-list.component';
import { ReservationListComponent } from './reservation-list.component';

@Component({
  selector: 'app-outlet-detail',
  standalone: true,
  imports: [
    TabViewModule,
    CardModule,
    TagModule,
    ButtonModule,
    MenuListComponent,
    OrderListComponent,
    CustomerListComponent,
    ReservationListComponent,
  ],
  template: `
    <div>
      <div class="flex align-items-center gap-2 mb-3">
        <button
          class="p-2 border-none bg-transparent cursor-pointer text-gray-500 hover:text-gray-700"
          (click)="router.navigate(['/restaurant'])"
        >
          <i class="pi pi-arrow-left text-xl"></i>
        </button>
        @if (outlet) {
          <h2 class="text-xl font-bold m-0">{{ outlet.name }}</h2>
        }
      </div>

      @if (outlet) {
        <p-tabView>
          <p-tabPanel header="Menu">
            <app-menu-list [outletId]="outletId" />
          </p-tabPanel>
          <p-tabPanel header="Orders">
            <app-order-list [outletId]="outletId" />
          </p-tabPanel>
          <p-tabPanel header="Customers">
            <app-customer-list [outletId]="outletId" />
          </p-tabPanel>
          <p-tabPanel header="Reservations">
            <app-reservation-list [outletId]="outletId" />
          </p-tabPanel>
        </p-tabView>
      } @else {
        <div class="text-center text-gray-500 py-6">Loading...</div>
      }
    </div>
  `,
})
export class OutletDetailComponent implements OnInit {
  outlet?: Outlet;
  outletId = '';

  constructor(
    protected router: Router,
    private route: ActivatedRoute,
    private api: RestaurantApiService,
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.outletId = id;
      this.api.getOutlet(id).subscribe({
        next: (o) => (this.outlet = o),
      });
    }
  }
}

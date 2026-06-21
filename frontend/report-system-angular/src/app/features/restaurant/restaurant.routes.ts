import { Routes } from '@angular/router';

export const restaurantRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./outlet-list.component').then((m) => m.OutletListComponent),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./outlet-detail.component').then((m) => m.OutletDetailComponent),
  },
  {
    path: 'customers',
    loadComponent: () =>
      import('./customer-list.component').then((m) => m.CustomerListComponent),
  },
  {
    path: 'reservations',
    loadComponent: () =>
      import('./reservation-list.component').then((m) => m.ReservationListComponent),
  },
];

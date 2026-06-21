import { Routes } from '@angular/router';

export const propertyRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./property-list.component').then((m) => m.PropertyListComponent),
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./property-form.component').then((m) => m.PropertyFormComponent),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./property-detail.component').then(
        (m) => m.PropertyDetailComponent,
      ),
  },
  {
    path: ':id/edit',
    loadComponent: () =>
      import('./property-form.component').then((m) => m.PropertyFormComponent),
  },
  {
    path: 'leases',
    loadComponent: () =>
      import('./lease-list.component').then((m) => m.LeaseListComponent),
  },
  {
    path: 'maintenance',
    loadComponent: () =>
      import('./maintenance-list.component').then((m) => m.MaintenanceListComponent),
  },
];

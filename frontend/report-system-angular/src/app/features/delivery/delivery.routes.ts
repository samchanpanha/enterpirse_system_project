import { Routes } from '@angular/router';

export const deliveryRoutes: Routes = [
  { path: '', loadComponent: () => import('./delivery-list.component').then(m => m.DeliveryListComponent) },
  { path: 'drivers', loadComponent: () => import('./driver-list.component').then(m => m.DriverListComponent) },
  { path: 'fleet', loadComponent: () => import('./fleet-list.component').then(m => m.FleetListComponent) },
  { path: 'tracking', loadComponent: () => import('./delivery-tracking.component').then(m => m.DeliveryTrackingComponent) },
];

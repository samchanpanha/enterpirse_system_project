import { Routes } from '@angular/router';

export const paymentRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./payment-list.component').then((m) => m.PaymentListComponent),
  },
];

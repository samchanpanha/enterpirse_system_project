import { Routes } from '@angular/router';
import { MainLayoutComponent } from './shared/layout/main-layout.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full',
  },
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.routes').then((m) => m.authRoutes),
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadChildren: () =>
          import('./features/dashboard/dashboard.routes').then(
            (m) => m.dashboardRoutes,
          ),
      },
      {
        path: 'properties',
        loadChildren: () =>
          import('./features/property/property.routes').then(
            (m) => m.propertyRoutes,
          ),
      },
      {
        path: 'restaurant',
        loadChildren: () =>
          import('./features/restaurant/restaurant.routes').then(
            (m) => m.restaurantRoutes,
          ),
      },
      {
        path: 'inventory',
        loadChildren: () =>
          import('./features/inventory/inventory.routes').then(
            (m) => m.inventoryRoutes,
          ),
      },
      {
        path: 'finance',
        loadChildren: () =>
          import('./features/finance/finance.routes').then(
            (m) => m.financeRoutes,
          ),
      },
      {
        path: 'payment',
        loadChildren: () =>
          import('./features/payment/payment.routes').then(
            (m) => m.paymentRoutes,
          ),
      },
      {
        path: 'reports',
        loadChildren: () =>
          import('./features/reporting/reporting.routes').then(
            (m) => m.reportingRoutes,
          ),
      },
    ],
  },
];

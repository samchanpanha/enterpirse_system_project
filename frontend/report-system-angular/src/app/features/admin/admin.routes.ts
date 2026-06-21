import { Routes } from '@angular/router';

export const adminRoutes: Routes = [
  {
    path: 'users',
    loadComponent: () =>
      import('./users/user-list.component').then((m) => m.UserListComponent),
  },
  {
    path: 'tenants',
    loadComponent: () =>
      import('./tenants/tenant-list.component').then(
        (m) => m.TenantListComponent,
      ),
  },
  {
    path: 'branches',
    loadComponent: () =>
      import('./branches/branch-list.component').then(
        (m) => m.BranchListComponent,
      ),
  },
  { path: '', redirectTo: 'users', pathMatch: 'full' },
];

import { Routes } from '@angular/router';

export const financeRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./chart-of-accounts-list.component').then(
        (m) => m.ChartOfAccountsListComponent,
      ),
  },
  {
    path: 'accounts/new',
    loadComponent: () =>
      import('./account-form.component').then((m) => m.AccountFormComponent),
  },
  {
    path: 'accounts/:id/edit',
    loadComponent: () =>
      import('./account-form.component').then((m) => m.AccountFormComponent),
  },
  {
    path: 'journal-entries',
    loadComponent: () =>
      import('./journal-entry-list.component').then(
        (m) => m.JournalEntryListComponent,
      ),
  },
  {
    path: 'invoices',
    loadComponent: () =>
      import('./invoice-list.component').then((m) => m.InvoiceListComponent),
  },
  {
    path: 'invoices/new',
    loadComponent: () =>
      import('./invoice-form.component').then((m) => m.InvoiceFormComponent),
  },
  {
    path: 'invoices/:id',
    loadComponent: () =>
      import('./invoice-detail.component').then(
        (m) => m.InvoiceDetailComponent,
      ),
  },
  {
    path: 'taxes',
    loadComponent: () =>
      import('./tax-list.component').then((m) => m.TaxListComponent),
  },
  {
    path: 'employees',
    loadComponent: () =>
      import('./employee-list.component').then(
        (m) => m.EmployeeListComponent,
      ),
  },
  {
    path: 'payroll',
    loadComponent: () =>
      import('./payroll-list.component').then((m) => m.PayrollListComponent),
  },
];

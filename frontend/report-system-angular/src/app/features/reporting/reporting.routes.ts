import { Routes } from '@angular/router';

export const reportingRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./report-list.component').then((m) => m.ReportListComponent),
  },
];

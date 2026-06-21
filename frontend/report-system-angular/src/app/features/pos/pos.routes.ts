import { Routes } from '@angular/router';

export const posRoutes: Routes = [
  { path: '', loadComponent: () => import('./pos-terminal.component').then(m => m.PosTerminalComponent) },
  { path: 'kds', loadComponent: () => import('./kds-display.component').then(m => m.KdsDisplayComponent) },
  { path: 'reports', loadComponent: () => import('./pos-reports.component').then(m => m.PosReportsComponent) },
];

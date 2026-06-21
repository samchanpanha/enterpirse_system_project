import { Routes } from '@angular/router';

export const realtyRoutes: Routes = [
  { path: '', loadComponent: () => import('./listing-list.component').then(m => m.ListingListComponent) },
  { path: 'agents', loadComponent: () => import('./agent-list.component').then(m => m.AgentListComponent) },
  { path: 'leads', loadComponent: () => import('./lead-list.component').then(m => m.LeadListComponent) },
  { path: 'housing', loadComponent: () => import('./housing.component').then(m => m.HousingComponent) },
];

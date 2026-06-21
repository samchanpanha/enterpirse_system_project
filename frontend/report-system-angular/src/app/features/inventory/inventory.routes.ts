import { Routes } from '@angular/router';

export const inventoryRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./product-list.component').then((m) => m.ProductListComponent),
  },
  {
    path: 'products/new',
    loadComponent: () =>
      import('./product-form.component').then((m) => m.ProductFormComponent),
  },
  {
    path: 'products/:id/edit',
    loadComponent: () =>
      import('./product-form.component').then((m) => m.ProductFormComponent),
  },
  {
    path: 'suppliers',
    loadComponent: () =>
      import('./supplier-list.component').then((m) => m.SupplierListComponent),
  },
  {
    path: 'purchase-orders',
    loadComponent: () =>
      import('./purchase-order-list.component').then(
        (m) => m.PurchaseOrderListComponent,
      ),
  },
  {
    path: 'purchase-orders/:id',
    loadComponent: () =>
      import('./purchase-order-detail.component').then(
        (m) => m.PurchaseOrderDetailComponent,
      ),
  },
  {
    path: 'transfers',
    loadComponent: () =>
      import('./stock-transfer-list.component').then(
        (m) => m.StockTransferListComponent,
      ),
  },
];

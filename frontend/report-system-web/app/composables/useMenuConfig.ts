import type { MenuItem } from './useLayoutState'

/**
 * Centralised sidebar menu config. Each entry declares:
 * - path: route the item links to
 * - title: display text
 * - icon: Iconify icon (ant-design:xxx)
 * - permission: optional permission code required to see the item
 * - feature: optional client-level feature code required to see the item
 * - children: sub-menu items
 *
 * TODO: gate items with `feature` once the feature store exists (Phase 5).
 * TODO: gate items with `permission` once the permission store exists (Phase 3).
 */
export const useMenuConfig = () => {
  const menu: MenuItem[] = [
    {
      path: '/app/dashboard',
      title: 'Dashboard',
      icon: 'ant-design:appstore-outlined'
    },
    {
      path: '/app/property',
      title: 'Property',
      icon: 'ant-design:home-outlined',
      children: [
        { path: '/app/property', title: 'List' },
        { path: '/app/property/schedule', title: 'Schedules' },
        { path: '/app/property/maintenance', title: 'Maintenance' }
      ]
    },
    {
      path: '/app/restaurant',
      title: 'Restaurant',
      icon: 'ant-design:shop-outlined',
      feature: 'restaurant.pos',
      children: [
        { path: '/app/restaurant', title: 'POS / Floor Plan' },
        { path: '/app/restaurant/menu', title: 'Menu', feature: 'restaurant.menu' },
        { path: '/app/restaurant/orders', title: 'Orders', feature: 'restaurant.orders' },
        { path: '/app/restaurant/kds', title: 'Kitchen Display', feature: 'restaurant.kds' },
        { path: '/app/restaurant/reservations', title: 'Reservations', feature: 'restaurant.reservations' },
        { path: '/app/restaurant/customers', title: 'Customers', feature: 'restaurant.customers' }
      ]
    },
    {
      path: '/app/inventory',
      title: 'Inventory',
      icon: 'ant-design:database-outlined',
      feature: 'inventory.products',
      children: [
        { path: '/app/inventory', title: 'Products & Stock' },
        { path: '/app/inventory/suppliers', title: 'Suppliers', feature: 'inventory.suppliers' },
        { path: '/app/inventory/purchase-orders', title: 'Purchase Orders', feature: 'inventory.purchase_orders' },
        { path: '/app/inventory/transfers', title: 'Stock Transfers', feature: 'inventory.stock_transfers' }
      ]
    },
    {
      path: '/app/finance',
      title: 'Finance',
      icon: 'ant-design:account-book-outlined',
      feature: 'finance.accounts',
      children: [
        { path: '/app/finance', title: 'Overview' },
        { path: '/app/finance/accounts', title: 'Chart of Accounts' },
        { path: '/app/finance/invoices', title: 'Invoices', feature: 'finance.invoices' },
        { path: '/app/finance/tax', title: 'Tax', feature: 'finance.tax' },
        { path: '/app/finance/employees', title: 'Employees', feature: 'finance.employees' },
        { path: '/app/finance/payroll', title: 'Payroll', feature: 'finance.payroll' }
      ]
    },
    {
      path: '/app/payment',
      title: 'Payments',
      icon: 'ant-design:credit-card-outlined',
      feature: 'payment.cash',
      children: [
        { path: '/app/payment', title: 'Transactions' },
        { path: '/app/payment/reconciliation', title: 'Reconciliation', feature: 'payment.reconciliation' }
      ]
    },
    {
      path: '/app/reports',
      title: 'Reports',
      icon: 'ant-design:bar-chart-outlined',
      feature: 'reports.definitions',
      children: [
        { path: '/app/reports', title: 'Report Definitions' },
        { path: '/app/reports/dashboards', title: 'Dashboards', feature: 'reports.dashboards' }
      ]
    },
    {
      path: '/app/admin',
      title: 'Admin',
      icon: 'ant-design:setting-outlined',
      feature: 'platform.branches',
      children: [
        { path: '/app/admin/branches', title: 'Branches' },
        { path: '/app/admin/users', title: 'Users', permission: 'users.read' },
        { path: '/app/admin/roles', title: 'Roles', permission: 'roles.read' },
        { path: '/app/admin/permissions', title: 'Permissions', permission: 'permissions.read' },
        { path: '/app/admin/clients', title: 'Clients', permission: 'clients.read' },
        { path: '/app/admin/features', title: 'Features', permission: 'features.read' }
      ]
    }
  ]

  return { menu }
}

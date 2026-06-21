import { Injectable, signal } from '@angular/core';

export interface NavItem {
  label: string;
  icon: string;
  route: string;
  badge?: string;
  badgeColor?: string;
  children?: NavItem[];
}

@Injectable({ providedIn: 'root' })
export class NavService {
  readonly collapsed = signal(false);
  readonly mobileOpen = signal(false);

  readonly navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'pi pi-home', route: '/dashboard' },
    {
      label: 'POS',
      icon: 'pi pi-shopping-cart',
      route: '/pos',
      badge: 'NEW',
      badgeColor: 'bg-emerald-500',
      children: [
        { label: 'POS Terminal', icon: 'pi pi-shopping-cart', route: '/pos' },
        { label: 'KDS Display', icon: 'pi pi-desktop', route: '/pos/kds' },
        { label: 'Daily Reports', icon: 'pi pi-file', route: '/pos/reports' },
      ],
    },
    {
      label: 'Property',
      icon: 'pi pi-building',
      route: '/properties',
      children: [
        { label: 'All Properties', icon: 'pi pi-building', route: '/properties' },
        { label: 'Leases', icon: 'pi pi-file-signature', route: '/properties/leases' },
        { label: 'Maintenance', icon: 'pi pi-wrench', route: '/properties/maintenance' },
      ],
    },
    {
      label: 'Real Estate',
      icon: 'pi pi-map',
      route: '/realty',
      badge: 'NEW',
      badgeColor: 'bg-violet-500',
      children: [
        { label: 'Listings', icon: 'pi pi-list', route: '/realty' },
        { label: 'Agents', icon: 'pi pi-users', route: '/realty/agents' },
        { label: 'Leads', icon: 'pi pi-phone', route: '/realty/leads' },
        { label: 'Housing', icon: 'pi pi-home', route: '/realty/housing' },
      ],
    },
    {
      label: 'Restaurant',
      icon: 'pi pi-shop',
      route: '/restaurant',
      children: [
        { label: 'Outlets', icon: 'pi pi-shop', route: '/restaurant' },
      ],
    },
    {
      label: 'Delivery',
      icon: 'pi pi-truck',
      route: '/delivery',
      badge: 'NEW',
      badgeColor: 'bg-orange-500',
      children: [
        { label: 'Dispatch', icon: 'pi pi-bolt', route: '/delivery' },
        { label: 'Drivers', icon: 'pi pi-users', route: '/delivery/drivers' },
        { label: 'Fleet', icon: 'pi pi-car', route: '/delivery/fleet' },
        { label: 'Tracking', icon: 'pi pi-map-marker', route: '/delivery/tracking' },
      ],
    },
    {
      label: 'Inventory',
      icon: 'pi pi-box',
      route: '/inventory',
      children: [
        { label: 'Products', icon: 'pi pi-box', route: '/inventory' },
        { label: 'Suppliers', icon: 'pi pi-truck', route: '/inventory/suppliers' },
        { label: 'Purchase Orders', icon: 'pi pi-shopping-cart', route: '/inventory/purchase-orders' },
        { label: 'Stock Transfers', icon: 'pi pi-arrows-h', route: '/inventory/transfers' },
      ],
    },
    {
      label: 'Finance',
      icon: 'pi pi-wallet',
      route: '/finance',
      children: [
        { label: 'Chart of Accounts', icon: 'pi pi-book', route: '/finance' },
        { label: 'Journal Entries', icon: 'pi pi-pencil', route: '/finance/journal-entries' },
        { label: 'Invoices', icon: 'pi pi-file', route: '/finance/invoices' },
        { label: 'Taxes', icon: 'pi pi-percentage', route: '/finance/taxes' },
        { label: 'Employees', icon: 'pi pi-users', route: '/finance/employees' },
        { label: 'Payroll', icon: 'pi pi-money-bill', route: '/finance/payroll' },
      ],
    },

    { label: 'Payments', icon: 'pi pi-credit-card', route: '/payment' },
    { label: 'Reports', icon: 'pi pi-chart-line', route: '/reports' },
  ];

  toggleCollapsed() {
    this.collapsed.update((v) => !v);
  }

  toggleMobile() {
    this.mobileOpen.update((v) => !v);
  }
}

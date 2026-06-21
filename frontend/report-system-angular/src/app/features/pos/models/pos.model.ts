export interface PosCart {
  id: string;
  tenantId: string;
  branchId: string;
  outletId: string;
  customerId: string;
  tableNumber: string;
  items: PosCartItem[];
  subtotal: number;
  tax: number;
  total: number;
  status: 'OPEN' | 'ORDERED' | 'PREPARING' | 'READY' | 'COMPLETED' | 'CANCELLED';
  createdAt: string;
  updatedAt: string;
}

export interface PosCartItem {
  id: string;
  menuItemId: string;
  name: string;
  quantity: number;
  unitPrice: number;
  modifiers: string;
  total: number;
}

export interface KdsOrder {
  id: string;
  orderId: string;
  outletId: string;
  tableNumber: string;
  items: KdsOrderItem[];
  status: 'NEW' | 'IN_PROGRESS' | 'READY';
  priority: 'NORMAL' | 'RUSH' | 'VIP';
  createdAt: string;
  startedAt: string;
  readyAt: string;
}

export interface KdsOrderItem {
  menuItemId: string;
  name: string;
  quantity: number;
  modifiers: string;
  status: 'NEW' | 'IN_PROGRESS' | 'READY';
  firedAt: string;
}

export interface PosReport {
  date: string;
  totalOrders: number;
  totalRevenue: number;
  averageOrderValue: number;
  topItems: { name: string; count: number; revenue: number }[];
  paymentBreakdown: { method: string; count: number; total: number }[];
}

export interface PosCartRequest {
  tenantId?: string;
  outletId: string;
  customerId?: string;
  tableNumber?: string;
  items: { menuItemId: string; quantity: number; unitPrice: number; modifiers?: string }[];
}

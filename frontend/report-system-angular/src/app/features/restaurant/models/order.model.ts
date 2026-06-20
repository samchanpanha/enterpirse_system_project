export interface Order {
  id: string;
  tenantId: string;
  branchId: string;
  outletId: string;
  tableId: string;
  customerId: string;
  orderNumber: string;
  type: string;
  status: string;
  subtotal: number;
  discount: number;
  taxAmount: number;
  serviceCharge: number;
  total: number;
  paymentStatus: string;
  notes: string;
  servedBy: string;
  createdAt: string;
  completedAt: string;
}

export interface OrderItem {
  id: string;
  orderId: string;
  menuItemId: string;
  quantity: number;
  unitPrice: number;
  modifiers: string;
  subtotal: number;
  status: string;
  notes: string;
  createdAt: string;
}

export interface OrderRequest {
  outletId: string;
  tableId?: string;
  customerId?: string;
  type?: string;
  notes?: string;
  servedBy?: string;
}

export interface OrderWithItemsRequest extends OrderRequest {
  items: OrderItemRequest[];
}

export interface OrderItemRequest {
  menuItemId: string;
  quantity: number;
  unitPrice: number;
  modifiers?: string;
}

export interface OrderCompleteRequest {
  discount?: number;
  serviceCharge?: number;
  paymentMethod?: string;
}

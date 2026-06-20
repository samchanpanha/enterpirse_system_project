export interface PurchaseOrder {
  id: string;
  tenantId: string;
  branchId: string;
  supplierId: string;
  poNumber: string;
  status: string;
  orderDate: string;
  expectedDate: string;
  receivedDate: string;
  subtotal: number;
  taxAmount: number;
  shippingCost: number;
  total: number;
  currency: string;
  notes: string;
  createdBy: string;
  approvedBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface PurchaseOrderItem {
  id: string;
  purchaseOrderId: string;
  productId: string;
  quantityOrdered: number;
  quantityReceived: number;
  unitCost: number;
  subtotal: number;
  createdAt: string;
}

export interface PurchaseOrderRequest {
  supplierId: string;
  orderDate: string;
  expectedDate?: string;
  notes?: string;
}

export interface PurchaseOrderItemRequest {
  productId: string;
  quantity: number;
  unitCost: number;
}

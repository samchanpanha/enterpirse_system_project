export interface StockTransfer {
  id: string;
  tenantId: string;
  transferNumber: string;
  fromBranchId: string;
  toBranchId: string;
  fromWarehouseId: string;
  toWarehouseId: string;
  status: string;
  notes: string;
  createdBy: string;
  approvedBy: string;
  shippedBy: string;
  receivedBy: string;
  shippedAt: string;
  receivedAt: string;
  cancelledAt: string;
  cancelReason: string;
  totalItems: number;
  totalValue: number;
  items: StockTransferItem[];
  createdAt: string;
  updatedAt: string;
}

export interface StockTransferItem {
  id: string;
  transferId: string;
  productId: string;
  productName: string;
  quantity: number;
  receivedQuantity: number;
  unitCost: number;
  discrepancyNotes: string;
  lineOrder: number;
  createdAt: string;
}

export interface StockTransferRequest {
  toBranchId: string;
  fromWarehouseId?: string;
  toWarehouseId?: string;
  notes?: string;
  items: { productId: string; quantity: number; unitCost?: number }[];
}

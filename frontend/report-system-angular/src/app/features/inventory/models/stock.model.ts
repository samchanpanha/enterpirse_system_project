export interface StockEntry {
  id: string;
  tenantId: string;
  branchId: string;
  warehouseId: string;
  productId: string;
  quantity: number;
  unitCost: number;
  batchNumber: string;
  expiryDate: string;
  referenceType: string;
  referenceId: string;
  notes: string;
  createdBy: string;
  createdAt: string;
}

export interface StockExit {
  id: string;
  tenantId: string;
  branchId: string;
  warehouseId: string;
  productId: string;
  quantity: number;
  referenceType: string;
  referenceId: string;
  notes: string;
  createdBy: string;
  createdAt: string;
}

export interface StockLevel {
  productId: string;
  quantity: number;
}

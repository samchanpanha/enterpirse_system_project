export interface Product {
  id: string; tenantId: string; categoryId: string
  name: string; nameKh?: string; sku: string; barcode?: string
  unit?: string; unitPrice: number; costPrice: number
  minStock?: number; maxStock?: number
  tracked: boolean; active: boolean; imageUrl?: string
  createdAt: string; updatedAt?: string
}

export interface Supplier {
  id: string; tenantId: string
  name: string; contactPerson?: string; phone: string
  email?: string; address?: string; taxNumber?: string
  paymentTerms?: string; currency?: string
  active: boolean; notes?: string
}

export interface PurchaseOrder {
  id: string; tenantId: string; supplierId: string
  poNumber: string; status: string
  orderDate: string; expectedDate?: string; receivedDate?: string
  subtotal: number; taxAmount?: number; shippingCost?: number
  total: number; currency?: string; notes?: string
  createdBy?: string; approvedBy?: string
}

export interface PurchaseOrderItem {
  id: string; purchaseOrderId: string; productId: string
  quantityOrdered: number; quantityReceived: number
  unitCost: number; subtotal: number
}

export interface StockEntry {
  id: string; tenantId: string; warehouseId: string; productId: string
  quantity: number; unitCost: number; batchNumber?: string
  expiryDate?: string; referenceType?: string; referenceId?: string
  notes?: string; createdBy?: string
}

export interface StockExit {
  id: string; tenantId: string; warehouseId: string; productId: string
  quantity: number; referenceType?: string; referenceId?: string
  notes?: string; createdBy?: string
}

export interface Warehouse {
  id: string; tenantId: string; name: string; type: string
  location?: string; active: boolean
}

export interface ProductCategory {
  id: string; tenantId: string; name: string; parentId?: string
}

export interface StockTransferItem {
  id?: string
  transferId?: string
  productId: string
  productName?: string
  quantity: number
  receivedQuantity?: number
  unitCost: number
  discrepancyNotes?: string
  lineOrder?: number
}

export type StockTransferStatus = 'DRAFT' | 'SHIPPED' | 'RECEIVED' | 'CANCELLED'

export interface StockTransfer {
  id: string
  tenantId: string
  transferNumber: string
  fromBranchId: string
  toBranchId: string
  fromWarehouseId?: string
  toWarehouseId?: string
  status: StockTransferStatus
  notes?: string
  createdBy?: string
  shippedBy?: string
  receivedBy?: string
  shippedAt?: string
  receivedAt?: string
  cancelledAt?: string
  cancelReason?: string
  totalItems: number
  totalValue: number
  createdAt: string
  updatedAt?: string
  items: StockTransferItem[]
}

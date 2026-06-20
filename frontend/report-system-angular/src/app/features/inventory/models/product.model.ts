export interface Product {
  id: string;
  tenantId: string;
  branchId: string;
  categoryId: string;
  name: string;
  nameKh: string;
  sku: string;
  barcode: string;
  unit: string;
  unitPrice: number;
  costPrice: number;
  minStock: number;
  maxStock: number;
  tracked: boolean;
  active: boolean;
  imageUrl: string;
  createdAt: string;
  updatedAt: string;
}

export interface ProductCategory {
  id: string;
  tenantId: string;
  name: string;
  parentId: string;
  createdAt: string;
}

export interface ProductRequest {
  categoryId?: string;
  name: string;
  sku: string;
  unitPrice: number;
  costPrice?: number;
  unit?: string;
  minStock?: number;
  maxStock?: number;
  tracked?: boolean;
}

export interface Category {
  id: string;
  tenantId: string;
  branchId: string;
  outletId: string;
  name: string;
  description: string;
  sortOrder: number;
  active: boolean;
  createdAt: string;
}

export interface MenuItem {
  id: string;
  tenantId: string;
  branchId: string;
  categoryId: string;
  name: string;
  nameKh: string;
  description: string;
  descriptionKh: string;
  price: number;
  currency: string;
  taxRate: number;
  imageUrl: string;
  options: string;
  modifiers: string;
  active: boolean;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface CategoryRequest {
  outletId: string;
  name: string;
  sortOrder?: number;
}

export interface MenuItemRequest {
  categoryId: string;
  name: string;
  price: number;
  taxRate?: number;
  active?: boolean;
  nameKh?: string;
  description?: string;
}

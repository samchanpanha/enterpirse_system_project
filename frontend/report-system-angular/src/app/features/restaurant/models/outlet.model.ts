export interface Outlet {
  id: string;
  tenantId: string;
  branchId: string;
  name: string;
  address: string;
  phone: string;
  email: string;
  taxNumber: string;
  type: string;
  currency: string;
  settings: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface OutletRequest {
  name: string;
  address?: string;
  phone?: string;
  email?: string;
  type?: string;
}

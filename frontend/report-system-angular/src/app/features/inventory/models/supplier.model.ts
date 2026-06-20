export interface Supplier {
  id: string;
  tenantId: string;
  branchId: string;
  name: string;
  contactPerson: string;
  phone: string;
  email: string;
  address: string;
  taxNumber: string;
  paymentTerms: string;
  currency: string;
  active: boolean;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export interface SupplierRequest {
  name: string;
  phone: string;
  contactPerson?: string;
  email?: string;
  address?: string;
  active?: boolean;
}

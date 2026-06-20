export interface Customer {
  id: string;
  tenantId: string;
  branchId: string;
  outletId: string;
  name: string;
  phone: string;
  email: string;
  birthday: string;
  vip: boolean;
  notes: string;
  totalVisits: number;
  totalSpent: number;
  lastVisitAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface CustomerRequest {
  outletId: string;
  name: string;
  phone: string;
  email?: string;
  vip?: boolean;
}

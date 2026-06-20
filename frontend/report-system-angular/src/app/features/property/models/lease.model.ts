export interface Lease {
  id: string;
  tenantId: string;
  branchId: string;
  unitId: string;
  tenantName: string;
  tenantPhone: string;
  tenantEmail: string;
  idType: string;
  idNumber: string;
  startDate: string;
  endDate: string;
  rentAmount: number;
  depositAmount: number;
  paymentDay: number;
  status: string;
  documents: string;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export interface LeaseRequest {
  unitId: string;
  tenantName: string;
  tenantPhone?: string;
  tenantEmail?: string;
  startDate: string;
  endDate: string;
  rentAmount: number;
  depositAmount?: number;
  paymentDay?: number;
  notes?: string;
}

export interface LeaseRenewRequest {
  newEndDate: string;
  newRent?: number;
}

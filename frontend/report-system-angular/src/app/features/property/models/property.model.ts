export interface Property {
  id: string;
  tenantId: string;
  branchId: string;
  name: string;
  type: string;
  address: string;
  city: string;
  district: string;
  totalUnits: number;
  status: string;
  ownerName: string;
  ownerPhone: string;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export interface PropertyRequest {
  name: string;
  type: string;
  address: string;
  city: string;
  district: string;
  status?: string;
  ownerName?: string;
  ownerPhone?: string;
  notes?: string;
}

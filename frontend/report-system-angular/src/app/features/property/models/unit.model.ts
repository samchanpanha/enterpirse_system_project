export interface Unit {
  id: string;
  tenantId: string;
  branchId: string;
  propertyId: string;
  label: string;
  floor: number;
  bedrooms: number;
  bathrooms: number;
  areaSqm: number;
  rentAmount: number;
  depositAmount: number;
  currency: string;
  status: string;
  type: string;
  amenities: string;
  images: string;
  createdAt: string;
  updatedAt: string;
}

export interface UnitRequest {
  propertyId: string;
  label: string;
  floor: number;
  bedrooms: number;
  bathrooms: number;
  rentAmount: number;
  status?: string;
  type?: string;
}

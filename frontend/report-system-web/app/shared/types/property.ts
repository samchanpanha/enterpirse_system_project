export interface Property {
  id: string
  tenantId: string
  name: string
  type: string
  address: string
  city: string
  district: string
  totalUnits: number
  status: string
}

export interface Unit {
  id: string
  tenantId: string
  branchId: string
  propertyId: string
  label: string
  floor: number
  bedrooms: number
  bathrooms: number
  areaSqm: number
  rentAmount: number
  depositAmount: number
  status: string
}

export interface Lease {
  id: string
  tenantId: string
  branchId: string
  unitId: string
  tenantName: string
  tenantPhone: string
  startDate: string
  endDate: string
  rentAmount: number
  depositAmount: number
  status: string
}

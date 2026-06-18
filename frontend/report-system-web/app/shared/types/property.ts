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
  propertyId: string
  label: string
  floor: number
  bedrooms: number
  rentAmount: number
  status: string
}

export interface Lease {
  id: string
  unitId: string
  tenantName: string
  startDate: string
  endDate: string
  rentAmount: number
  status: string
}

export interface Listing {
  id: string;
  tenantId: string;
  branchId: string;
  agentId: string;
  title: string;
  titleKh: string;
  description: string;
  descriptionKh: string;
  propertyType: string;
  listingType: string;
  status: 'ACTIVE' | 'SOLD' | 'RENTED' | 'WITHDRAWN' | 'EXPIRED';
  price: number;
  currency: string;
  areaSqm: number;
  bedrooms: number;
  bathrooms: number;
  floors: number;
  yearBuilt: number;
  address: string;
  city: string;
  district: string;
  province: string;
  lat: number;
  lng: number;
  features: string;
  featured: boolean;
  published: boolean;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Agent {
  id: string;
  tenantId: string;
  branchId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  licenseNumber: string;
  specialization: string;
  status: 'ACTIVE' | 'INACTIVE';
  totalSales: number;
  totalListings: number;
  rating: number;
  createdAt: string;
  updatedAt: string;
}

export interface Lead {
  id: string;
  tenantId: string;
  branchId: string;
  agentId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  source: string;
  status: 'NEW' | 'CONTACTED' | 'QUALIFIED' | 'PROPOSAL' | 'NEGOTIATION' | 'CLOSED_WON' | 'CLOSED_LOST';
  budget: number;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export interface Resident {
  id: string;
  tenantId: string;
  branchId: string;
  propertyId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  unitNumber: string;
  moveInDate: string;
  leaseEnd: string;
  status: 'ACTIVE' | 'MOVE_OUT' | 'EVICTED';
  createdAt: string;
  updatedAt: string;
}

export interface AgentRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  licenseNumber: string;
  specialization: string;
}

export interface LeadRequest {
  agentId?: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  source: string;
  budget?: number;
  notes?: string;
}

export interface ListingRequest {
  title: string;
  propertyType: string;
  listingType?: string;
  price?: number;
  areaSqm?: number;
  bedrooms?: number;
  bathrooms?: number;
  address?: string;
  city?: string;
  district?: string;
}

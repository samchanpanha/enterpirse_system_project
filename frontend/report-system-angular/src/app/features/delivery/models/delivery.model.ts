export interface Delivery {
  id: string;
  tenantId: string;
  branchId: string;
  outletId: string;
  orderId: string;
  driverId: string;
  customerName: string;
  customerPhone: string;
  deliveryAddress: string;
  pickupAddress: string;
  status: 'PENDING' | 'ASSIGNED' | 'PICKED_UP' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';
  estimatedDelivery: string;
  actualDelivery: string;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export interface Driver {
  id: string;
  tenantId: string;
  branchId: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  licenseNumber: string;
  vehicleType: string;
  status: 'ACTIVE' | 'INACTIVE' | 'ON_DELIVERY' | 'OFF_DUTY';
  rating: number;
  totalDeliveries: number;
  createdAt: string;
  updatedAt: string;
}

export interface FleetVehicle {
  id: string;
  tenantId: string;
  branchId: string;
  vehicleType: string;
  plateNumber: string;
  make: string;
  model: string;
  year: number;
  status: 'ACTIVE' | 'MAINTENANCE' | 'RETIRED';
  assignedDriverId: string;
  createdAt: string;
  updatedAt: string;
}

export interface DeliveryZone {
  id: string;
  tenantId: string;
  branchId: string;
  name: string;
  description: string;
  deliveryFee: number;
  minOrderAmount: number;
  estimatedTimeMinutes: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DriverPayout {
  id: string;
  tenantId: string;
  branchId: string;
  driverId: string;
  driverName: string;
  periodStart: string;
  periodEnd: string;
  totalDeliveries: number;
  totalEarnings: number;
  bonuses: number;
  deductions: number;
  netPay: number;
  status: 'PENDING' | 'PAID' | 'CANCELLED';
  createdAt: string;
  updatedAt: string;
}

export interface DeliveryRequest {
  tenantId?: string;
  outletId?: string;
  orderId?: string;
  driverId?: string;
  customerName: string;
  customerPhone: string;
  deliveryAddress: string;
  pickupAddress: string;
  notes?: string;
}

export interface DriverRequest {
  tenantId?: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  licenseNumber: string;
  vehicleType: string;
}

export interface FleetRequest {
  tenantId?: string;
  vehicleType: string;
  plateNumber: string;
  make: string;
  model: string;
  year: number;
  assignedDriverId?: string;
}

export interface ZoneRequest {
  tenantId?: string;
  name: string;
  description?: string;
  deliveryFee: number;
  minOrderAmount?: number;
  estimatedTimeMinutes: number;
}

export interface Outlet {
  id: string; tenantId: string; name: string
  address?: string; phone?: string; email?: string
  taxNumber?: string; type?: string; currency?: string
  settings?: string; active: boolean
  createdAt: string; updatedAt?: string
}

export interface RestaurantTable {
  id: string; tenantId: string; outletId: string
  label: string; capacity: number
  floor?: string; section?: string
  posX?: number; posY?: number
  status: string; qrCodeUrl?: string
}

export interface Category {
  id: string; tenantId: string; outletId: string
  name: string; description?: string
  sortOrder: number; active: boolean
}

export interface MenuItem {
  id: string; tenantId?: string; categoryId: string
  name: string; nameKh?: string; description?: string; descriptionKh?: string
  price: number; currency?: string; taxRate: number
  imageUrl?: string; options?: string; modifiers?: string
  active: boolean; sortOrder?: number
}

export interface OrderItem {
  id: string; orderId: string; menuItemId: string
  name?: string; quantity: number; unitPrice: number
  modifiers?: string; subtotal: number
  status: string; notes?: string
}

export interface Order {
  id: string; tenantId?: string; outletId: string
  tableId?: string; customerId?: string
  orderNumber: string; type: string; status: string
  subtotal: number; discount?: number
  taxAmount?: number; serviceCharge?: number
  total: number; paymentStatus: string
  notes?: string; servedBy?: string
  createdAt: string; completedAt?: string
  items?: OrderItem[]
}

export interface Customer {
  id: string; tenantId: string; outletId: string
  name: string; phone: string; email?: string
  birthday?: string; vip: boolean; notes?: string
  totalVisits: number; totalSpent: number
  lastVisitAt?: string
}

export interface Reservation {
  id: string; tenantId: string; outletId: string
  tableId?: string; customerId?: string
  customerName?: string; customerPhone?: string
  guestName?: string; guestPhone?: string; guestCount?: number
  partySize?: number
  reservationDate?: string; reservationTime?: string
  reservationAt?: string
  durationMinutes?: number
  status: string; notes?: string
}

export interface Reservation {
  id: string;
  tenantId: string;
  branchId: string;
  outletId: string;
  tableId: string;
  customerId: string;
  guestName: string;
  guestPhone: string;
  guestCount: number;
  reservationTime: string;
  durationMinutes: number;
  status: string;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReservationRequest {
  outletId: string;
  tableId?: string;
  guestName: string;
  guestPhone: string;
  guestCount: number;
  reservationTime: string;
  durationMinutes?: number;
  notes?: string;
}

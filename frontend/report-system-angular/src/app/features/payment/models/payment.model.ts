export interface PaymentTransaction {
  id: string;
  transactionNumber: string;
  transactionDate: string;
  type: 'SALE' | 'REFUND' | 'CHARGEBACK' | 'RECONCILIATION';
  method: 'CASH' | 'CARD' | 'BANK_TRANSFER' | 'MOBILE' | 'OTHER';
  amount: number;
  feeAmount: number;
  netAmount: number;
  currency: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED' | 'CANCELLED';
  referenceNumber?: string;
  description?: string;
  reconciled: boolean;
  reconciliationDate?: string;
  createdAt: string;
}

export interface ReconciliationRequest {
  startDate: string;
  endDate: string;
  notes?: string;
}

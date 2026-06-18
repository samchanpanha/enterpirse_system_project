export interface Transaction {
  id: string; tenantId: string; transactionId: string
  invoiceId?: string; amount: number; currency: string
  gateway: string; gatewayRef?: string; method: string
  status: string; customerName: string; customerPhone: string
  sourceType?: string; sourceId?: string
  paidAt?: string
}

export interface Refund {
  id: string; tenantId: string; transactionId: string
  amount: number; reason: string; gatewayRef?: string
  status: string; processedAt?: string; createdBy?: string
}

export interface ReconciliationRecord {
  id: string; tenantId: string; gateway: string
  statementDate: string
  totalExpected: number; totalMatched: number; totalUnmatched: number
  matchedCount: number; unmatchedCount: number
  status: string; processedAt?: string
}

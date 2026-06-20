import { defineStore } from 'pinia'
import type { Transaction, ReconciliationRecord } from '~/shared/types/payment'

export const usePaymentStore = defineStore('payment', () => {
  const branchStore = useBranchStore()

  const transactions = ref<Transaction[]>([])
  const reconciliations = ref<ReconciliationRecord[]>([])
  const loading = ref(false)

  async function fetchTransactions (tenantId: string) {
    loading.value = true
    try { transactions.value = await branchStore.$apiWithBranch(`/payment/payments/by-tenant/${tenantId}`) } finally { loading.value = false }
  }

  async function fetchReconciliations (tenantId: string) {
    loading.value = true
    try { reconciliations.value = await branchStore.$apiWithBranch(`/payment/payments/reconciliation/by-tenant/${tenantId}`) } finally { loading.value = false }
  }

  async function processPayment (data: any) {
    const t = await branchStore.$apiWithBranch('/payment/payments/process', { method: 'POST', body: data })
    transactions.value.unshift(t)
    return t
  }

  async function refundPayment (id: string, data: { amount: number; reason: string; createdBy?: string }) {
    return await branchStore.$apiWithBranch(`/payment/payments/${id}/refund`, { method: 'POST', body: data })
  }

  async function startReconciliation (data: { tenantId: string; gateway: string; statementDate: string }) {
    const r = await branchStore.$apiWithBranch('/payment/payments/reconciliation/start', { method: 'POST', body: data })
    reconciliations.value.push(r)
    return r
  }

  async function completeReconciliation (id: string) {
    const r = await branchStore.$apiWithBranch(`/payment/payments/reconciliation/${id}/complete`, { method: 'POST' })
    return r
  }

  return {
    transactions,
    reconciliations,
    loading,
    fetchTransactions,
    fetchReconciliations,
    processPayment,
    refundPayment,
    startReconciliation,
    completeReconciliation
  }
})

import { defineStore } from 'pinia'
import type { Account, JournalEntry, Invoice, TaxRecord, Employee, PayrollPeriod } from '~/shared/types/finance'

export const useFinanceStore = defineStore('finance', () => {
  const branchStore = useBranchStore()

  const accounts = ref<Account[]>([])
  const journalEntries = ref<JournalEntry[]>([])
  const invoices = ref<Invoice[]>([])
  const taxRecords = ref<TaxRecord[]>([])
  const employees = ref<Employee[]>([])
  const payrollPeriods = ref<PayrollPeriod[]>([])
  const loading = ref(false)

  async function fetchAccounts (tenantId: string) {
    loading.value = true
    try { accounts.value = await branchStore.$apiWithBranch<Account[]>(`/finance/accounts/by-tenant/${tenantId}`) } finally { loading.value = false }
  }
  async function createAccount (data: Partial<Account>) {
    const a = await branchStore.$apiWithBranch<Account>('/finance/accounts', { method: 'POST', body: data })
    accounts.value.push(a)
    return a
  }
  async function updateAccount (id: string, data: Partial<Account>) {
    const a = await branchStore.$apiWithBranch<Account>(`/finance/accounts/${id}`, { method: 'PUT', body: data })
    const idx = accounts.value.findIndex(x => x.id === id)
    if (idx >= 0) { accounts.value[idx] = a }
    return a
  }

  async function fetchJournalEntries (tenantId: string) {
    loading.value = true
    try { journalEntries.value = await branchStore.$apiWithBranch<JournalEntry[]>(`/finance/journal-entries/by-tenant/${tenantId}`) } finally { loading.value = false }
  }
  async function createJournalEntry (data: any) {
    const je = await branchStore.$apiWithBranch<JournalEntry>('/finance/journal-entries', { method: 'POST', body: data })
    journalEntries.value.push(je)
    return je
  }

  async function fetchInvoices (tenantId: string) {
    loading.value = true
    try { invoices.value = await branchStore.$apiWithBranch<Invoice[]>(`/finance/invoices/by-tenant/${tenantId}`) } finally { loading.value = false }
  }
  async function createInvoice (data: Partial<Invoice>) {
    const inv = await branchStore.$apiWithBranch<Invoice>('/finance/invoices/with-items', { method: 'POST', body: data })
    invoices.value.push(inv)
    return inv
  }
  async function payInvoice (id: string, amount: number) {
    const inv = await branchStore.$apiWithBranch<Invoice>(`/finance/invoices/${id}/pay`, { method: 'POST', body: { amount } })
    const idx = invoices.value.findIndex(i => i.id === id)
    if (idx >= 0) { invoices.value[idx] = inv }
    return inv
  }

  async function fetchTaxRecords (tenantId: string, taxType: string, year: number, month: number) {
    loading.value = true
    try { taxRecords.value = await branchStore.$apiWithBranch<TaxRecord[]>(`/finance/tax/records/${tenantId}/${taxType}/${year}/${month}`) } finally { loading.value = false }
  }
  async function createTaxRecord (data: Partial<TaxRecord>) {
    return await branchStore.$apiWithBranch<TaxRecord>('/finance/tax/records', { method: 'POST', body: data })
  }
  async function generateTaxReport (data: any) {
    return await branchStore.$apiWithBranch('/finance/tax/reports/generate', { method: 'POST', body: data })
  }

  async function fetchEmployees (tenantId: string) {
    loading.value = true
    try { employees.value = await branchStore.$apiWithBranch<Employee[]>(`/finance/employees/by-tenant/${tenantId}`) } finally { loading.value = false }
  }
  async function createEmployee (data: Partial<Employee>) {
    const e = await branchStore.$apiWithBranch<Employee>('/finance/employees', { method: 'POST', body: data })
    employees.value.push(e)
    return e
  }

  async function createPayrollPeriod (data: Partial<PayrollPeriod>) {
    const pp = await branchStore.$apiWithBranch<PayrollPeriod>('/finance/payroll/periods', { method: 'POST', body: data })
    payrollPeriods.value.push(pp)
    return pp
  }
  async function runPayroll (periodId: string) {
    return await branchStore.$apiWithBranch(`/finance/payroll/periods/${periodId}/run`, { method: 'POST' })
  }

  return {
    accounts,
    journalEntries,
    invoices,
    taxRecords,
    employees,
    payrollPeriods,
    loading,
    fetchAccounts,
    createAccount,
    updateAccount,
    fetchJournalEntries,
    createJournalEntry,
    fetchInvoices,
    createInvoice,
    payInvoice,
    fetchTaxRecords,
    createTaxRecord,
    generateTaxReport,
    fetchEmployees,
    createEmployee,
    createPayrollPeriod,
    runPayroll
  }
})

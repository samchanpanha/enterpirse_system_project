export interface Account {
  id: string; tenantId: string; code: string; name: string
  type: string; active: boolean; contra: boolean; parentId?: string
}
export interface JournalEntryLine {
  id: string; journalEntryId: string; accountId: string
  debit: number; credit: number; description?: string
}
export interface JournalEntry {
  id: string; tenantId: string; entryNumber: string
  entryDate: string; description: string
  referenceType?: string; referenceId?: string
  posted: boolean; postedAt?: string; createdBy?: string
  lines?: JournalEntryLine[]
}
export interface InvoiceItemInput {
  description: string
  quantity: number
  unitPrice: number
  taxRate?: number
  taxAmount?: number
  total?: number
  accountId?: string
}
export interface Invoice {
  id: string; tenantId?: string; invoiceNumber: string
  invoiceType: string; sourceType?: string; sourceId?: string
  customerName: string; customerTin?: string
  issueDate: string; dueDate: string
  subtotal: number; discount?: number; taxAmount: number
  total: number; amountPaid: number; balanceDue: number
  status: string; currency?: string; notes?: string
  items?: InvoiceItemInput[]
}
export interface TaxRecord {
  id: string; tenantId: string; taxType: string
  periodMonth: number; periodYear: number
  taxableAmount: number; taxRate: number; taxAmount: number
  sourceType?: string; sourceId?: string
}
export interface TaxFilingReport {
  id: string; tenantId: string; taxType: string
  periodMonth: number; periodYear: number
  totalTax: number; status: string; filedDate?: string
  referenceNumber?: string; exportUrl?: string
}
export interface Employee {
  id: string; tenantId: string; code?: string
  firstName: string; lastName: string; khmerName?: string
  gender?: string; birthDate?: string
  phone?: string; email?: string
  position?: string; department?: string
  hireDate?: string; status: string
  baseSalary: number; bankAccount?: string; bankName?: string
  nssfNumber?: string; taxDependents: number
}
export interface PayrollPeriod {
  id: string; tenantId: string
  periodMonth: number; periodYear: number
  startDate: string; endDate: string; paymentDate?: string
  status: string
}
export interface PayrollItem {
  id: string; payrollPeriodId: string; employeeId: string
  baseSalary: number; allowances: number; overtimeAmount: number
  grossSalary: number; tosAmount: number; tofbAmount: number
  nssfEmployee: number; nssfEmployer: number
  deductions: number; netSalary: number
}

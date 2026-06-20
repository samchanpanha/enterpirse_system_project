export interface Payroll {
  id: string;
  payrollNumber: string;
  payPeriodStart: string;
  payPeriodEnd: string;
  payDate: string;
  status: 'DRAFT' | 'PROCESSED' | 'PAID' | 'CANCELLED';
  totalGross: number;
  totalDeductions: number;
  totalNet: number;
  employeeCount: number;
  entries: PayrollEntry[];
  createdAt: string;
}

export interface PayrollEntry {
  id: string;
  employeeId: string;
  employeeName: string;
  grossPay: number;
  deductions: number;
  netPay: number;
  taxWithheld: number;
  notes?: string;
}

export interface PayrollRequest {
  payPeriodStart: string;
  payPeriodEnd: string;
  payDate: string;
  entries: { employeeId: string; grossPay: number; deductions: number; notes?: string }[];
}

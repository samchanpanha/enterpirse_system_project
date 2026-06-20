export interface Report {
  id: string;
  reportName: string;
  reportType: 'FINANCIAL' | 'OPERATIONAL' | 'SALES' | 'INVENTORY' | 'CUSTOM';
  format: 'PDF' | 'CSV' | 'XLSX' | 'HTML';
  status: 'DRAFT' | 'GENERATED' | 'SCHEDULED' | 'FAILED';
  parameters?: Record<string, string>;
  generatedAt?: string;
  fileUrl?: string;
  schedule?: string;
  createdAt: string;
}

export interface ReportRequest {
  reportName: string;
  reportType: Report['reportType'];
  format: Report['format'];
  parameters?: Record<string, string>;
}

export interface DashboardSummary {
  totalRevenue: number;
  totalExpenses: number;
  netIncome: number;
  openInvoices: number;
  overdueInvoices: number;
  activeProperties: number;
  pendingOrders: number;
  lowStockProducts: number;
  periodStart: string;
  periodEnd: string;
}

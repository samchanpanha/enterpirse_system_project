export interface Invoice {
  id: string;
  invoiceNumber: string;
  invoiceDate: string;
  dueDate: string;
  status: 'DRAFT' | 'SENT' | 'PAID' | 'OVERDUE' | 'CANCELLED';
  type: 'SALE' | 'PURCHASE';
  customerName?: string;
  vendorName?: string;
  subtotal: number;
  taxTotal: number;
  total: number;
  paidAmount: number;
  notes?: string;
  items: InvoiceLine[];
  createdAt: string;
}

export interface InvoiceLine {
  id: string;
  description: string;
  quantity: number;
  unitPrice: number;
  total: number;
  taxRate?: number;
  taxAmount?: number;
}

export interface InvoiceRequest {
  invoiceDate: string;
  dueDate: string;
  type: 'SALE' | 'PURCHASE';
  customerName?: string;
  vendorName?: string;
  notes?: string;
  items: { description: string; quantity: number; unitPrice: number; taxRate?: number }[];
}

export interface InvoicePaymentRequest {
  amount: number;
  paymentDate: string;
  notes?: string;
}

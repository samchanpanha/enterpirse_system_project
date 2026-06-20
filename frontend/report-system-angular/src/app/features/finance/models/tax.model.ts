export interface Tax {
  id: string;
  taxName: string;
  taxRate: number;
  taxType: 'SALES' | 'VAT' | 'WITHHOLDING' | 'OTHER';
  active: boolean;
  description?: string;
  createdAt: string;
}

export interface TaxRequest {
  taxName: string;
  taxRate: number;
  taxType: Tax['taxType'];
  description?: string;
}

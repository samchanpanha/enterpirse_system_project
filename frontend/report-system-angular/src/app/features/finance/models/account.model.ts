export interface Account {
  id: string;
  accountCode: string;
  accountName: string;
  accountType: 'ASSET' | 'LIABILITY' | 'EQUITY' | 'REVENUE' | 'EXPENSE';
  parentId?: string;
  description?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AccountRequest {
  accountCode: string;
  accountName: string;
  accountType: Account['accountType'];
  parentId?: string;
  description?: string;
}

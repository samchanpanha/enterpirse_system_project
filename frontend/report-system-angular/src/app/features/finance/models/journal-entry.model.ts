export interface JournalEntry {
  id: string;
  entryNumber: string;
  entryDate: string;
  description: string;
  status: 'DRAFT' | 'POSTED' | 'REVERSED';
  totalDebit: number;
  totalCredit: number;
  lines: JournalLine[];
  createdAt: string;
}

export interface JournalLine {
  id: string;
  accountId: string;
  accountCode: string;
  accountName: string;
  debit: number;
  credit: number;
  description?: string;
}

export interface JournalEntryRequest {
  entryDate: string;
  description: string;
  lines: { accountId: string; debit: number; credit: number; description?: string }[];
}

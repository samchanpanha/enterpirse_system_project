import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { BranchService } from '../../../core/services/branch.service';
import { Account, AccountRequest } from '../models/account.model';
import { JournalEntry, JournalEntryRequest } from '../models/journal-entry.model';
import { Invoice, InvoiceRequest, InvoicePaymentRequest } from '../models/invoice.model';
import { Tax, TaxRequest } from '../models/tax.model';
import { Employee, EmployeeRequest } from '../models/employee.model';
import { Payroll, PayrollRequest } from '../models/payroll.model';

@Injectable({ providedIn: 'root' })
export class FinanceApiService {
  private base = `${environment.apiUrl}/finance`;

  constructor(
    private http: HttpClient,
    private branch: BranchService,
  ) {}

  private get tenantId() {
    return this.branch.tenantId;
  }

  getAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.base}/accounts/by-tenant/${this.tenantId}`);
  }

  getAccount(id: string): Observable<Account> {
    return this.http.get<Account>(`${this.base}/accounts/${id}`);
  }

  createAccount(data: AccountRequest): Observable<Account> {
    return this.http.post<Account>(`${this.base}/accounts`, { ...data, tenantId: this.tenantId });
  }

  updateAccount(id: string, data: Partial<AccountRequest>): Observable<Account> {
    return this.http.put<Account>(`${this.base}/accounts/${id}`, data);
  }

  getJournalEntries(): Observable<JournalEntry[]> {
    return this.http.get<JournalEntry[]>(`${this.base}/journal-entries/by-tenant/${this.tenantId}`);
  }

  getJournalEntry(id: string): Observable<JournalEntry> {
    return this.http.get<JournalEntry>(`${this.base}/journal-entries/${id}`);
  }

  createJournalEntry(data: JournalEntryRequest): Observable<JournalEntry> {
    return this.http.post<JournalEntry>(`${this.base}/journal-entries`, { ...data, tenantId: this.tenantId });
  }

  postJournalEntry(id: string): Observable<JournalEntry> {
    return this.http.post<JournalEntry>(`${this.base}/journal-entries/${id}/post`, {});
  }

  reverseJournalEntry(id: string): Observable<JournalEntry> {
    return this.http.post<JournalEntry>(`${this.base}/journal-entries/${id}/reverse`, {});
  }

  getInvoices(type?: string): Observable<Invoice[]> {
    const params = type ? `?type=${type}` : '';
    return this.http.get<Invoice[]>(`${this.base}/invoices/by-tenant/${this.tenantId}${params}`);
  }

  getInvoice(id: string): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.base}/invoices/${id}`);
  }

  createInvoice(data: InvoiceRequest): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.base}/invoices`, { ...data, tenantId: this.tenantId });
  }

  recordInvoicePayment(id: string, data: InvoicePaymentRequest): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.base}/invoices/${id}/pay`, data);
  }

  cancelInvoice(id: string): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.base}/invoices/${id}/cancel`, {});
  }

  getTaxes(): Observable<Tax[]> {
    return this.http.get<Tax[]>(`${this.base}/taxes/by-tenant/${this.tenantId}`);
  }

  createTax(data: TaxRequest): Observable<Tax> {
    return this.http.post<Tax>(`${this.base}/taxes`, { ...data, tenantId: this.tenantId });
  }

  updateTax(id: string, data: Partial<TaxRequest>): Observable<Tax> {
    return this.http.put<Tax>(`${this.base}/taxes/${id}`, data);
  }

  getEmployees(): Observable<Employee[]> {
    return this.http.get<Employee[]>(`${this.base}/employees/by-tenant/${this.tenantId}`);
  }

  getEmployee(id: string): Observable<Employee> {
    return this.http.get<Employee>(`${this.base}/employees/${id}`);
  }

  createEmployee(data: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>(`${this.base}/employees`, { ...data, tenantId: this.tenantId });
  }

  updateEmployee(id: string, data: Partial<EmployeeRequest>): Observable<Employee> {
    return this.http.put<Employee>(`${this.base}/employees/${id}`, data);
  }

  getPayrolls(): Observable<Payroll[]> {
    return this.http.get<Payroll[]>(`${this.base}/payroll/by-tenant/${this.tenantId}`);
  }

  getPayroll(id: string): Observable<Payroll> {
    return this.http.get<Payroll>(`${this.base}/payroll/${id}`);
  }

  createPayroll(data: PayrollRequest): Observable<Payroll> {
    return this.http.post<Payroll>(`${this.base}/payroll`, { ...data, tenantId: this.tenantId });
  }

  processPayroll(id: string): Observable<Payroll> {
    return this.http.post<Payroll>(`${this.base}/payroll/${id}/process`, {});
  }

  payPayroll(id: string): Observable<Payroll> {
    return this.http.post<Payroll>(`${this.base}/payroll/${id}/pay`, {});
  }

  cancelPayroll(id: string): Observable<Payroll> {
    return this.http.post<Payroll>(`${this.base}/payroll/${id}/cancel`, {});
  }
}

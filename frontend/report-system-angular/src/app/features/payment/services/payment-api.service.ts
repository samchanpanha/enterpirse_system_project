import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { BranchService } from '../../../core/services/branch.service';
import { PaymentTransaction, ReconciliationRequest } from '../models/payment.model';

@Injectable({ providedIn: 'root' })
export class PaymentApiService {
  private base = `${environment.apiUrl}/payment`;

  constructor(
    private http: HttpClient,
    private branch: BranchService,
  ) {}

  private get tenantId() {
    return this.branch.tenantId;
  }

  getTransactions(status?: string): Observable<PaymentTransaction[]> {
    const params = status ? `?status=${status}` : '';
    return this.http.get<PaymentTransaction[]>(`${this.base}/transactions/by-tenant/${this.tenantId}${params}`);
  }

  getTransaction(id: string): Observable<PaymentTransaction> {
    return this.http.get<PaymentTransaction>(`${this.base}/transactions/${id}`);
  }

  reconcile(data: ReconciliationRequest): Observable<any> {
    return this.http.post(`${this.base}/transactions/reconcile`, { ...data, tenantId: this.tenantId });
  }

  refundTransaction(id: string): Observable<PaymentTransaction> {
    return this.http.post<PaymentTransaction>(`${this.base}/transactions/${id}/refund`, {});
  }

  cancelTransaction(id: string): Observable<PaymentTransaction> {
    return this.http.post<PaymentTransaction>(`${this.base}/transactions/${id}/cancel`, {});
  }
}

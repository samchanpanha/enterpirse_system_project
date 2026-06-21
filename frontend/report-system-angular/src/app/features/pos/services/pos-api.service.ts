import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { BranchService } from '../../../core/services/branch.service';
import { PosCart, KdsOrder, PosReport, PosCartRequest } from '../models/pos.model';

@Injectable({ providedIn: 'root' })
export class PosApiService {
  private base = `${environment.apiUrl}/pos`;

  constructor(private http: HttpClient, private branch: BranchService) {}

  private get tenantId() { return this.branch.tenantId; }

  // Cart operations
  getActiveCart(outletId: string): Observable<PosCart> {
    return this.http.get<PosCart>(`${this.base}/cart/${outletId}`);
  }

  addToCart(data: PosCartRequest): Observable<PosCart> {
    return this.http.post<PosCart>(`${this.base}/cart`, { ...data, tenantId: this.tenantId });
  }

  updateCartItem(cartId: string, itemId: string, quantity: number): Observable<PosCart> {
    return this.http.put<PosCart>(`${this.base}/cart/${cartId}/items/${itemId}`, { quantity });
  }

  removeCartItem(cartId: string, itemId: string): Observable<PosCart> {
    return this.http.delete<PosCart>(`${this.base}/cart/${cartId}/items/${itemId}`);
  }

  submitOrder(cartId: string, data: { paymentMethod: string; discount?: number; serviceCharge?: number }): Observable<any> {
    return this.http.post(`${this.base}/cart/${cartId}/submit`, data);
  }

  // KDS
  getKdsOrders(outletId: string): Observable<KdsOrder[]> {
    return this.http.get<KdsOrder[]>(`${this.base}/kds/${outletId}`);
  }

  updateKdsItemStatus(orderId: string, itemIndex: number, status: string): Observable<any> {
    return this.http.put(`${this.base}/kds/${orderId}/items/${itemIndex}`, { status });
  }

  // Reports
  getDailyReport(outletId: string, date: string): Observable<PosReport> {
    return this.http.get<PosReport>(`${this.base}/reports/${outletId}?date=${date}`);
  }
}

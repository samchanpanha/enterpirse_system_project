import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { BranchService } from '../../../core/services/branch.service';

@Injectable({ providedIn: 'root' })
export class DashboardApiService {
  private base = `${environment.apiUrl}`;

  constructor(
    private http: HttpClient,
    private branch: BranchService,
  ) {}

  private get tenantId() {
    return this.branch.tenantId;
  }

  getPropertyCount(): Observable<number> {
    return this.http
      .get<any>(`${this.base}/property/properties/by-tenant/${this.tenantId}`)
      .pipe(
        map((res) => (Array.isArray(res) ? res.length : (res?.count ?? 0))),
        catchError(() => of(0)),
      );
  }

  getInventoryCount(): Observable<number> {
    return this.http
      .get<any>(`${this.base}/inventory/products/by-tenant/${this.tenantId}`)
      .pipe(
        map((res) => (Array.isArray(res) ? res.length : (res?.count ?? 0))),
        catchError(() => of(0)),
      );
  }

  getFinanceRevenue(): Observable<number> {
    return this.http
      .get<any>(`${this.base}/finance/revenue/by-tenant/${this.tenantId}`)
      .pipe(
        map((res) => (res?.revenue ?? res?.total ?? 0)),
        catchError(() => of(0)),
      );
  }

  getPaymentPending(): Observable<number> {
    return this.http
      .get<any>(`${this.base}/payment/payments/pending/by-tenant/${this.tenantId}`)
      .pipe(
        map((res) => (Array.isArray(res) ? res.length : (res?.count ?? 0))),
        catchError(() => of(0)),
      );
  }

  getRestaurantOrderCount(): Observable<number> {
    return this.http
      .get<any>(`${this.base}/restaurant/orders/by-tenant/${this.tenantId}`)
      .pipe(
        map((res) => (Array.isArray(res) ? res.length : (res?.count ?? 0))),
        catchError(() => of(0)),
      );
  }

  getDeliveryCount(): Observable<number> {
    return this.http
      .get<any>(`${this.base}/delivery/deliveries/by-tenant/${this.tenantId}`)
      .pipe(
        map((res) => (Array.isArray(res) ? res.length : (res?.count ?? 0))),
        catchError(() => of(0)),
      );
  }

  getListingCount(): Observable<number> {
    return this.http
      .get<any>(`${this.base}/realty/listings/by-tenant/${this.tenantId}`)
      .pipe(
        map((res) => (Array.isArray(res) ? res.length : (res?.count ?? 0))),
        catchError(() => of(0)),
      );
  }

  getInvoiceCount(): Observable<number> {
    return this.http
      .get<any>(`${this.base}/finance/invoices/by-tenant/${this.tenantId}`)
      .pipe(
        map((res) => (Array.isArray(res) ? res.length : (res?.count ?? 0))),
        catchError(() => of(0)),
      );
  }

  getLeaseCount(): Observable<number> {
    return this.http
      .get<any>(`${this.base}/property/leases/active/by-tenant/${this.tenantId}`)
      .pipe(
        map((res) => (Array.isArray(res) ? res.length : (res?.count ?? 0))),
        catchError(() => of(0)),
      );
  }

  getReportCount(): Observable<number> {
    return this.http
      .get<any>(`${this.base}/reporting/reports/definitions/by-tenant/${this.tenantId}`)
      .pipe(
        map((res) => (Array.isArray(res) ? res.length : (res?.count ?? 0))),
        catchError(() => of(0)),
      );
  }
}

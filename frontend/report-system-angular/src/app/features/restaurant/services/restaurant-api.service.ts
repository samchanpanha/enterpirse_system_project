import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { BranchService } from '../../../core/services/branch.service';
import { Outlet } from '../models/outlet.model';
import { Category, MenuItem, CategoryRequest, MenuItemRequest } from '../models/menu.model';
import { Order, OrderItem, OrderCompleteRequest } from '../models/order.model';
import { Customer } from '../models/customer.model';
import { Reservation, ReservationRequest } from '../models/reservation.model';

@Injectable({ providedIn: 'root' })
export class RestaurantApiService {
  private base = `${environment.apiUrl}/restaurant`;

  constructor(
    private http: HttpClient,
    private branch: BranchService,
  ) {}

  private get tenantId() {
    return this.branch.tenantId;
  }

  getOutlets(): Observable<Outlet[]> {
    return this.http.get<Outlet[]>(`${this.base}/outlets/by-tenant/${this.tenantId}`);
  }

  getOutlet(id: string): Observable<Outlet> {
    return this.http.get<Outlet>(`${this.base}/outlets/${id}`);
  }

  createOutlet(data: Outlet): Observable<Outlet> {
    return this.http.post<Outlet>(`${this.base}/outlets`, { ...data, tenantId: this.tenantId });
  }

  getCategories(outletId: string): Observable<Category[]> {
    return this.http.get<Category[]>(
      `${this.base}/menu/categories?tenantId=${this.tenantId}&outletId=${outletId}`,
    );
  }

  createCategory(data: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(`${this.base}/menu/categories`, { ...data, tenantId: this.tenantId });
  }

  getMenuItems(categoryId: string): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.base}/menu/items/by-category/${categoryId}`);
  }

  getActiveMenuItems(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.base}/menu/items/active/${this.tenantId}`);
  }

  createMenuItem(data: MenuItemRequest): Observable<MenuItem> {
    return this.http.post<MenuItem>(`${this.base}/menu/items`, { ...data, tenantId: this.tenantId });
  }

  updateMenuItem(id: string, data: Partial<MenuItemRequest>): Observable<MenuItem> {
    return this.http.put<MenuItem>(`${this.base}/menu/items/${id}`, data);
  }

  deleteMenuItem(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/menu/items/${id}`);
  }

  getOrdersByOutlet(outletId: string, status = 'pending'): Observable<Order[]> {
    return this.http.get<Order[]>(
      `${this.base}/orders/by-outlet/${outletId}?status=${status}`,
    );
  }

  getOrder(id: string): Observable<Order> {
    return this.http.get<Order>(`${this.base}/orders/${id}`);
  }

  getOrderItems(orderId: string): Observable<OrderItem[]> {
    return this.http.get<OrderItem[]>(`${this.base}/orders/${orderId}/items`);
  }

  updateOrderStatus(id: string, status: string): Observable<Order> {
    return this.http.put<Order>(`${this.base}/orders/${id}/status`, { status });
  }

  completeOrder(id: string, data: OrderCompleteRequest): Observable<Order> {
    return this.http.post<Order>(`${this.base}/orders/${id}/complete`, data);
  }

  getCustomersByOutlet(outletId: string): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.base}/customers/by-outlet/${outletId}`);
  }

  getCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.base}/customers/by-tenant/${this.tenantId}`);
  }

  createCustomer(data: Customer): Observable<Customer> {
    return this.http.post<Customer>(`${this.base}/customers`, { ...data, tenantId: this.tenantId });
  }

  getReservationsByOutlet(outletId: string, date?: string): Observable<Reservation[]> {
    const params = date ? `?date=${date}` : '';
    return this.http.get<Reservation[]>(
      `${this.base}/reservations/by-outlet/${outletId}${params}`,
    );
  }

  getReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.base}/reservations/by-tenant/${this.tenantId}`);
  }

  createReservation(data: ReservationRequest): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.base}/reservations`, { ...data, tenantId: this.tenantId });
  }

  updateReservationStatus(id: string, status: string): Observable<Reservation> {
    return this.http.put<Reservation>(`${this.base}/reservations/${id}/status`, { status });
  }

  cancelReservation(id: string): Observable<void> {
    return this.http.post<void>(`${this.base}/reservations/${id}/cancel`, {});
  }
}

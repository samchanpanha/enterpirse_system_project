import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { BranchService } from '../../../core/services/branch.service';
import { Product, ProductRequest } from '../models/product.model';
import { Supplier, SupplierRequest } from '../models/supplier.model';
import { PurchaseOrder, PurchaseOrderItemRequest } from '../models/purchase-order.model';
import { StockTransfer, StockTransferRequest } from '../models/stock-transfer.model';

@Injectable({ providedIn: 'root' })
export class InventoryApiService {
  private base = `${environment.apiUrl}/inventory`;

  constructor(
    private http: HttpClient,
    private branch: BranchService,
  ) {}

  private get tenantId() {
    return this.branch.tenantId;
  }

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.base}/products/by-tenant/${this.tenantId}`);
  }

  getProduct(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.base}/products/${id}`);
  }

  createProduct(data: ProductRequest): Observable<Product> {
    return this.http.post<Product>(`${this.base}/products`, { ...data, tenantId: this.tenantId });
  }

  updateProduct(id: string, data: Partial<ProductRequest>): Observable<Product> {
    return this.http.put<Product>(`${this.base}/products/${id}`, data);
  }

  getSuppliers(): Observable<Supplier[]> {
    return this.http.get<Supplier[]>(`${this.base}/suppliers/by-tenant/${this.tenantId}`);
  }

  getSupplier(id: string): Observable<Supplier> {
    return this.http.get<Supplier>(`${this.base}/suppliers/${id}`);
  }

  createSupplier(data: SupplierRequest): Observable<Supplier> {
    return this.http.post<Supplier>(`${this.base}/suppliers`, { ...data, tenantId: this.tenantId });
  }

  updateSupplier(id: string, data: Partial<SupplierRequest>): Observable<Supplier> {
    return this.http.put<Supplier>(`${this.base}/suppliers/${id}`, data);
  }

  deleteSupplier(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/suppliers/${id}`);
  }

  getStockLevel(productId: string): Observable<number> {
    return this.http.get<number>(
      `${this.base}/stock/current/${this.tenantId}/${productId}`,
    );
  }

  addStockEntry(data: {
    warehouseId: string;
    productId: string;
    quantity: number;
    unitCost?: number;
    batchNumber?: string;
    notes?: string;
  }): Observable<any> {
    return this.http.post(`${this.base}/stock/entry`, { ...data, tenantId: this.tenantId });
  }

  getPurchaseOrders(): Observable<PurchaseOrder[]> {
    return this.http.get<PurchaseOrder[]>(
      `${this.base}/purchase-orders/by-tenant/${this.tenantId}`,
    );
  }

  getPurchaseOrder(id: string): Observable<PurchaseOrder> {
    return this.http.get<PurchaseOrder>(`${this.base}/purchase-orders/${id}`);
  }

  createPurchaseOrder(data: { supplierId: string; orderDate: string }): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder>(`${this.base}/purchase-orders`, {
      ...data,
      tenantId: this.tenantId,
    });
  }

  addPurchaseOrderItem(poId: string, data: PurchaseOrderItemRequest): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder>(`${this.base}/purchase-orders/${poId}/items`, data);
  }

  receivePurchaseOrder(poId: string): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder>(`${this.base}/purchase-orders/${poId}/receive`, {});
  }

  cancelPurchaseOrder(poId: string): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder>(`${this.base}/purchase-orders/${poId}/cancel`, {});
  }

  getTransfers(): Observable<StockTransfer[]> {
    return this.http.get<StockTransfer[]>(
      `${this.base}/transfers?tenantId=${this.tenantId}`,
    );
  }

  getTransfer(id: string): Observable<StockTransfer> {
    return this.http.get<StockTransfer>(`${this.base}/transfers/${id}`);
  }

  createTransfer(data: StockTransferRequest): Observable<StockTransfer> {
    return this.http.post<StockTransfer>(`${this.base}/transfers`, {
      ...data,
      tenantId: this.tenantId,
    });
  }

  shipTransfer(id: string): Observable<StockTransfer> {
    return this.http.post<StockTransfer>(`${this.base}/transfers/${id}/ship`, {});
  }

  receiveTransfer(id: string): Observable<StockTransfer> {
    return this.http.post<StockTransfer>(`${this.base}/transfers/${id}/receive`, {});
  }

  cancelTransfer(id: string): Observable<StockTransfer> {
    return this.http.post<StockTransfer>(`${this.base}/transfers/${id}/cancel`, {});
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { BranchService } from '../../../core/services/branch.service';
import { Delivery, Driver, FleetVehicle, DeliveryZone, DriverPayout, DeliveryRequest, DriverRequest, FleetRequest, ZoneRequest } from '../models/delivery.model';

@Injectable({ providedIn: 'root' })
export class DeliveryApiService {
  private base = `${environment.apiUrl}/delivery`;

  constructor(private http: HttpClient, private branch: BranchService) {}

  private get tenantId() { return this.branch.tenantId; }

  // Deliveries
  getDeliveries(): Observable<Delivery[]> {
    return this.http.get<Delivery[]>(`${this.base}/deliveries/by-tenant/${this.tenantId}`);
  }

  getDelivery(id: string): Observable<Delivery> {
    return this.http.get<Delivery>(`${this.base}/deliveries/${id}`);
  }

  createDelivery(data: DeliveryRequest): Observable<Delivery> {
    return this.http.post<Delivery>(`${this.base}/deliveries`, { ...data, tenantId: this.tenantId });
  }

  updateDeliveryStatus(id: string, status: string): Observable<Delivery> {
    return this.http.put<Delivery>(`${this.base}/deliveries/${id}/status`, { status });
  }

  // Drivers
  getDrivers(): Observable<Driver[]> {
    return this.http.get<Driver[]>(`${this.base}/drivers/by-tenant/${this.tenantId}`);
  }

  getDriver(id: string): Observable<Driver> {
    return this.http.get<Driver>(`${this.base}/drivers/${id}`);
  }

  createDriver(data: DriverRequest): Observable<Driver> {
    return this.http.post<Driver>(`${this.base}/drivers`, { ...data, tenantId: this.tenantId });
  }

  updateDriver(id: string, data: Partial<DriverRequest>): Observable<Driver> {
    return this.http.put<Driver>(`${this.base}/drivers/${id}`, data);
  }

  // Fleet
  getFleet(): Observable<FleetVehicle[]> {
    return this.http.get<FleetVehicle[]>(`${this.base}/fleet-vehicles/by-tenant/${this.tenantId}`);
  }

  getVehicle(id: string): Observable<FleetVehicle> {
    return this.http.get<FleetVehicle>(`${this.base}/fleet-vehicles/${id}`);
  }

  createVehicle(data: FleetRequest): Observable<FleetVehicle> {
    return this.http.post<FleetVehicle>(`${this.base}/fleet-vehicles`, { ...data, tenantId: this.tenantId });
  }

  updateVehicle(id: string, data: Partial<FleetRequest>): Observable<FleetVehicle> {
    return this.http.put<FleetVehicle>(`${this.base}/fleet-vehicles/${id}`, data);
  }

  // Zones
  getZones(): Observable<DeliveryZone[]> {
    return this.http.get<DeliveryZone[]>(`${this.base}/delivery-zones/by-tenant/${this.tenantId}`);
  }

  createZone(data: ZoneRequest): Observable<DeliveryZone> {
    return this.http.post<DeliveryZone>(`${this.base}/delivery-zones`, { ...data, tenantId: this.tenantId });
  }

  // Payouts
  getPayouts(): Observable<DriverPayout[]> {
    return this.http.get<DriverPayout[]>(`${this.base}/driver-payouts/by-tenant/${this.tenantId}`);
  }
}

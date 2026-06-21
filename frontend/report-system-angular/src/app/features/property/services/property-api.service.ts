import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { BranchService } from '../../../core/services/branch.service';
import { Property, PropertyRequest } from '../models/property.model';
import { Unit, UnitRequest } from '../models/unit.model';
import { Lease, LeaseRequest, LeaseRenewRequest } from '../models/lease.model';
import { Schedule, ScheduleRequest } from '../models/schedule.model';
import {
  MaintenanceTicket,
  MaintenanceRequest,
  MaintenanceStatusRequest,
  MaintenanceAssignRequest,
  MaintenanceCompleteRequest,
} from '../models/maintenance.model';

@Injectable({ providedIn: 'root' })
export class PropertyApiService {
  private base = `${environment.apiUrl}/property`;

  constructor(
    private http: HttpClient,
    private branch: BranchService,
  ) {}

  private get tenantId() {
    return this.branch.tenantId;
  }

  getProperties(): Observable<Property[]> {
    return this.http.get<Property[]>(
      `${this.base}/properties/by-tenant/${this.tenantId}`,
    );
  }

  getProperty(id: string): Observable<Property> {
    return this.http.get<Property>(`${this.base}/properties/${id}`);
  }

  createProperty(data: PropertyRequest): Observable<Property> {
    return this.http.post<Property>(`${this.base}/properties`, {
      ...data,
      tenantId: this.tenantId,
    });
  }

  updateProperty(id: string, data: Partial<PropertyRequest>): Observable<Property> {
    return this.http.put<Property>(`${this.base}/properties/${id}`, data);
  }

  deleteProperty(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/properties/${id}`);
  }

  getUnitsByProperty(propertyId: string): Observable<Unit[]> {
    return this.http.get<Unit[]>(
      `${this.base}/units/by-property/${propertyId}`,
    );
  }

  getUnit(id: string): Observable<Unit> {
    return this.http.get<Unit>(`${this.base}/units/${id}`);
  }

  createUnit(data: UnitRequest): Observable<Unit> {
    return this.http.post<Unit>(`${this.base}/units`, {
      ...data,
      tenantId: this.tenantId,
    });
  }

  updateUnit(id: string, data: Partial<UnitRequest>): Observable<Unit> {
    return this.http.put<Unit>(`${this.base}/units/${id}`, data);
  }

  deleteUnit(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/units/${id}`);
  }

  getLeasesByUnit(unitId: string): Observable<Lease[]> {
    return this.http.get<Lease[]>(`${this.base}/leases/by-unit/${unitId}`);
  }

  getLease(id: string): Observable<Lease> {
    return this.http.get<Lease>(`${this.base}/leases/${id}`);
  }

  getActiveLeases(): Observable<Lease[]> {
    return this.http.get<Lease[]>(
      `${this.base}/leases/active/by-tenant/${this.tenantId}`,
    );
  }

  createLease(data: LeaseRequest): Observable<Lease> {
    return this.http.post<Lease>(`${this.base}/leases`, {
      ...data,
      tenantId: this.tenantId,
    });
  }

  terminateLease(id: string): Observable<Lease> {
    return this.http.post<Lease>(`${this.base}/leases/${id}/terminate`, {});
  }

  renewLease(id: string, data: LeaseRenewRequest): Observable<Lease> {
    return this.http.post<Lease>(`${this.base}/leases/${id}/renew`, data);
  }

  getSchedulesByUnit(unitId: string): Observable<Schedule[]> {
    return this.http.get<Schedule[]>(
      `${this.base}/schedules/by-unit/${unitId}`,
    );
  }

  createSchedule(data: ScheduleRequest): Observable<Schedule> {
    return this.http.post<Schedule>(`${this.base}/schedules`, {
      ...data,
      tenantId: this.tenantId,
    });
  }

  updateSchedule(id: string, data: Partial<ScheduleRequest>): Observable<Schedule> {
    return this.http.put<Schedule>(`${this.base}/schedules/${id}`, data);
  }

  deleteSchedule(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/schedules/${id}`);
  }

  getTicketsByProperty(propertyId: string): Observable<MaintenanceTicket[]> {
    return this.http.get<MaintenanceTicket[]>(
      `${this.base}/maintenance/by-property/${propertyId}`,
    );
  }

  createTicket(data: MaintenanceRequest): Observable<MaintenanceTicket> {
    return this.http.post<MaintenanceTicket>(`${this.base}/maintenance`, {
      ...data,
      tenantId: this.tenantId,
    });
  }

  updateTicketStatus(
    id: string,
    data: MaintenanceStatusRequest,
  ): Observable<MaintenanceTicket> {
    return this.http.put<MaintenanceTicket>(
      `${this.base}/maintenance/${id}/status`,
      data,
    );
  }

  assignTicket(
    id: string,
    data: MaintenanceAssignRequest,
  ): Observable<MaintenanceTicket> {
    return this.http.post<MaintenanceTicket>(
      `${this.base}/maintenance/${id}/assign`,
      data,
    );
  }

  completeTicket(
    id: string,
    data?: MaintenanceCompleteRequest,
  ): Observable<MaintenanceTicket> {
    return this.http.post<MaintenanceTicket>(
      `${this.base}/maintenance/${id}/complete`,
      data ?? {},
    );
  }

  getMaintenanceTickets(): Observable<MaintenanceTicket[]> {
    return this.http.get<MaintenanceTicket[]>(
      `${this.base}/maintenance/by-tenant/${this.tenantId}`,
    );
  }

  createMaintenanceTicket(data: any): Observable<MaintenanceTicket> {
    return this.http.post<MaintenanceTicket>(`${this.base}/maintenance`, {
      ...data,
      tenantId: this.tenantId,
    });
  }

  updateMaintenanceStatus(id: string, status: string): Observable<MaintenanceTicket> {
    return this.http.put<MaintenanceTicket>(
      `${this.base}/maintenance/${id}/status`,
      { status },
    );
  }
}

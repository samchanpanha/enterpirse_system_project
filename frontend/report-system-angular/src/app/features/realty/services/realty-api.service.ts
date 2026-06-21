import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { BranchService } from '../../../core/services/branch.service';
import { Listing, Agent, Lead, Resident, AgentRequest, LeadRequest, ListingRequest } from '../models/realty.model';

@Injectable({ providedIn: 'root' })
export class RealtyApiService {
  private base = `${environment.apiUrl}/realty`;

  constructor(private http: HttpClient, private branch: BranchService) {}

  private get tenantId() { return this.branch.tenantId; }

  // Listings
  getListings(): Observable<Listing[]> {
    return this.http.get<Listing[]>(`${this.base}/listings/by-tenant/${this.tenantId}`);
  }
  getListing(id: string): Observable<Listing> {
    return this.http.get<Listing>(`${this.base}/listings/${id}`);
  }
  createListing(data: ListingRequest): Observable<Listing> {
    return this.http.post<Listing>(`${this.base}/listings`, { ...data, tenantId: this.tenantId });
  }
  updateListing(id: string, data: Partial<Listing>): Observable<Listing> {
    return this.http.put<Listing>(`${this.base}/listings/${id}`, data);
  }
  deleteListing(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/listings/${id}`);
  }

  // Agents
  getAgents(): Observable<Agent[]> {
    return this.http.get<Agent[]>(`${this.base}/agents/by-tenant/${this.tenantId}`);
  }
  createAgent(data: AgentRequest): Observable<Agent> {
    return this.http.post<Agent>(`${this.base}/agents`, { ...data, tenantId: this.tenantId });
  }
  updateAgent(id: string, data: Partial<AgentRequest>): Observable<Agent> {
    return this.http.put<Agent>(`${this.base}/agents/${id}`, data);
  }

  // Leads
  getLeads(): Observable<Lead[]> {
    return this.http.get<Lead[]>(`${this.base}/leads/by-tenant/${this.tenantId}`);
  }
  createLead(data: LeadRequest): Observable<Lead> {
    return this.http.post<Lead>(`${this.base}/leads`, { ...data, tenantId: this.tenantId });
  }
  updateLead(id: string, data: Partial<LeadRequest>): Observable<Lead> {
    return this.http.put<Lead>(`${this.base}/leads/${id}`, data);
  }

  // Residents
  getResidents(): Observable<Resident[]> {
    return this.http.get<Resident[]>(`${this.base}/residents/by-tenant/${this.tenantId}`);
  }
}

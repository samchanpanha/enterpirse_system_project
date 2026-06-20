import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { BranchService } from '../../../core/services/branch.service';
import { Report, ReportRequest, DashboardSummary } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportingApiService {
  private base = `${environment.apiUrl}/reporting`;

  constructor(
    private http: HttpClient,
    private branch: BranchService,
  ) {}

  private get tenantId() {
    return this.branch.tenantId;
  }

  getReports(): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.base}/reports/by-tenant/${this.tenantId}`);
  }

  getReport(id: string): Observable<Report> {
    return this.http.get<Report>(`${this.base}/reports/${id}`);
  }

  generateReport(data: ReportRequest): Observable<Report> {
    return this.http.post<Report>(`${this.base}/reports/generate`, { ...data, tenantId: this.tenantId });
  }

  scheduleReport(id: string, cron: string): Observable<Report> {
    return this.http.post<Report>(`${this.base}/reports/${id}/schedule`, { schedule: cron });
  }

  getDashboardSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.base}/dashboard/summary/${this.tenantId}`);
  }
}

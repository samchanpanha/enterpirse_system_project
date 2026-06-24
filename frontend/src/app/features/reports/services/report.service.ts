import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  Report, 
  ReportExecution, 
  ReportResult, 
  DataSource,
  CreateReportRequest, 
  UpdateReportRequest,
  ExecuteReportRequest 
} from '../models/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private readonly apiUrl = '/api/reports';
  private readonly dataSourceUrl = '/api/datasources';

  constructor(private http: HttpClient) {}

  // Report CRUD operations
  getReports(): Observable<Report[]> {
    return this.http.get<Report[]>(this.apiUrl);
  }

  getReport(id: string): Observable<Report> {
    return this.http.get<Report>(`${this.apiUrl}/${id}`);
  }

  createReport(request: CreateReportRequest): Observable<Report> {
    return this.http.post<Report>(this.apiUrl, request);
  }

  updateReport(id: string, request: UpdateReportRequest): Observable<Report> {
    return this.http.put<Report>(`${this.apiUrl}/${id}`, request);
  }

  deleteReport(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Report execution
  executeReport(id: string, request: ExecuteReportRequest): Observable<ReportExecution> {
    return this.http.post<ReportExecution>(`${this.apiUrl}/${id}/execute`, request);
  }

  getExecution(executionId: string): Observable<ReportExecution> {
    return this.http.get<ReportExecution>(`${this.apiUrl}/executions/${executionId}`);
  }

  getExecutionResult(executionId: string): Observable<ReportResult> {
    return this.http.get<ReportResult>(`${this.apiUrl}/executions/${executionId}/result`);
  }

  cancelExecution(executionId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/executions/${executionId}`);
  }

  // Scheduled reports
  getScheduledReports(): Observable<Report[]> {
    return this.http.get<Report[]>(`${this.apiUrl}/scheduled`);
  }

  // Data sources
  getDataSources(): Observable<DataSource[]> {
    return this.http.get<DataSource[]>(this.dataSourceUrl);
  }

  getDataSource(id: string): Observable<DataSource> {
    return this.http.get<DataSource>(`${this.dataSourceUrl}/${id}`);
  }

  // Export report results
  exportResult(executionId: string, format: 'CSV' | 'Excel' | 'PDF'): Observable<Blob> {
    const params = new HttpParams().set('format', format);
    return this.http.get(`${this.apiUrl}/executions/${executionId}/export`, {
      params,
      responseType: 'blob'
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { ReportingApiService } from './services/reporting-api.service';
import { Report, ReportRequest } from './models/report.model';
import { DashboardSummary } from './models/report.model';

@Component({
  selector: 'app-report-list',
  standalone: true,
  imports: [DecimalPipe, DatePipe, FormsModule, TableModule, SelectModule, ButtonModule, InputTextModule, TagModule, DialogModule, ToastModule],
  providers: [MessageService],
  template: `
    <p-toast />

    <div class="grid mb-4">
      <div class="col-12 md:col-3">
        <div class="card p-3 surface-card border-round shadow-1 text-center">
          <div class="text-2xl font-bold text-primary"><span>$</span>{{ (summary?.totalRevenue ?? 0) | number:'1.0-0' }}</div>
          <div class="text-sm text-gray-500">Total Revenue</div>
        </div>
      </div>
      <div class="col-12 md:col-3">
        <div class="card p-3 surface-card border-round shadow-1 text-center">
          <div class="text-2xl font-bold text-red-600"><span>$</span>{{ (summary?.totalExpenses ?? 0) | number:'1.0-0' }}</div>
          <div class="text-sm text-gray-500">Total Expenses</div>
        </div>
      </div>
      <div class="col-12 md:col-3">
        <div class="card p-3 surface-card border-round shadow-1 text-center">
          <div class="text-2xl font-bold" [class.text-green-600]="(summary?.netIncome ?? 0) >= 0" [class.text-red-600]="(summary?.netIncome ?? 0) < 0">
            <span>$</span>{{ (summary?.netIncome ?? 0) | number:'1.0-0' }}
          </div>
          <div class="text-sm text-gray-500">Net Income</div>
        </div>
      </div>
      <div class="col-12 md:col-3">
        <div class="card p-3 surface-card border-round shadow-1 text-center">
          <div class="text-2xl font-bold text-orange-600">{{ summary?.overdueInvoices || 0 }}</div>
          <div class="text-sm text-gray-500">Overdue Invoices</div>
        </div>
      </div>
    </div>

    <div class="flex align-items-center justify-content-between mb-3">
      <h2 class="text-xl font-bold m-0">Reports</h2>
      <p-button label="Generate Report" icon="pi pi-plus" severity="success" (onClick)="showGenerateForm()" />
    </div>

      <p-dialog
        [(visible)]="showDialog"
        header="Generate Report"
        [modal]="true"
        [style]="{ width: '500px' }"
        (onHide)="showDialog = false"
      >
        <div class="flex flex-column gap-3">
          <input pInputText [(ngModel)]="reportForm.reportName" placeholder="Report Name *" />
          <div class="grid">
            <div class="col-6">
              <label class="text-xs block mb-1">Type</label>
              <p-select
                [(ngModel)]="reportForm.reportType"
                [options]="typeOptions"
                optionLabel="label"
                optionValue="value"
                class="w-full"
              />
            </div>
            <div class="col-6">
              <label class="text-xs block mb-1">Format</label>
              <p-select
                [(ngModel)]="reportForm.format"
                [options]="formatOptions"
                optionLabel="label"
                optionValue="value"
                class="w-full"
              />
            </div>
          </div>
          <p-button label="Generate" [loading]="generating" (onClick)="generate()" />
        </div>
      </p-dialog>

    <p-table
      [value]="reports"
      [paginator]="true"
      [rows]="20"
      [loading]="loading"
      [globalFilterFields]="['reportName', 'reportType', 'format']"
      styleClass="p-datatable-striped"
    >
      <ng-template pTemplate="caption">
        <div class="flex justify-content-end">
          <input pInputText type="text" placeholder="Search reports..." />
        </div>
      </ng-template>
      <ng-template pTemplate="header">
        <tr>
          <th>Report Name</th>
          <th>Type</th>
          <th>Format</th>
          <th>Status</th>
          <th>Generated</th>
          <th>Actions</th>
        </tr>
      </ng-template>
      <ng-template pTemplate="body" let-r>
        <tr>
          <td class="font-medium">{{ r.reportName }}</td>
          <td><p-tag [value]="r.reportType" severity="info" /></td>
          <td><p-tag [value]="r.format" severity="warn" /></td>
          <td>
            <p-tag
              [value]="r.status"
              [severity]="r.status === 'GENERATED' ? 'success' : r.status === 'FAILED' ? 'danger' : r.status === 'SCHEDULED' ? 'info' : 'warn'"
            />
          </td>
          <td>{{ r.generatedAt | date }}</td>
          <td>
            <div class="flex gap-1">
              @if (r.status === 'GENERATED' && r.fileUrl) {
                <a [href]="r.fileUrl" target="_blank" pButton label="Download" size="small" severity="success" class="no-underline"></a>
              }
              <p-button label="Schedule" size="small" severity="info" (onClick)="schedule(r)" />
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template pTemplate="emptymessage">
        <tr><td colspan="6" class="text-center text-gray-500 py-4">No reports found.</td></tr>
      </ng-template>
    </p-table>
  `,
})
export class ReportListComponent implements OnInit {
  reports: Report[] = [];
  summary?: DashboardSummary;
  loading = false;
  showDialog = false;
  generating = false;

  typeOptions = [
    { label: 'Financial', value: 'FINANCIAL' },
    { label: 'Operational', value: 'OPERATIONAL' },
    { label: 'Sales', value: 'SALES' },
    { label: 'Inventory', value: 'INVENTORY' },
    { label: 'Custom', value: 'CUSTOM' },
  ];

  formatOptions = [
    { label: 'PDF', value: 'PDF' },
    { label: 'CSV', value: 'CSV' },
    { label: 'XLSX', value: 'XLSX' },
    { label: 'HTML', value: 'HTML' },
  ];

  reportForm: ReportRequest = { reportName: '', reportType: 'FINANCIAL', format: 'PDF' };

  constructor(
    private api: ReportingApiService,
    private message: MessageService,
  ) {}

  ngOnInit() {
    this.load();
    this.api.getDashboardSummary().subscribe((res) => (this.summary = res));
  }

  load() {
    this.loading = true;
    this.api.getReports().subscribe({
      next: (res) => { this.reports = res; this.loading = false; },
      error: () => { this.loading = false; this.message.add({ severity: 'error', summary: 'Error', detail: 'Failed to load reports' }); },
    });
  }

  showGenerateForm() {
    this.reportForm = { reportName: '', reportType: 'FINANCIAL', format: 'PDF' };
    this.showDialog = true;
  }

  generate() {
    if (!this.reportForm.reportName) return;
    this.generating = true;
    this.api.generateReport(this.reportForm).subscribe({
      next: () => {
        this.message.add({ severity: 'success', summary: 'Generated', detail: 'Report generated' });
        this.showDialog = false;
        this.load();
        this.generating = false;
      },
      error: () => { this.generating = false; },
    });
  }

  schedule(r: Report) {
    const cron = prompt('Enter cron expression (e.g. 0 0 * * 1 for weekly Monday):');
    if (cron) {
      this.api.scheduleReport(r.id, cron).subscribe({
        next: () => {
          this.message.add({ severity: 'success', summary: 'Scheduled', detail: 'Report scheduled' });
          this.load();
        },
      });
    }
  }
}

export interface Report {
  id: string;
  name: string;
  description?: string;
  type: 'SQL' | 'JASPER' | 'CUSTOM';
  query?: string;
  dataSourceId: string;
  parameters?: ReportParameter[];
  active: boolean;
  schedule?: ReportSchedule;
  createdAt: string;
  updatedAt?: string;
  createdBy?: string;
  tenantId: string;
}

export interface ReportParameter {
  id: string;
  name: string;
  label: string;
  type: 'STRING' | 'NUMBER' | 'DATE' | 'BOOLEAN' | 'SELECT';
  required: boolean;
  defaultValue?: string;
  options?: SelectOption[];
}

export interface SelectOption {
  value: string;
  label: string;
}

export interface ReportSchedule {
  id: string;
  cronExpression: string;
  enabled: boolean;
  lastRun?: string;
  nextRun?: string;
}

export interface ReportExecution {
  id: string;
  reportId: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  parameters?: Record<string, any>;
  result?: ReportResult;
  error?: string;
  startedAt?: string;
  completedAt?: string;
  executedBy: string;
}

export interface ReportResult {
  id: string;
  executionId: string;
  data: any[];
  columns: ColumnDefinition[];
  rowCount: number;
  generatedAt: string;
}

export interface ColumnDefinition {
  name: string;
  label: string;
  type: 'STRING' | 'NUMBER' | 'DATE' | 'BOOLEAN';
}

export interface DataSource {
  id: string;
  name: string;
  type: 'POSTGRESQL' | 'MYSQL' | 'ORACLE' | 'MONGODB';
  connectionUrl: string;
  username?: string;
  active: boolean;
  createdAt: string;
}

export interface CreateReportRequest {
  name: string;
  description?: string;
  type: 'SQL' | 'JASPER' | 'CUSTOM';
  query?: string;
  dataSourceId: string;
  parameters?: ReportParameter[];
  active?: boolean;
  schedule?: ReportSchedule;
}

export interface UpdateReportRequest {
  name?: string;
  description?: string;
  query?: string;
  parameters?: ReportParameter[];
  active?: boolean;
  schedule?: ReportSchedule;
}

export interface ExecuteReportRequest {
  parameters?: Record<string, any>;
}

export interface ReportDefinition {
  id: string; tenantId: string
  name: string; code?: string
  type: string
  config: string
  system: boolean
  createdBy?: string
  createdAt?: string; updatedAt?: string
}

export interface ScheduledReport {
  id: string; tenantId: string
  reportDefinitionId: string
  schedule: string
  recipients: string
  format: string
  enabled: boolean
  lastRunAt?: string; nextRunAt?: string
}

export interface ReportExecution {
  id: string; tenantId: string
  reportDefinitionId: string
  status: string
  startedAt: string
  completedAt?: string
  rowCount?: number
  errorMessage?: string
  resultUrl?: string
}

export interface DashboardConfig {
  id: string; tenantId: string
  name: string
  description?: string
  layout: string
  isDefault?: boolean
  widgets?: string
  shared?: boolean
  createdBy?: string
  createdAt?: string; updatedAt?: string
}

export interface AggregatedSnapshot {
  id: string; tenantId: string
  scope: string
  scopeId?: string
  periodDate: string
  metrics: string
}

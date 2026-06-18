import { defineStore } from 'pinia'

export interface ReportDefinition {
  id: string; tenantId: string; name: string
  code?: string; type: string; config: string
  system: boolean
}
export interface ReportExecution {
  id: string; reportId: string; tenantId: string
  parameters?: string; status: string
  outputUrl?: string; rowCount?: number
  durationMs?: number; errorMessage?: string
  requestedBy?: string
  startedAt?: string; completedAt?: string
}
export interface DashboardConfig {
  id: string; tenantId: string; name: string
  layout: string; isDefault: boolean
  createdBy?: string
  createdAt?: string
}

export const useReportingStore = defineStore('reporting', () => {
  const branchStore = useBranchStore()

  const definitions = ref<ReportDefinition[]>([])
  const executions = ref<ReportExecution[]>([])
  const dashboards = ref<DashboardConfig[]>([])
  const loading = ref(false)

  async function fetchDefinitions (tenantId: string) {
    loading.value = true
    try { definitions.value = await branchStore.$apiWithBranch(`/reporting/reports/definitions/by-tenant/${tenantId}`) } finally { loading.value = false }
  }

  async function createDefinition (data: Partial<ReportDefinition>) {
    const d = await branchStore.$apiWithBranch('/reporting/reports/definitions', { method: 'POST', body: data })
    definitions.value.push(d)
    return d
  }

  async function executeReport (reportId: string, tenantId: string, parameters?: string, requestedBy?: string) {
    return await branchStore.$apiWithBranch(`/reporting/reports/definitions/${reportId}/execute`, {
      method: 'POST',
      body: { tenantId, parameters, requestedBy }
    })
  }

  async function fetchDashboards (tenantId: string) {
    loading.value = true
    try { dashboards.value = await branchStore.$apiWithBranch(`/reporting/dashboards/by-tenant/${tenantId}`) } finally { loading.value = false }
  }

  async function createDashboard (data: Partial<DashboardConfig>) {
    const d = await branchStore.$apiWithBranch('/reporting/dashboards', { method: 'POST', body: data })
    dashboards.value.push(d)
    return d
  }

  return {
    definitions,
    executions,
    dashboards,
    loading,
    fetchDefinitions,
    createDefinition,
    executeReport,
    fetchDashboards,
    createDashboard
  }
})

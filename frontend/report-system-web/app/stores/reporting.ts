import { defineStore } from 'pinia'
import type { ReportDefinition, ReportExecution, DashboardConfig } from '~/shared/types/reporting'

function safeJsonParse<T = any> (value: any): T | undefined {
  if (value == null) { return undefined }
  if (typeof value === 'string') {
    try { return JSON.parse(value) as T } catch { return value as unknown as T }
  }
  return value as T
}

function parseExecution (e: any): ReportExecution {
  const parsed: ReportExecution = { ...e, resultData: safeJsonParse<any[]>(e?.resultData) }
  return parsed
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
    const body: any = { ...data }
    if (body.config != null && typeof body.config !== 'string') { body.config = JSON.stringify(body.config) }
    if (body.layout != null && typeof body.layout !== 'string') { body.layout = JSON.stringify(body.layout) }
    const d = await branchStore.$apiWithBranch('/reporting/reports/definitions', { method: 'POST', body })
    definitions.value.push(d)
    return d
  }

  async function executeReport (reportId: string, tenantId: string, parameters?: string, requestedBy?: string) {
    const e = await branchStore.$apiWithBranch(`/reporting/reports/definitions/${reportId}/execute`, {
      method: 'POST',
      body: { tenantId, parameters, requestedBy }
    })
    const parsed = parseExecution(e)
    const idx = executions.value.findIndex(ex => ex.id === parsed.id)
    if (idx >= 0) { executions.value[idx] = parsed } else { executions.value.unshift(parsed) }
    return parsed
  }

  async function fetchExecution (id: string) {
    const e = await branchStore.$apiWithBranch(`/reporting/reports/executions/${id}`)
    const parsed = parseExecution(e)
    const idx = executions.value.findIndex(ex => ex.id === parsed.id)
    if (idx >= 0) { executions.value[idx] = parsed } else { executions.value.unshift(parsed) }
    return parsed
  }

  async function fetchExecutions (reportId: string) {
    const res: any = await branchStore.$apiWithBranch(`/reporting/reports/executions/by-report/${reportId}`)
    const list: ReportExecution[] = (Array.isArray(res) ? res : []).map(parseExecution)
    executions.value = list
    return list
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
    fetchExecution,
    fetchExecutions,
    fetchDashboards,
    createDashboard
  }
})

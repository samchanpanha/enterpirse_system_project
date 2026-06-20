<template>
  <div>
    <AdminPageHeader title="Report Definitions" :subtitle="`${list.items.value.length} reports`">
      <template #actions>
        <button
          v-if="can('report.read')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="openCreateDrawer"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Report
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search reports…"
    />

    <AdminTable
      :items="filteredItems"
      :columns="columns"
      :pagination="list.pagination"
      :selectable="true"
      :selected-ids="list.selectedIds.value"
      @page-change="list.setPage"
      @page-size-change="list.setPageSize"
      @toggle-select="(row) => list.toggleSelect(row.id)"
      @toggle-all="list.isAllSelected() ? list.clearSelection() : list.selectAll()"
    >
      <template #cell-name="{ value }">
        <span class="font-medium text-text-primary">{{ value }}</span>
      </template>
      <template #cell-type="{ value }">
        <span class="inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded bg-info-light text-info">
          {{ value }}
        </span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('report.read')"
          class="text-sm text-primary hover:text-primary-hover px-2 py-0.5"
          @click="runReport(row)"
        >
          Run
        </button>
      </template>
    </AdminTable>

    <!-- Create report drawer -->
    <AdminDrawer
      v-model="createDrawer.open.value"
      title="New Report"
      subtitle="Build a report definition"
      width="lg"
    >
      <AdminForm
        v-if="createDrawer.open.value"
        v-model="formData"
        :groups="formGroups"
      />
      <div class="mt-6">
        <div class="flex items-center justify-between mb-2">
          <h4 class="text-sm font-medium text-text-primary">
            Columns
          </h4>
          <button
            type="button"
            class="text-xs text-primary hover:text-primary-hover"
            @click="addColumn"
          >
            + Add column
          </button>
        </div>
        <div class="space-y-2">
          <div
            v-for="(col, idx) in newColumns"
            :key="idx"
            class="grid grid-cols-12 gap-2 items-center p-2 border rounded bg-gray-50"
          >
            <div class="col-span-5">
              <input
                v-model="col.name"
                type="text"
                placeholder="Column name"
                class="w-full px-2 py-1 text-sm border border-border rounded"
              >
            </div>
            <div class="col-span-4">
              <input
                v-model="col.key"
                type="text"
                placeholder="Key"
                class="w-full px-2 py-1 text-sm border border-border rounded"
              >
            </div>
            <div class="col-span-2">
              <select
                v-model="col.type"
                class="w-full px-2 py-1 text-sm border border-border rounded"
              >
                <option value="text">
                  Text
                </option>
                <option value="number">
                  Number
                </option>
                <option value="date">
                  Date
                </option>
                <option value="currency">
                  Currency
                </option>
              </select>
            </div>
            <div class="col-span-1 text-right">
              <button
                type="button"
                class="text-danger hover:opacity-80"
                @click="removeColumn(idx)"
              >
                <Icon icon="ant-design:delete-outlined" />
              </button>
            </div>
          </div>
          <p v-if="newColumns.length === 0" class="text-xs text-text-secondary">
            No columns defined yet.
          </p>
        </div>
      </div>
      <p v-if="createDrawer.error.value" class="mt-3 text-sm text-danger bg-danger-light p-2 rounded">
        {{ createDrawer.error.value }}
      </p>
      <template #footer>
        <button class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50" @click="createDrawer.close()">
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50 inline-flex items-center gap-1.5"
          :disabled="createDrawer.saving.value"
          @click="submitCreate"
        >
          <Icon v-if="createDrawer.saving.value" icon="ant-design:loading-outlined" class="animate-spin" />
          {{ createDrawer.saving.value ? 'Saving…' : 'Create' }}
        </button>
      </template>
    </AdminDrawer>

    <!-- Run report drawer -->
    <AdminDrawer
      v-model="runDrawer.open.value"
      :title="`Run Report: ${runDrawer.editing.value?.name || ''}`"
      subtitle="Execution status and results"
      width="xl"
    >
      <div v-if="runLoading" class="flex items-center gap-2 text-text-secondary py-8">
        <Icon icon="ant-design:loading-outlined" class="animate-spin text-lg" />
        <span>Running report…</span>
      </div>
      <div v-else-if="currentExecution" class="space-y-4">
        <div class="flex items-center gap-3">
          <span
            :class="[
              'inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded',
              statusClass(currentExecution.status)
            ]"
          >
            {{ currentExecution.status }}
          </span>
          <span class="text-sm text-text-secondary">
            {{ currentExecution.rowCount ?? 0 }} rows
          </span>
          <span v-if="currentExecution.durationMs" class="text-sm text-text-secondary">
            {{ currentExecution.durationMs }} ms
          </span>
        </div>
        <p v-if="currentExecution.errorMessage" class="text-sm text-danger bg-danger-light p-2 rounded">
          {{ currentExecution.errorMessage }}
        </p>
        <div v-if="currentExecution.status === 'completed' && resultRows.length > 0">
          <AdminTable
            :items="resultRows"
            :columns="resultColumns"
            :show-size-changer="false"
          />
        </div>
        <div v-else-if="currentExecution.status === 'completed'" class="text-sm text-text-secondary py-4">
          No rows returned.
        </div>
      </div>
      <div v-else class="text-sm text-text-secondary py-8">
        Click Run to execute the report.
      </div>
      <template #footer>
        <button class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50" @click="runDrawer.close()">
          Close
        </button>
        <button
          v-if="currentExecution"
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover"
          @click="refreshExecution"
        >
          Refresh
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import type { FormGroup } from '~/shared/types/form'
import type { ReportColumn, ReportDefinition, ReportExecution } from '~/shared/types/reporting'

definePageMeta({ middleware: 'auth' })

const store = useReportingStore()
const { can } = usePermission()
const { user } = useAuth()
const list = useListPage<ReportDefinition>({ pageSize: 20 })
const createDrawer = useDrawer<ReportDefinition>()
const runDrawer = useDrawer<ReportDefinition>()

const newColumns = ref<ReportColumn[]>([])
const currentExecution = ref<ReportExecution | null>(null)
const runLoading = ref(false)
let pollTimer: ReturnType<typeof setInterval> | null = null

const columns: ColumnDef[] = [
  { key: 'name', title: 'Name', sortable: true },
  { key: 'type', title: 'Type', width: '140px' },
  { key: 'createdAt', title: 'Created', width: '180px' }
]

const filteredItems = computed(() => {
  const q = list.search.value.toLowerCase()
  if (!q) { return list.items.value }
  return list.items.value.filter(r => r.name.toLowerCase().includes(q))
})

const formData = computed({
  get: () => createDrawer.formData.value,
  set: (val) => { createDrawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  {
    title: 'Report details',
    fields: [
      { key: 'name', label: 'Name', type: 'text', required: true, span: 12 },
      {
        key: 'type',
        label: 'Type',
        type: 'select',
        required: true,
        span: 12,
        options: [
          { label: 'Table', value: 'table' },
          { label: 'Chart', value: 'chart' },
          { label: 'Summary', value: 'summary' }
        ]
      },
      {
        key: 'source',
        label: 'Data source',
        type: 'select',
        required: true,
        span: 12,
        options: [
          { label: 'Snapshots', value: 'snapshots' },
          { label: 'Accounts', value: 'accounts' },
          { label: 'Invoices', value: 'invoices' },
          { label: 'Payments', value: 'payments' },
          { label: 'Inventory', value: 'inventory' }
        ]
      }
    ]
  }
])

const selectedReportLayout = computed<ReportColumn[]>(() => {
  const report = runDrawer.editing.value
  if (!report?.layout) { return [] }
  if (Array.isArray(report.layout)) { return report.layout }
  try { return JSON.parse(report.layout) as ReportColumn[] } catch { return [] }
})

const resultRows = computed(() => currentExecution.value?.resultData || [])

const resultColumns = computed<ColumnDef[]>(() => {
  const layout = selectedReportLayout.value
  if (layout.length > 0) {
    return layout.map(c => ({ key: c.key, title: c.name }))
  }
  if (resultRows.value.length === 0) { return [] }
  return Object.keys(resultRows.value[0]).map(k => ({ key: k, title: k }))
})

function statusClass (status: string) {
  switch (status) {
    case 'completed': return 'bg-success-light text-success'
    case 'failed': return 'bg-danger-light text-danger'
    case 'running': return 'bg-info-light text-info'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

function openCreateDrawer () {
  newColumns.value = []
  createDrawer.openFor(null)
}

function addColumn () {
  newColumns.value.push({ name: '', key: '', type: 'text' })
}

function removeColumn (idx: number) {
  newColumns.value.splice(idx, 1)
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchDefinitions(user.value.tenantId)
    list.setItems(store.definitions || [], (store.definitions || []).length)
  } finally { list.loading.value = false }
}

async function submitCreate () {
  const f = createDrawer.formData.value
  if (!f.name || !f.type || !f.source) {
    createDrawer.error.value = 'Name, type and data source are required'
    return
  }
  createDrawer.saving.value = true
  createDrawer.error.value = null
  try {
    await store.createDefinition({
      tenantId: user.value?.tenantId,
      name: f.name,
      type: f.type,
      config: { source: f.source },
      layout: newColumns.value
    })
    await load()
    createDrawer.close()
  } catch (e: any) {
    createDrawer.error.value = e?.data?.message || 'Failed to create report'
  } finally {
    createDrawer.saving.value = false
  }
}

async function runReport (row: ReportDefinition) {
  stopPoll()
  currentExecution.value = null
  runDrawer.openFor(row)
  runLoading.value = true
  try {
    const exec = await store.executeReport(row.id, user.value?.tenantId || '')
    currentExecution.value = exec
    if (exec.status === 'running') {
      startPoll(exec.id)
    }
  } catch (e: any) {
    currentExecution.value = {
      id: '',
      reportId: row.id,
      tenantId: row.tenantId,
      status: 'failed',
      errorMessage: e?.data?.message || 'Failed to run report'
    }
  } finally {
    runLoading.value = false
  }
}

async function refreshExecution () {
  if (!currentExecution.value) { return }
  runLoading.value = true
  try {
    currentExecution.value = await store.fetchExecution(currentExecution.value.id)
  } finally {
    runLoading.value = false
  }
}

function startPoll (id: string) {
  stopPoll()
  pollTimer = setInterval(async () => {
    try {
      const exec = await store.fetchExecution(id)
      currentExecution.value = exec
      if (exec.status === 'completed' || exec.status === 'failed') {
        stopPoll()
      }
    } catch {
      stopPoll()
    }
  }, 2000)
}

function stopPoll () {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(load)
onUnmounted(stopPoll)
</script>

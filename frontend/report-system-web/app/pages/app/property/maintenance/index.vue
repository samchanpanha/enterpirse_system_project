<template>
  <div>
    <AdminPageHeader
      title="Maintenance Tickets"
      :subtitle="`${tickets.length} tickets for this property`"
    >
      <template #actions>
        <button
          v-if="can('maintenance.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="drawer.openFor(null); resetForm()"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Ticket
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by title…"
      :has-active-filters="list.filters.value.priority || list.filters.value.status"
      @reset="list.resetFilters()"
    >
      <template #filters>
        <select
          v-model="list.filters.value.priority"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All priorities
          </option>
          <option value="HIGH">
            High
          </option>
          <option value="MEDIUM">
            Medium
          </option>
          <option value="LOW">
            Low
          </option>
        </select>
        <select
          v-model="list.filters.value.status"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All statuses
          </option>
          <option value="OPEN">
            Open
          </option>
          <option value="IN_PROGRESS">
            In progress
          </option>
          <option value="RESOLVED">
            Resolved
          </option>
          <option value="CLOSED">
            Closed
          </option>
        </select>
      </template>
    </AdminSearchBar>

    <AdminTable
      :items="filteredItems"
      :columns="columns"
      :loading="list.loading.value"
      :pagination="list.pagination"
      :selectable="true"
      :selected-ids="list.selectedIds.value"
      @page-change="list.setPage"
      @page-size-change="list.setPageSize"
      @toggle-select="(row) => list.toggleSelect(row.id)"
      @toggle-all="list.isAllSelected() ? list.clearSelection() : list.selectAll()"
    >
      <template #cell-priority="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', priorityClass(value)]">
          <span :class="['w-1.5 h-1.5 rounded-full', priorityDot(value)]" />
          {{ value }}
        </span>
      </template>
      <template #cell-status="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', statusClass(value)]">
          <span :class="['w-1.5 h-1.5 rounded-full', statusDot(value)]" />
          {{ value }}
        </span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('maintenance.update') && row.status !== 'CLOSED'"
          class="text-sm text-primary hover:text-primary-hover px-2 py-0.5"
          @click="updateStatus(row, 'IN_PROGRESS')"
        >
          Start
        </button>
        <button
          v-if="can('maintenance.update') && row.status === 'IN_PROGRESS'"
          class="text-sm text-success hover:opacity-80 px-2 py-0.5"
          @click="updateStatus(row, 'RESOLVED')"
        >
          Resolve
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      :title="drawer.isEdit() ? 'Edit Ticket' : 'New Ticket'"
      width="md"
    >
      <AdminForm
        v-if="drawer.open.value"
        v-model="formData"
        :groups="formGroups"
        @submit="save"
      />
      <template #footer>
        <button
          class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50"
          @click="drawer.close()"
        >
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50"
          :disabled="drawer.saving.value"
          @click="submitForm"
        >
          {{ drawer.isEdit() ? 'Update' : 'Create' }}
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import { useFormSchema } from '~/composables/useFormSchema'

definePageMeta({ middleware: 'auth' })

const route = useRoute()
const propertyId = computed(() => route.params.id as string)
const { can } = usePermission()
const { user } = useAuth()
const branchStore = useBranchStore()
const { text, select, textarea, group, opt } = useFormSchema()

const tickets = ref<any[]>([])
const list = useListPage<any>({
  pageSize: 20,
  initialFilters: { priority: '', status: '' }
})
const drawer = useDrawer<any>()

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const formGroups = computed(() => [
  group('Issue', [
    text('title', 'Title', { required: true, placeholder: 'Leaking faucet' }),
    select('priority', 'Priority', [
      opt('HIGH', 'High'),
      opt('MEDIUM', 'Medium'),
      opt('LOW', 'Low')
    ], { required: true }),
    textarea('description', 'Description', { span: 12 })
  ])
])

const columns: ColumnDef[] = [
  { key: 'title', title: 'Title', sortable: true },
  { key: 'priority', title: 'Priority', width: '120px' },
  { key: 'status', title: 'Status', width: '140px' }
]

const filteredItems = computed(() => {
  let items = tickets.value
  const q = list.search.value.toLowerCase()
  if (q) { items = items.filter(t => t.title.toLowerCase().includes(q)) }
  if (list.filters.value.priority) { items = items.filter(t => t.priority === list.filters.value.priority) }
  if (list.filters.value.status) { items = items.filter(t => t.status === list.filters.value.status) }
  return items
})

function priorityClass (p: string): string {
  switch (p) {
    case 'HIGH': return 'bg-danger-light text-danger'
    case 'MEDIUM': return 'bg-warning-light text-warning'
    case 'LOW': return 'bg-success-light text-success'
    default: return 'bg-gray-100 text-text-secondary'
  }
}
function priorityDot (p: string): string {
  return p === 'HIGH' ? 'bg-danger' : p === 'MEDIUM' ? 'bg-warning' : 'bg-success'
}
function statusClass (s: string): string {
  switch (s) {
    case 'OPEN': return 'bg-warning-light text-warning'
    case 'IN_PROGRESS': return 'bg-primary-light text-primary'
    case 'RESOLVED': return 'bg-success-light text-success'
    case 'CLOSED': return 'bg-gray-100 text-text-secondary'
    default: return 'bg-gray-100 text-text-secondary'
  }
}
function statusDot (s: string): string {
  return s === 'OPEN' ? 'bg-warning' : s === 'IN_PROGRESS' ? 'bg-primary' : s === 'RESOLVED' ? 'bg-success' : 'bg-text-disabled'
}

function resetForm () {
  drawer.formData.value = { title: '', priority: 'MEDIUM', description: '' }
}

function submitForm () {
  save(formData.value)
}

async function save (data: any) {
  drawer.saving.value = true
  try {
    const config = useRuntimeConfig()
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    if (user.value?.tenantId) { headers['X-Tenant-Id'] = user.value.tenantId }
    if (branchStore.currentBranchId) { headers['X-Branch-Id'] = branchStore.currentBranchId }
    await $fetch(`${config.public.apiBaseUrl}/property/maintenance`, {
      method: 'POST', headers, body: { propertyId: propertyId.value, ...data }
    })
    await load()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.data?.message || 'Failed to create ticket'
  } finally {
    drawer.saving.value = false
  }
}

async function updateStatus (t: any, status: string) {
  try {
    const config = useRuntimeConfig()
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    if (user.value?.tenantId) { headers['X-Tenant-Id'] = user.value.tenantId }
    await $fetch(`${config.public.apiBaseUrl}/property/maintenance/${t.id}/status`, {
      method: 'PUT', headers, body: { status }
    })
    await load()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to update')
  }
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    const config = useRuntimeConfig()
    const headers: Record<string, string> = {}
    if (user.value.tenantId) { headers['X-Tenant-Id'] = user.value.tenantId }
    if (branchStore.currentBranchId) { headers['X-Branch-Id'] = branchStore.currentBranchId }
    const res: any = await $fetch(`${config.public.apiBaseUrl}/property/maintenance/by-property/${propertyId.value}`, { headers })
    tickets.value = Array.isArray(res) ? res : (res?.content || [])
  } catch (e) {
    tickets.value = []
  } finally {
    list.loading.value = false
  }
}

onMounted(load)
</script>

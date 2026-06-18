<template>
  <div>
    <AdminPageHeader
      title="Stock Transfers"
      subtitle="Move inventory between branches. Each transfer requires a source branch, destination branch, and at least one item."
    >
      <template #actions>
        <button
          v-if="can('inventory.transfer.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="createTransfer()"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Transfer
        </button>
      </template>
    </AdminPageHeader>

    <div class="px-6 py-3 flex items-center gap-2 border-b border-border bg-white flex-wrap">
      <span class="text-xs text-text-secondary uppercase tracking-wide">Filter:</span>
      <button
        v-for="s in statusFilters"
        :key="s.value"
        :class="[
          'px-3 py-1 rounded-full text-xs font-medium transition-colors',
          statusFilter === s.value
            ? 'bg-primary text-white'
            : 'bg-white text-text-secondary border border-border hover:bg-gray-50'
        ]"
        @click="setStatusFilter(s.value)"
      >
        {{ s.label }} ({{ counts[s.value] || 0 }})
      </button>
    </div>

    <AdminTable
      :items="filteredTransfers"
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
      <template #cell-status="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', statusClass(value)]">
          <span :class="['w-1.5 h-1.5 rounded-full', statusDot(value)]" />
          {{ value }}
        </span>
      </template>
      <template #cell-fromBranchId="{ value }">
        <span class="text-text-primary">{{ branchName(value) }}</span>
      </template>
      <template #cell-toBranchId="{ value }">
        <span class="text-text-primary">{{ branchName(value) }}</span>
      </template>
      <template #cell-createdAt="{ value }">
        <span class="text-text-secondary text-xs">{{ new Date(value).toLocaleDateString() }}</span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('inventory.transfer.ship') && row.status === 'DRAFT'"
          class="text-sm text-primary hover:text-primary-hover px-2 py-0.5"
          @click="ship(row)"
        >
          Ship
        </button>
        <button
          v-if="can('inventory.transfer.receive') && row.status === 'SHIPPED'"
          class="text-sm text-success hover:opacity-80 px-2 py-0.5"
          @click="receive(row)"
        >
          Receive
        </button>
        <button
          v-if="can('inventory.transfer.cancel') && (row.status === 'DRAFT' || row.status === 'SHIPPED')"
          class="text-sm text-danger hover:opacity-80 px-2 py-0.5"
          @click="cancelTransfer(row)"
        >
          Cancel
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      title="New Stock Transfer"
      subtitle="Move stock from one branch to another"
      width="md"
    >
      <AdminForm
        v-if="drawer.open.value"
        v-model="formData"
        :groups="formGroups"
        @submit="save"
      />
      <p
        v-if="drawer.error.value"
        class="mt-3 text-sm text-danger flex items-center gap-1 bg-danger-light p-2 rounded"
      >
        <Icon icon="ant-design:exclamation-circle-outlined" />
        {{ drawer.error.value }}
      </p>
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
          Create Transfer
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { StockTransfer } from '~/shared/types/inventory'
import type { FormGroup } from '~/shared/types/form'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import { useFormSchema } from '~/composables/useFormSchema'

definePageMeta({ middleware: 'auth' })

const store = useInventoryStore()
const branchStore = useBranchStore()
const { can } = usePermission()
const { user } = useAuth()
const { select, textarea, group, opt } = useFormSchema()

const list = useListPage<StockTransfer>({ pageSize: 20 })
const drawer = useDrawer<StockTransfer>()
const statusFilter = ref<string>('ALL')

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const statusFilters = [
  { value: 'ALL', label: 'All' },
  { value: 'DRAFT', label: 'Draft' },
  { value: 'SHIPPED', label: 'In transit' },
  { value: 'RECEIVED', label: 'Received' },
  { value: 'CANCELLED', label: 'Cancelled' }
]

const counts = computed(() => {
  const c: Record<string, number> = { ALL: list.items.value.length }
  for (const t of list.items.value) {
    c[t.status] = (c[t.status] || 0) + 1
  }
  return c
})

const filteredTransfers = computed(() => {
  if (statusFilter.value === 'ALL') { return list.items.value }
  return list.items.value.filter(t => t.status === statusFilter.value)
})

const formGroups = computed<FormGroup[]>(() => [
  group('Branches', [
    select('fromBranchId', 'From branch', branchStore.branches.map(b => opt(b.id, b.name)), { required: true, span: 6 }),
    select('toBranchId', 'To branch', branchStore.branches.map(b => opt(b.id, b.name)), { required: true, span: 6 })
  ]),
  group('Details', [
    textarea('notes', 'Notes', { span: 12, placeholder: 'Optional notes for the receiving branch' })
  ])
])

const columns: ColumnDef[] = [
  { key: 'fromBranchId', title: 'From', width: '180px' },
  { key: 'toBranchId', title: 'To', width: '180px' },
  { key: 'status', title: 'Status', width: '140px' },
  { key: 'createdAt', title: 'Created', width: '140px' }
]

function setStatusFilter (s: string) {
  statusFilter.value = s
}

function branchName (id: string): string {
  return branchStore.branches.find(b => b.id === id)?.name || id.slice(0, 8)
}

function statusClass (status: string): string {
  switch (status) {
    case 'DRAFT': return 'bg-gray-100 text-text-secondary'
    case 'SHIPPED': return 'bg-primary-light text-primary'
    case 'RECEIVED': return 'bg-success-light text-success'
    case 'CANCELLED': return 'bg-danger-light text-danger'
    default: return 'bg-warning-light text-warning'
  }
}

function statusDot (status: string): string {
  switch (status) {
    case 'DRAFT': return 'bg-text-disabled'
    case 'SHIPPED': return 'bg-primary'
    case 'RECEIVED': return 'bg-success'
    case 'CANCELLED': return 'bg-danger'
    default: return 'bg-warning'
  }
}

function createTransfer () {
  drawer.openFor(null)
  drawer.formData.value = {
    fromBranchId: branchStore.currentBranchId || '',
    toBranchId: '',
    notes: ''
  }
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await Promise.all([
      branchStore.fetchBranches(),
      store.fetchTransfers(user.value.tenantId)
    ])
    const items = store.transfers
    list.setItems(items || [], (items || []).length)
  } catch (e: any) {
    list.error.value = e?.data?.message || 'Failed to load transfers'
  } finally {
    list.loading.value = false
  }
}

function submitForm () {
  const f = formData.value
  if (!f.fromBranchId) { drawer.error.value = 'Source branch is required'; return }
  if (!f.toBranchId) { drawer.error.value = 'Destination branch is required'; return }
  if (f.fromBranchId === f.toBranchId) { drawer.error.value = 'Source and destination must differ'; return }
  drawer.error.value = null
  save(f)
}

async function save (data: any) {
  drawer.saving.value = true
  try {
    await store.createTransfer({
      tenantId: user.value!.tenantId,
      ...data,
      items: []
    } as any)
    await load()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.data?.message || 'Failed to create transfer'
  } finally {
    drawer.saving.value = false
  }
}

async function ship (t: StockTransfer) {
  if (!confirm('Ship this transfer?')) { return }
  try {
    await store.shipTransfer(t.id)
    await load()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to ship')
  }
}

async function receive (t: StockTransfer) {
  if (!confirm('Mark this transfer as received?')) { return }
  try {
    await store.receiveTransfer(t.id)
    await load()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to receive')
  }
}

async function cancelTransfer (t: StockTransfer) {
  const reason = prompt('Reason for cancellation:')
  if (!reason) { return }
  try {
    await store.cancelTransfer(t.id, reason)
    await load()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to cancel')
  }
}

onMounted(load)
</script>

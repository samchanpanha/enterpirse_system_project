<template>
  <div>
    <AdminPageHeader title="Invoices" :subtitle="`${list.items.value.length} invoices`">
      <template #actions>
        <button
          v-if="can('finance.invoice.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="openCreate()"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Invoice
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by invoice number or customer…"
      :has-active-filters="list.filters.value.status"
      @reset="list.resetFilters()"
    >
      <template #filters>
        <select
          v-model="list.filters.value.status"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All statuses
          </option>
          <option value="DRAFT">
            Draft
          </option>
          <option value="OPEN">
            Open
          </option>
          <option value="PARTIAL">
            Partial
          </option>
          <option value="PAID">
            Paid
          </option>
          <option value="OVERDUE">
            Overdue
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
      <template #cell-invoiceNumber="{ value }">
        <span class="font-mono text-text-primary">{{ value }}</span>
      </template>
      <template #cell-customerName="{ value }">
        <span class="text-text-primary">{{ value }}</span>
      </template>
      <template #cell-totalAmount="{ value }">
        <span class="font-medium text-text-primary">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #cell-balanceDue="{ value }">
        <span :class="Number(value) > 0 ? 'font-medium text-danger' : 'text-text-secondary'">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #cell-status="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', statusClass(value)]">
          <span :class="['w-1.5 h-1.5 rounded-full', statusDot(value)]" />
          {{ value }}
        </span>
      </template>
      <template #cell-issueDate="{ value }">
        <span class="text-text-secondary text-xs">{{ new Date(value).toLocaleDateString() }}</span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('finance.invoice.pay') && row.status !== 'PAID'"
          class="text-sm text-success hover:opacity-80 px-2 py-0.5"
        >
          Mark paid
        </button>
      </template>
    </AdminTable>
  </div>
</template>

<script setup lang="ts">
import type { Invoice } from '~/shared/types/finance'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'

definePageMeta({ middleware: 'auth' })

const store = useFinanceStore()
const { can } = usePermission()
const { user } = useAuth()

const list = useListPage<Invoice>({
  pageSize: 20,
  initialFilters: { status: '' }
})

const columns: ColumnDef[] = [
  { key: 'invoiceNumber', title: 'Invoice #', sortable: true, width: '140px' },
  { key: 'customerName', title: 'Customer', width: '200px' },
  { key: 'issueDate', title: 'Issued', width: '120px' },
  { key: 'dueDate', title: 'Due', width: '120px' },
  { key: 'totalAmount', title: 'Total', align: 'right', width: '120px' },
  { key: 'balanceDue', title: 'Balance', align: 'right', width: '120px' },
  { key: 'status', title: 'Status', width: '120px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  if (list.filters.value.status) { items = items.filter(i => i.status === list.filters.value.status) }
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(i =>
      i.invoiceNumber?.toLowerCase().includes(q) ||
      i.customerName?.toLowerCase().includes(q)
    )
  }
  return items
})

function statusClass (status: string): string {
  switch (status) {
    case 'DRAFT': return 'bg-gray-100 text-text-secondary'
    case 'OPEN': return 'bg-primary-light text-primary'
    case 'PARTIAL': return 'bg-warning-light text-warning'
    case 'PAID': return 'bg-success-light text-success'
    case 'OVERDUE': return 'bg-danger-light text-danger'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

function statusDot (status: string): string {
  switch (status) {
    case 'DRAFT': return 'bg-text-disabled'
    case 'OPEN': return 'bg-primary'
    case 'PARTIAL': return 'bg-warning'
    case 'PAID': return 'bg-success'
    case 'OVERDUE': return 'bg-danger'
    default: return 'bg-text-disabled'
  }
}

function openCreate () {
  // TODO: open drawer with form
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchInvoices(user.value.tenantId)
    list.setItems(store.invoices || [], (store.invoices || []).length)
  } finally {
    list.loading.value = false
  }
}

onMounted(load)
</script>

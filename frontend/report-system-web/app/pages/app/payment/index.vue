<template>
  <div>
    <AdminPageHeader title="Payments" :subtitle="`${list.items.value.length} transactions`">
      <template #actions>
        <button
          v-if="can('payment.transaction.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Payment
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by reference or customer…"
      :has-active-filters="list.filters.value.gateway"
      @reset="list.resetFilters()"
    >
      <template #filters>
        <select
          v-model="list.filters.value.gateway"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All gateways
          </option>
          <option value="ABA_PAYWAY">
            ABA PayWay
          </option>
          <option value="WING">
            Wing
          </option>
          <option value="PI_PAY">
            Pi Pay
          </option>
          <option value="CASH">
            Cash
          </option>
        </select>
      </template>
    </AdminSearchBar>

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
      <template #cell-reference="{ value }">
        <span class="font-mono text-text-primary">{{ value }}</span>
      </template>
      <template #cell-gateway="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', gatewayClass(value)]">
          <Icon :icon="gatewayIcon(value)" />
          {{ value }}
        </span>
      </template>
      <template #cell-amount="{ value, row }">
        <span class="font-medium text-text-primary">${{ Number(value).toFixed(2) }} <span class="text-text-disabled text-xs">{{ row.currency }}</span></span>
      </template>
      <template #cell-status="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', value === 'SUCCESS' ? 'bg-success-light text-success' : value === 'PENDING' ? 'bg-warning-light text-warning' : 'bg-danger-light text-danger']">
          <span :class="['w-1.5 h-1.5 rounded-full', value === 'SUCCESS' ? 'bg-success' : value === 'PENDING' ? 'bg-warning' : 'bg-danger']" />
          {{ value }}
        </span>
      </template>
      <template #cell-paidAt="{ value }">
        <span class="text-text-secondary text-xs">{{ value ? new Date(value).toLocaleString() : '—' }}</span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('payment.transaction.refund') && row.status === 'SUCCESS'"
          class="text-sm text-danger hover:opacity-80 px-2 py-0.5"
        >
          Refund
        </button>
      </template>
    </AdminTable>
  </div>
</template>

<script setup lang="ts">
import type { Transaction } from '~/shared/types/payment'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'

definePageMeta({ middleware: 'auth' })

const store = usePaymentStore()
const { can } = usePermission()
const { user } = useAuth()

const list = useListPage<Transaction>({
  pageSize: 20,
  initialFilters: { gateway: '' }
})

const columns: ColumnDef[] = [
  { key: 'transactionId', title: 'Reference', width: '180px' },
  { key: 'gateway', title: 'Gateway', width: '140px' },
  { key: 'amount', title: 'Amount', align: 'right', width: '160px' },
  { key: 'status', title: 'Status', width: '120px' },
  { key: 'paidAt', title: 'Date', width: '180px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  if (list.filters.value.gateway) { items = items.filter(p => p.gateway === list.filters.value.gateway) }
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(p =>
      p.transactionId?.toLowerCase().includes(q) ||
      p.customerName?.toLowerCase().includes(q)
    )
  }
  return items
})

function gatewayClass (g: string): string {
  switch (g) {
    case 'ABA_PAYWAY': return 'bg-primary-light text-primary'
    case 'WING': return 'bg-info-light text-info'
    case 'PI_PAY': return 'bg-success-light text-success'
    case 'CASH': return 'bg-warning-light text-warning'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

function gatewayIcon (g: string): string {
  switch (g) {
    case 'ABA_PAYWAY': return 'ant-design:bank-outlined'
    case 'WING': return 'ant-design:thunderbolt-outlined'
    case 'PI_PAY': return 'ant-design:mobile-outlined'
    case 'CASH': return 'ant-design:dollar-outlined'
    default: return 'ant-design:credit-card-outlined'
  }
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchTransactions(user.value.tenantId)
    list.setItems(store.transactions || [], (store.transactions || []).length)
  } finally { list.loading.value = false }
}

onMounted(load)
</script>

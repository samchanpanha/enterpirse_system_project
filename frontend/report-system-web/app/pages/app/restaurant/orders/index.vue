<template>
  <div>
    <AdminPageHeader title="Orders" :subtitle="`${list.items.value.length} orders`">
      <template #actions>
        <select
          v-model="list.filters.value.status"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All statuses
          </option>
          <option value="PENDING">
            Pending
          </option>
          <option value="PREPARING">
            Preparing
          </option>
          <option value="READY">
            Ready
          </option>
          <option value="SERVED">
            Served
          </option>
          <option value="COMPLETED">
            Completed
          </option>
          <option value="CANCELLED">
            Cancelled
          </option>
        </select>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by order ID or table…"
    />

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
      <template #cell-id="{ value }">
        <span class="font-mono text-xs text-text-primary">#{{ value.slice(0, 8) }}</span>
      </template>
      <template #cell-status="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', statusClass(value)]">
          <span :class="['w-1.5 h-1.5 rounded-full', statusDot(value)]" />
          {{ value }}
        </span>
      </template>
      <template #cell-totalAmount="{ value }">
        <span class="font-medium text-text-primary">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #cell-createdAt="{ value }">
        <span class="text-text-secondary text-xs">{{ new Date(value).toLocaleString() }}</span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="row.status === 'PENDING'"
          class="text-sm text-primary hover:text-primary-hover px-2 py-0.5"
          @click="updateStatus(row, 'PREPARING')"
        >
          Prepare
        </button>
        <button
          v-if="row.status === 'PREPARING'"
          class="text-sm text-warning hover:opacity-80 px-2 py-0.5"
          @click="updateStatus(row, 'READY')"
        >
          Ready
        </button>
        <button
          v-if="row.status === 'READY'"
          class="text-sm text-success hover:opacity-80 px-2 py-0.5"
          @click="updateStatus(row, 'SERVED')"
        >
          Serve
        </button>
        <button
          v-if="['SERVED', 'READY'].includes(row.status)"
          class="text-sm text-text-secondary hover:text-text-primary px-2 py-0.5"
          @click="updateStatus(row, 'COMPLETED')"
        >
          Complete
        </button>
      </template>
    </AdminTable>
  </div>
</template>

<script setup lang="ts">
import type { Order } from '~/shared/types/restaurant'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'

definePageMeta({ middleware: 'auth' })

const store = useRestaurantStore()
const { user } = useAuth()

const list = useListPage<Order>({
  pageSize: 20,
  initialFilters: { status: '' }
})

const columns: ColumnDef[] = [
  { key: 'id', title: 'Order', width: '120px' },
  { key: 'tableId', title: 'Table', width: '120px' },
  { key: 'status', title: 'Status', width: '140px' },
  { key: 'totalAmount', title: 'Total', align: 'right', width: '120px' },
  { key: 'createdAt', title: 'Created', width: '180px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  if (list.filters.value.status) {
    items = items.filter(o => o.status === list.filters.value.status)
  }
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(o =>
      o.id.toLowerCase().includes(q) ||
      o.tableId?.toLowerCase().includes(q)
    )
  }
  return items
})

function statusClass (status: string): string {
  switch (status) {
    case 'PENDING': return 'bg-warning-light text-warning'
    case 'PREPARING': return 'bg-primary-light text-primary'
    case 'READY': return 'bg-success-light text-success'
    case 'SERVED': return 'bg-info-light text-info'
    case 'COMPLETED': return 'bg-gray-100 text-text-secondary'
    case 'CANCELLED': return 'bg-danger-light text-danger'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

function statusDot (status: string): string {
  switch (status) {
    case 'PENDING': return 'bg-warning'
    case 'PREPARING': return 'bg-primary'
    case 'READY': return 'bg-success'
    case 'SERVED': return 'bg-info'
    case 'COMPLETED': return 'bg-text-disabled'
    case 'CANCELLED': return 'bg-danger'
    default: return 'bg-text-disabled'
  }
}

async function updateStatus (o: Order, newStatus: string) {
  try {
    const config = useRuntimeConfig()
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    if (user.value?.tenantId) { headers['X-Tenant-Id'] = user.value.tenantId }
    await $fetch(`${config.public.apiBaseUrl}/restaurant/orders/${o.id}/status`, {
      method: 'PUT',
      headers,
      body: { status: newStatus }
    })
    await load()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to update status')
  }
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchOrders(user.value.tenantId)
    const items = store.orders
    list.setItems(items || [], (items || []).length)
  } catch (e: any) {
    list.error.value = e?.data?.message || 'Failed to load orders'
  } finally {
    list.loading.value = false
  }
}

onMounted(load)
</script>

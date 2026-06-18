<template>
  <div>
    <AdminPageHeader title="Reconciliation" :subtitle="`${list.items.value.length} records`">
      <template #actions>
        <button
          v-if="can('payment.reconciliation.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
        >
          <Icon icon="ant-design:plus-outlined" />
          New
        </button>
      </template>
    </AdminPageHeader>

    <AdminTable
      :items="list.items.value"
      :columns="columns"
      :pagination="list.pagination"
      @page-change="list.setPage"
      @page-size-change="list.setPageSize"
    >
      <template #cell-period="{ value }">
        <span class="font-mono text-text-primary">{{ value }}</span>
      </template>
      <template #cell-matches="{ value }">
        <span class="text-success font-mono">{{ value }}</span>
      </template>
      <template #cell-mismatches="{ value }">
        <span class="text-danger font-mono">{{ value }}</span>
      </template>
      <template #cell-status="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', value === 'COMPLETED' ? 'bg-success-light text-success' : 'bg-warning-light text-warning']">
          {{ value }}
        </span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('payment.reconciliation.complete') && row.status !== 'COMPLETED'"
          class="text-sm text-success hover:opacity-80 px-2 py-0.5"
        >
          Complete
        </button>
      </template>
    </AdminTable>
  </div>
</template>

<script setup lang="ts">
import type { ReconciliationRecord } from '~/shared/types/payment'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'

definePageMeta({ middleware: 'auth' })

const store = usePaymentStore()
const { can } = usePermission()
const { user } = useAuth()
const list = useListPage<ReconciliationRecord>({ pageSize: 20 })

const columns: ColumnDef[] = [
  { key: 'statementDate', title: 'Date', width: '140px' },
  { key: 'gateway', title: 'Gateway', width: '140px' },
  { key: 'matchedCount', title: 'Matches', align: 'right', width: '100px' },
  { key: 'unmatchedCount', title: 'Mismatches', align: 'right', width: '120px' },
  { key: 'status', title: 'Status', width: '140px' }
]

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchReconciliations(user.value.tenantId)
    list.setItems(store.reconciliations || [], (store.reconciliations || []).length)
  } finally { list.loading.value = false }
}

onMounted(load)
</script>

<template>
  <div>
    <AdminPageHeader title="Payroll" :subtitle="`${list.items.value.length} payroll periods`">
      <template #actions>
        <button
          v-if="can('finance.payroll.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Period
        </button>
        <button
          v-if="can('finance.payroll.run')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 border border-primary text-primary text-sm rounded hover:bg-primary-light transition-colors"
        >
          <Icon icon="ant-design:play-circle-outlined" />
          Run Payroll
        </button>
      </template>
    </AdminPageHeader>

    <AdminTable
      :items="filteredItems"
      :columns="columns"
      :pagination="list.pagination"
      @page-change="list.setPage"
      @page-size-change="list.setPageSize"
    >
      <template #cell-period="{ value }">
        <span class="font-mono text-text-primary">{{ value }}</span>
      </template>
      <template #cell-totalGross="{ value }">
        <span class="font-medium text-text-primary">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #cell-totalNet="{ value }">
        <span class="font-medium text-success">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #cell-status="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', value === 'PAID' ? 'bg-success-light text-success' : value === 'APPROVED' ? 'bg-primary-light text-primary' : 'bg-warning-light text-warning']">
          {{ value }}
        </span>
      </template>
    </AdminTable>
  </div>
</template>

<script setup lang="ts">
import type { PayrollPeriod } from '~/shared/types/finance'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'

definePageMeta({ middleware: 'auth' })

const store = useFinanceStore()
const { can } = usePermission()
const list = useListPage<PayrollPeriod>({ pageSize: 12 })

const columns: ColumnDef[] = [
  { key: 'period', title: 'Period', width: '140px' },
  { key: 'employeeCount', title: 'Employees', align: 'right', width: '120px' },
  { key: 'totalGross', title: 'Gross', align: 'right', width: '140px' },
  { key: 'totalNet', title: 'Net', align: 'right', width: '140px' },
  { key: 'status', title: 'Status', width: '120px' }
]

const filteredItems = computed(() => list.items.value)

onMounted(() => {
  list.setItems(store.payrollPeriods || [], (store.payrollPeriods || []).length)
})
</script>

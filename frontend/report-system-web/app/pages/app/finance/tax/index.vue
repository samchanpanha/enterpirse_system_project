<template>
  <div>
    <AdminPageHeader title="Tax" :subtitle="`${records.length} tax records`">
      <template #actions>
        <button
          v-if="can('finance.tax.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Record
        </button>
      </template>
    </AdminPageHeader>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 p-6">
      <div class="bg-white rounded shadow-card p-5">
        <div class="text-sm text-text-secondary">
          Total tax (YTD)
        </div>
        <div class="text-2xl font-semibold text-text-primary mt-1">
          ${{ totalTax.toFixed(2) }}
        </div>
      </div>
      <div class="bg-white rounded shadow-card p-5">
        <div class="text-sm text-text-secondary">
          Records
        </div>
        <div class="text-2xl font-semibold text-text-primary mt-1">
          {{ records.length }}
        </div>
      </div>
      <div class="bg-white rounded shadow-card p-5">
        <div class="text-sm text-text-secondary">
          Tax types
        </div>
        <div class="text-2xl font-semibold text-text-primary mt-1">
          {{ uniqueTypes }}
        </div>
      </div>
    </div>

    <AdminTable
      :items="records"
      :columns="columns"
      :pagination="{ page: 1, pageSize: 20, total: records.length, totalPages: 1 }"
    >
      <template #cell-taxType="{ value }">
        <span class="text-text-primary font-medium">{{ value }}</span>
      </template>
      <template #cell-period="{ row }">
        <span class="font-mono text-text-secondary">{{ String(row.periodMonth).padStart(2, '0') }}/{{ row.periodYear }}</span>
      </template>
      <template #cell-taxAmount="{ value }">
        <span class="font-medium text-text-primary">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #cell-taxRate="{ value }">
        <span class="text-text-secondary text-xs">{{ (Number(value) * 100).toFixed(1) }}%</span>
      </template>
    </AdminTable>
  </div>
</template>

<script setup lang="ts">
import type { TaxRecord } from '~/shared/types/finance'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'

definePageMeta({ middleware: 'auth' })

const store = useFinanceStore()
const { can } = usePermission()
const { user } = useAuth()
const records = ref<TaxRecord[]>([])

const columns: ColumnDef[] = [
  { key: 'taxType', title: 'Tax type' },
  { key: 'period', title: 'Period', width: '120px' },
  { key: 'taxableAmount', title: 'Taxable', align: 'right', width: '140px' },
  { key: 'taxRate', title: 'Rate', align: 'right', width: '100px' },
  { key: 'taxAmount', title: 'Amount', align: 'right', width: '140px' }
]

const totalTax = computed(() => records.value.reduce((s, r) => s + Number(r.taxAmount || 0), 0))
const uniqueTypes = computed(() => new Set(records.value.map(r => r.taxType)).size)

async function load () {
  if (!user.value?.tenantId) { return }
  try {
    const now = new Date()
    await store.fetchTaxRecords(user.value.tenantId, '', now.getFullYear(), now.getMonth() + 1)
    records.value = store.taxRecords || []
  } catch (e) { records.value = [] }
}

onMounted(load)
</script>

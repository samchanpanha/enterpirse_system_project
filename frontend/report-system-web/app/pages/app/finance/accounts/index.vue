<template>
  <div>
    <AdminPageHeader
      title="Chart of Accounts"
      :subtitle="`${list.items.value.length} accounts`"
    >
      <template #actions>
        <button
          v-if="can('finance.account.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="openCreate()"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Account
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by code or name…"
      :has-active-filters="list.filters.value.type"
      @reset="list.resetFilters()"
    >
      <template #filters>
        <select
          v-model="list.filters.value.type"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All types
          </option>
          <option value="ASSET">
            Asset
          </option>
          <option value="LIABILITY">
            Liability
          </option>
          <option value="EQUITY">
            Equity
          </option>
          <option value="REVENUE">
            Revenue
          </option>
          <option value="EXPENSE">
            Expense
          </option>
        </select>
      </template>
    </AdminSearchBar>

    <AdminTable
      :items="filteredItems"
      :columns="columns"
      :loading="list.loading.value"
      :pagination="list.pagination"
      @page-change="list.setPage"
      @page-size-change="list.setPageSize"
    >
      <template #cell-code="{ value }">
        <span class="font-mono text-text-primary">{{ value }}</span>
      </template>
      <template #cell-type="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', typeClass(value)]">
          {{ value }}
        </span>
      </template>
      <template #cell-balance="{ value }">
        <span class="font-medium text-text-primary">${{ Number(value).toFixed(2) }}</span>
      </template>
    </AdminTable>
  </div>
</template>

<script setup lang="ts">
import type { Account } from '~/shared/types/finance'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'

definePageMeta({ middleware: 'auth' })

const store = useFinanceStore()
const { can } = usePermission()
const { user } = useAuth()

const list = useListPage<Account>({
  pageSize: 25,
  initialFilters: { type: '' }
})

const columns: ColumnDef[] = [
  { key: 'code', title: 'Code', width: '120px' },
  { key: 'name', title: 'Name', sortable: true },
  { key: 'type', title: 'Type', width: '120px' },
  { key: 'balance', title: 'Balance', align: 'right', width: '160px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  if (list.filters.value.type) { items = items.filter(a => a.type === list.filters.value.type) }
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(a =>
      a.code?.toLowerCase().includes(q) ||
      a.name.toLowerCase().includes(q)
    )
  }
  return items
})

function typeClass (t: string): string {
  switch (t) {
    case 'ASSET': return 'bg-primary-light text-primary'
    case 'LIABILITY': return 'bg-danger-light text-danger'
    case 'EQUITY': return 'bg-info-light text-info'
    case 'REVENUE': return 'bg-success-light text-success'
    case 'EXPENSE': return 'bg-warning-light text-warning'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

function openCreate () { /* TODO */ }

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchAccounts(user.value.tenantId)
    list.setItems(store.accounts || [], (store.accounts || []).length)
  } finally { list.loading.value = false }
}

onMounted(load)
</script>

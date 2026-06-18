<template>
  <div>
    <AdminPageHeader title="Employees" :subtitle="`${list.items.value.length} employees on file`">
      <template #actions>
        <button
          v-if="can('finance.employee.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Employee
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by name or email…"
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
      <template #cell-name="{ row }">
        <span class="font-medium text-text-primary">{{ row.firstName }} {{ row.lastName }}</span>
      </template>
      <template #cell-position="{ value }">
        <span class="text-text-primary">{{ value }}</span>
      </template>
      <template #cell-baseSalary="{ value }">
        <span class="font-medium text-text-primary">{{ value ? '$' + Number(value).toFixed(2) : '—' }}</span>
      </template>
      <template #cell-active="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', value ? 'bg-success-light text-success' : 'bg-gray-100 text-text-secondary']">
          <span :class="['w-1.5 h-1.5 rounded-full', value ? 'bg-success' : 'bg-text-disabled']" />
          {{ value ? 'Active' : 'Inactive' }}
        </span>
      </template>
    </AdminTable>
  </div>
</template>

<script setup lang="ts">
import type { Employee } from '~/shared/types/finance'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'

definePageMeta({ middleware: 'auth' })

const store = useFinanceStore()
const { can } = usePermission()
const { user } = useAuth()

const list = useListPage<Employee>({ pageSize: 20 })

const columns: ColumnDef[] = [
  { key: 'name', title: 'Name', sortable: true },
  { key: 'position', title: 'Position', width: '200px' },
  { key: 'email', title: 'Email', width: '220px' },
  { key: 'baseSalary', title: 'Salary', align: 'right', width: '140px' },
  { key: 'active', title: 'Status', width: '120px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter((e) => {
      const fullName = `${e.firstName || ''} ${e.lastName || ''}`.toLowerCase()
      return fullName.includes(q) || e.email?.toLowerCase().includes(q)
    })
  }
  return items
})

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchEmployees(user.value.tenantId)
    list.setItems(store.employees || [], (store.employees || []).length)
  } finally { list.loading.value = false }
}

onMounted(load)
</script>

<template>
  <div>
    <AdminPageHeader title="Dashboards" :subtitle="`${list.items.value.length} dashboards`">
      <template #actions>
        <button
          v-if="can('dashboard.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Dashboard
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search dashboards…"
    />

    <AdminTable
      :items="filteredItems"
      :columns="columns"
      :pagination="list.pagination"
      @page-change="list.setPage"
      @page-size-change="list.setPageSize"
    >
      <template #cell-name="{ value }">
        <span class="font-medium text-text-primary">{{ value }}</span>
      </template>
      <template #cell-widgets="{ value }">
        <span class="text-text-secondary text-xs">{{ value || 0 }} widgets</span>
      </template>
    </AdminTable>
  </div>
</template>

<script setup lang="ts">
import type { DashboardConfig } from '~/shared/types/reporting'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'

definePageMeta({ middleware: 'auth' })

const store = useReportingStore()
const { can } = usePermission()
const { user } = useAuth()
const list = useListPage<DashboardConfig>({ pageSize: 20 })

const columns: ColumnDef[] = [
  { key: 'name', title: 'Name', sortable: true },
  { key: 'widgets', title: 'Widgets', width: '120px' },
  { key: 'createdAt', title: 'Created', width: '180px' }
]

const filteredItems = computed(() => {
  const q = list.search.value.toLowerCase()
  if (!q) { return list.items.value }
  return list.items.value.filter(d => d.name.toLowerCase().includes(q))
})

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchDashboards(user.value.tenantId)
    list.setItems((store.dashboards as any) || [], (store.dashboards || []).length)
  } finally { list.loading.value = false }
}

onMounted(load)
</script>

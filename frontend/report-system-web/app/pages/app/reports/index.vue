<template>
  <div>
    <AdminPageHeader title="Report Definitions" :subtitle="`${list.items.value.length} reports`">
      <template #actions>
        <button
          v-if="can('report.definition.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Report
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search reports…"
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
      <template #cell-name="{ value }">
        <span class="font-medium text-text-primary">{{ value }}</span>
      </template>
      <template #cell-type="{ value }">
        <span class="inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded bg-info-light text-info">
          {{ value }}
        </span>
      </template>
      <template #actions>
        <button
          v-if="can('report.definition.run')"
          class="text-sm text-primary hover:text-primary-hover px-2 py-0.5"
        >
          Run
        </button>
      </template>
    </AdminTable>
  </div>
</template>

<script setup lang="ts">
import type { ReportDefinition } from '~/shared/types/reporting'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'

definePageMeta({ middleware: 'auth' })

const store = useReportingStore()
const { can } = usePermission()
const { user } = useAuth()
const list = useListPage<ReportDefinition>({ pageSize: 20 })

const columns: ColumnDef[] = [
  { key: 'name', title: 'Name', sortable: true },
  { key: 'type', title: 'Type', width: '140px' },
  { key: 'createdAt', title: 'Created', width: '180px' }
]

const filteredItems = computed(() => {
  const q = list.search.value.toLowerCase()
  if (!q) { return list.items.value }
  return list.items.value.filter(r => r.name.toLowerCase().includes(q))
})

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchDefinitions(user.value.tenantId)
    list.setItems(store.definitions || [], (store.definitions || []).length)
  } finally { list.loading.value = false }
}

onMounted(load)
</script>

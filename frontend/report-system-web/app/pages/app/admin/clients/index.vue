<template>
  <div>
    <AdminPageHeader
      title="Clients"
      subtitle="SaaS tenants. The demo-corp tenant is your current organisation."
    >
      <template #actions>
        <button
          v-if="can('clients.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="openCreate()"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Client
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by name or slug…"
      :has-active-filters="list.filters.value.tier"
      @reset="list.resetFilters()"
    >
      <template #filters>
        <select
          v-model="list.filters.value.tier"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All tiers
          </option>
          <option value="BASIC">
            Basic
          </option>
          <option value="PRO">
            Pro
          </option>
          <option value="ENTERPRISE">
            Enterprise
          </option>
        </select>
      </template>
    </AdminSearchBar>

    <AdminTable
      :items="list.items.value"
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
      <template #cell-name="{ value, row }">
        <div>
          <p class="font-medium text-text-primary">
            {{ value }}
          </p>
          <p class="text-xs text-text-secondary">
            {{ row.slug }}
          </p>
        </div>
      </template>
      <template #cell-tier="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', tierClass(value)]">
          <Icon :icon="tierIcon(value)" />
          {{ value }}
        </span>
      </template>
      <template #cell-active="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', value ? 'bg-success-light text-success' : 'bg-gray-100 text-text-secondary']">
          <span :class="['w-1.5 h-1.5 rounded-full', value ? 'bg-success' : 'bg-text-disabled']" />
          {{ value ? 'Active' : 'Inactive' }}
        </span>
      </template>
      <template #actions>
        <button
          v-if="can('clients.update')"
          class="text-sm text-primary hover:text-primary-hover px-2 py-0.5"
        >
          Manage
        </button>
      </template>
    </AdminTable>
  </div>
</template>

<script setup lang="ts">
import type { ColumnDef } from '~/components/admin/AdminTable.vue'

definePageMeta({ middleware: 'auth' })

const { can } = usePermission()
const { user } = useAuth()
const list = useListPage<any>({
  pageSize: 20,
  initialFilters: { tier: '' }
})

const columns: ColumnDef[] = [
  { key: 'name', title: 'Name', sortable: true },
  { key: 'tier', title: 'Tier', width: '140px' },
  { key: 'active', title: 'Status', width: '120px' },
  { key: 'createdAt', title: 'Created', width: '180px' }
]

function tierClass (tier: string): string {
  switch (tier) {
    case 'BASIC': return 'bg-info-light text-info'
    case 'PRO': return 'bg-primary-light text-primary'
    case 'ENTERPRISE': return 'bg-warning-light text-warning'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

function tierIcon (tier: string): string {
  switch (tier) {
    case 'BASIC': return 'ant-design:star-outlined'
    case 'PRO': return 'ant-design:star-filled'
    case 'ENTERPRISE': return 'ant-design:crown-outlined'
    default: return 'ant-design:question-circle-outlined'
  }
}

function openCreate () { /* TODO */ }

function load () {
  // For now: show the current tenant as the only client (it's your organisation)
  if (!user.value) { return }
  list.setItems([{
    id: user.value.tenantId,
    name: user.value.tenantName || 'Demo Corp',
    slug: 'demo-corp',
    tier: 'PRO',
    active: true,
    createdAt: new Date().toISOString()
  }], 1)
  list.loading.value = false
}

onMounted(load)
</script>

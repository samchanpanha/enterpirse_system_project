<template>
  <div>
    <AdminPageHeader
      title="Permissions"
      subtitle="Permission catalog used by roles. System permissions are seeded; add custom ones as needed."
    >
      <template #actions>
        <button
          v-if="can('permissions.write')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="drawer.openFor(null)"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Permission
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by code or name…"
      :has-active-filters="!!list.filters.value.module"
      @reset="list.resetFilters()"
    >
      <template #filters>
        <select
          v-model="list.filters.value.module"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All modules
          </option>
          <option v-for="mod in modules" :key="mod" :value="mod">
            {{ mod }}
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
        <span class="font-mono text-xs text-text-secondary">{{ value }}</span>
      </template>
      <template #cell-module="{ value }">
        <span class="text-xs bg-gray-100 text-text-secondary px-1.5 py-0.5 rounded capitalize">{{ value }}</span>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      title="New Permission"
      width="md"
    >
      <AdminForm
        v-if="drawer.open.value"
        v-model="formData"
        :groups="formGroups"
      />
      <p v-if="drawer.error.value" class="mt-3 text-sm text-danger bg-danger-light p-2 rounded">
        {{ drawer.error.value }}
      </p>
      <template #footer>
        <button class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50" @click="drawer.close()">
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50"
          :disabled="drawer.saving.value"
          @click="submitForm"
        >
          {{ drawer.saving.value ? 'Saving…' : 'Create' }}
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import type { FormGroup } from '~/shared/types/form'
import type { Permission } from '~/shared/types/auth'

definePageMeta({ middleware: 'auth' })

const { can } = usePermission()
const config = useRuntimeConfig()
const list = useListPage<Permission>({ pageSize: 30, initialFilters: { module: '' } })
const drawer = useDrawer<Permission>()

const columns: ColumnDef[] = [
  { key: 'code', title: 'Code', sortable: true, width: '280px' },
  { key: 'name', title: 'Name', sortable: true },
  { key: 'module', title: 'Module', width: '140px' }
]

const modules = computed(() => [...new Set(list.items.value.map(p => p.module).filter(Boolean))].sort())

const filteredItems = computed(() => {
  let items = list.items.value
  if (list.search.value) {
    const q = list.search.value.toLowerCase()
    items = items.filter(p => p.code.toLowerCase().includes(q) || (p.name || '').toLowerCase().includes(q))
  }
  if (list.filters.value.module) {
    items = items.filter(p => p.module === list.filters.value.module)
  }
  return items
})

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  {
    title: 'Permission',
    fields: [
      { key: 'code', label: 'Code', type: 'text', required: true, placeholder: 'module.action', span: 12 },
      { key: 'name', label: 'Name', type: 'text', required: true, span: 12 },
      { key: 'module', label: 'Module', type: 'text', required: true, placeholder: 'admin', span: 12 }
    ]
  }
])

async function fetchPermissions () {
  list.loading.value = true
  try {
    const res: any = await $fetch(`${config.public.apiBaseUrl}/api/permissions`, {
      headers: { Authorization: `Bearer ${useAuth().token.value}` }
    })
    const items = Array.isArray(res) ? res : []
    list.setItems(items, items.length)
  } catch (e: any) {
    list.error.value = e?.data?.message || 'Failed to load permissions'
  } finally {
    list.loading.value = false
  }
}

async function submitForm () {
  const f = drawer.formData.value
  if (!f.code || !f.name || !f.module) {
    drawer.error.value = 'All fields are required'
    return
  }
  drawer.saving.value = true
  drawer.error.value = null
  try {
    await $fetch(`${config.public.apiBaseUrl}/api/permissions`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${useAuth().token.value}`, 'Content-Type': 'application/json' },
      body: {
        code: f.code,
        name: f.name,
        module: f.module
      }
    })
    await fetchPermissions()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.data?.message || 'Failed to create permission'
  } finally {
    drawer.saving.value = false
  }
}

onMounted(fetchPermissions)
</script>

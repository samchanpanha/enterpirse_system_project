<template>
  <div>
    <AdminPageHeader title="Branches" :subtitle="`Manage branches for ${tenantLabel}`">
      <template #actions>
        <button
          v-if="canCreate"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover disabled:opacity-50 transition-colors"
          :disabled="drawer.saving.value"
          @click="drawer.openFor(null)"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Branch
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by code or name…"
      :has-active-filters="hasActiveFilters"
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
          <option value="HQ">
            HQ
          </option>
          <option value="STORE">
            Store
          </option>
          <option value="WAREHOUSE">
            Warehouse
          </option>
          <option value="RESTAURANT">
            Restaurant
          </option>
          <option value="KIOSK">
            Kiosk
          </option>
        </select>
        <select
          v-model="list.filters.value.active"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All
          </option>
          <option value="true">
            Active only
          </option>
          <option value="false">
            Inactive only
          </option>
        </select>
      </template>
      <template #actions>
        <button
          v-if="list.hasSelection.value"
          class="text-sm text-text-secondary hover:text-danger px-2 py-1.5 transition-colors"
          @click="confirmBulkDelete"
        >
          Delete ({{ list.selectionCount.value }})
        </button>
      </template>
    </AdminSearchBar>

    <AdminTable
      :items="filteredItems"
      :columns="columns"
      :loading="list.loading.value"
      :pagination="list.pagination"
      :selectable="true"
      :selected-ids="list.selectedIds.value"
      @sort="onSort"
      @page-change="list.setPage"
      @page-size-change="list.setPageSize"
      @toggle-select="(row) => list.toggleSelect(row.id)"
      @toggle-all="list.isAllSelected() ? list.clearSelection() : list.selectAll()"
    >
      <template #cell-code="{ row, value }">
        <div class="flex items-center gap-2">
          <span class="font-mono text-text-primary">{{ value }}</span>
          <span
            v-if="row.default"
            class="text-xs bg-primary-light text-primary px-1.5 py-0.5 rounded"
          >
            DEFAULT
          </span>
          <span
            v-if="!row.active"
            class="text-xs bg-gray-100 text-text-secondary px-1.5 py-0.5 rounded"
          >
            INACTIVE
          </span>
        </div>
      </template>
      <template #cell-name="{ value }">
        <span class="font-medium text-text-primary">{{ value }}</span>
      </template>
      <template #cell-branchType="{ value }">
        <span class="inline-flex items-center gap-1 text-xs text-text-secondary">
          <Icon :icon="branchTypeIcon(value)" />
          {{ value }}
        </span>
      </template>
      <template #cell-currency="{ value, row }">
        <span class="text-text-secondary">{{ value }} · {{ row.timezone }}</span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="row.id !== currentBranchId"
          class="text-sm text-primary hover:text-primary-hover px-2 py-0.5"
          @click="switchTo(row.id)"
        >
          Switch
        </button>
        <button
          v-if="canUpdate"
          class="text-sm text-text-secondary hover:text-primary px-2 py-0.5"
          @click="drawer.openFor(row)"
        >
          Edit
        </button>
        <button
          v-if="canDelete && !row.default"
          class="text-sm text-danger hover:opacity-80 px-2 py-0.5"
          @click="del(row)"
        >
          Delete
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      :title="drawer.isEdit() ? 'Edit Branch' : 'New Branch'"
      :subtitle="drawer.isEdit() ? drawer.editing.value?.code : 'Create a new branch location'"
      width="lg"
    >
      <AdminForm
        v-if="drawer.open.value"
        v-model="formData"
        :groups="formGroups"
        @submit="save"
      />

      <p
        v-if="drawer.error.value"
        class="mt-3 text-sm text-danger flex items-center gap-1 bg-danger-light p-2 rounded"
      >
        <Icon icon="ant-design:exclamation-circle-outlined" />
        {{ drawer.error.value }}
      </p>

      <template #footer>
        <button
          type="button"
          class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50 transition-colors"
          @click="drawer.close()"
        >
          Cancel
        </button>
        <button
          type="button"
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50 transition-colors inline-flex items-center gap-1.5"
          :disabled="drawer.saving.value"
          @click="submitForm"
        >
          <Icon v-if="drawer.saving.value" icon="ant-design:loading-outlined" class="animate-spin" />
          {{ drawer.saving.value ? 'Saving…' : (drawer.isEdit() ? 'Update' : 'Create') }}
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { Branch } from '~/shared/types/branch'
import type { FormGroup } from '~/shared/types/form'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'

definePageMeta({ middleware: 'auth' })

const { user } = useAuth()
const { can } = usePermission()
const branchStore = useBranchStore()
const { currentBranchId } = storeToRefs(branchStore)

const canCreate = computed(() => can('branches.create'))
const canUpdate = computed(() => can('branches.update'))
const canDelete = computed(() => can('branches.delete'))

const tenantLabel = computed(() => {
  if (user.value?.tenantName) { return user.value.tenantName }
  if (user.value?.tenantId) { return user.value.tenantId.slice(0, 8) + '…' }
  return 'your tenant'
})

const list = useListPage<Branch>({
  pageSize: 12,
  initialFilters: { type: '', active: '' }
})

const drawer = useDrawer<Branch>()

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  {
    title: 'Identity',
    fields: [
      { key: 'code', label: 'Code', type: 'text', required: true, placeholder: 'BR01', disabled: drawer.isEdit(), span: 6 },
      {
        key: 'branchType',
        label: 'Type',
        type: 'select',
        required: true,
        span: 6,
        options: [
          { label: 'HQ', value: 'HQ' },
          { label: 'Store', value: 'STORE' },
          { label: 'Warehouse', value: 'WAREHOUSE' },
          { label: 'Restaurant', value: 'RESTAURANT' },
          { label: 'Kiosk', value: 'KIOSK' }
        ]
      },
      { key: 'name', label: 'Name', type: 'text', required: true, placeholder: 'Branch BKK1', span: 6 },
      { key: 'nameKh', label: 'Name (Khmer)', type: 'text', placeholder: 'សាខា', span: 6 }
    ]
  },
  {
    title: 'Location',
    fields: [
      { key: 'address', label: 'Address', type: 'text', span: 12 },
      { key: 'city', label: 'City', type: 'text', placeholder: 'Phnom Penh', span: 4 },
      { key: 'district', label: 'District', type: 'text', span: 4 },
      { key: 'province', label: 'Province', type: 'text', span: 4 }
    ]
  },
  {
    title: 'Contact',
    fields: [
      { key: 'phone', label: 'Phone', type: 'tel', span: 6 },
      { key: 'email', label: 'Email', type: 'email', span: 6 }
    ]
  },
  {
    title: 'Settings',
    fields: [
      { key: 'currency', label: 'Currency', type: 'text', placeholder: 'USD', span: 6 },
      { key: 'taxRate', label: 'Tax rate (%)', type: 'number', placeholder: '10', step: 0.01, min: 0, max: 100, span: 6 },
      { key: 'active', label: 'Active', type: 'switch', span: 6 },
      { key: 'default', label: 'Default branch', type: 'switch', span: 6 }
    ]
  }
])

const columns: ColumnDef[] = [
  { key: 'code', title: 'Code', sortable: true, width: '180px' },
  { key: 'name', title: 'Name', sortable: true },
  { key: 'branchType', title: 'Type', width: '140px' },
  { key: 'city', title: 'Location', width: '180px' },
  { key: 'phone', title: 'Phone', width: '160px' },
  { key: 'currency', title: 'Currency · TZ', width: '200px' }
]

const hasActiveFilters = computed(() => {
  return !!list.filters.value.type || !!list.filters.value.active || !!list.search.value
})

const filteredItems = computed(() => {
  let items = list.items.value
  if (list.search.value) {
    const q = list.search.value.toLowerCase()
    items = items.filter(b =>
      b.code.toLowerCase().includes(q) || b.name.toLowerCase().includes(q)
    )
  }
  if (list.filters.value.type) {
    items = items.filter(b => b.branchType === list.filters.value.type)
  }
  if (list.filters.value.active === 'true') { items = items.filter(b => b.active) }
  if (list.filters.value.active === 'false') { items = items.filter(b => !b.active) }
  return items
})

function onSort (field: string) {
  list.setSort(field)
}

function branchTypeIcon (type: string): string {
  switch (type) {
    case 'HQ': return 'ant-design:bank-outlined'
    case 'STORE': return 'ant-design:shop-outlined'
    case 'WAREHOUSE': return 'ant-design:home-outlined'
    case 'RESTAURANT': return 'ant-design:coffee-outlined'
    case 'KIOSK': return 'ant-design:appstore-outlined'
    default: return 'ant-design:environment-outlined'
  }
}

async function fetchBranches () {
  list.loading.value = true
  list.error.value = null
  try {
    const config = useRuntimeConfig()
    const headers: Record<string, string> = {}
    if (user.value?.tenantId) { headers['X-Tenant-Id'] = user.value.tenantId }
    const res: any = await $fetch(`${config.public.apiBaseUrl}/api/branches`, { headers })
    const items = Array.isArray(res) ? res : (res?.content || [])
    list.setItems(items, items.length)
  } catch (e: any) {
    list.error.value = e?.data?.message || 'Failed to load branches'
  } finally {
    list.loading.value = false
  }
}

async function submitForm () {
  if (!validateForm()) { return }
  await save(formData.value)
}

function validateForm (): boolean {
  drawer.error.value = null
  const f = formData.value
  if (!f.code) { drawer.error.value = 'Code is required'; return false }
  if (!f.name) { drawer.error.value = 'Name is required'; return false }
  if (f.taxRate !== null && f.taxRate !== undefined && (f.taxRate < 0 || f.taxRate > 100)) {
    drawer.error.value = 'Tax rate must be between 0 and 100'
    return false
  }
  return true
}

async function save (data: any) {
  drawer.saving.value = true
  drawer.error.value = null
  try {
    const config = useRuntimeConfig()
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    if (user.value?.tenantId) { headers['X-Tenant-Id'] = user.value.tenantId }
    if (currentBranchId.value) { headers['X-Branch-Id'] = currentBranchId.value }
    const body = {
      code: data.code,
      name: data.name,
      nameKh: data.nameKh || null,
      branchType: data.branchType,
      address: data.address || null,
      city: data.city || null,
      district: data.district || null,
      province: data.province || null,
      phone: data.phone || null,
      email: data.email || null,
      currency: data.currency || 'USD',
      taxRate: data.taxRate ?? null,
      active: data.active ?? true,
      isDefault: data.default ?? false
    }
    if (drawer.isEdit() && drawer.editing.value) {
      await $fetch(`${config.public.apiBaseUrl}/api/branches/${drawer.editing.value.id}`, {
        method: 'PUT', headers, body
      })
    } else {
      await $fetch(`${config.public.apiBaseUrl}/api/branches`, {
        method: 'POST', headers, body
      })
    }
    await fetchBranches()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.response?.data?.message || e?.data?.message || 'Failed to save branch'
  } finally {
    drawer.saving.value = false
  }
}

async function del (b: Branch) {
  if (!confirm(`Delete branch "${b.name}"?`)) { return }
  try {
    const config = useRuntimeConfig()
    const headers: Record<string, string> = {}
    if (user.value?.tenantId) { headers['X-Tenant-Id'] = user.value.tenantId }
    await $fetch(`${config.public.apiBaseUrl}/api/branches/${b.id}`, { method: 'DELETE', headers })
    await fetchBranches()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to delete branch')
  }
}

async function confirmBulkDelete () {
  if (!confirm(`Delete ${list.selectionCount.value} selected branches?`)) { return }
  for (const b of list.selectedItems.value) {
    if (b.default) { continue }
    try {
      const config = useRuntimeConfig()
      const headers: Record<string, string> = {}
      if (user.value?.tenantId) { headers['X-Tenant-Id'] = user.value.tenantId }
      await $fetch(`${config.public.apiBaseUrl}/api/branches/${b.id}`, { method: 'DELETE', headers })
    } catch (e) { /* swallow */ }
  }
  list.clearSelection()
  await fetchBranches()
}

function switchTo (id: string) {
  branchStore.setCurrentBranch(id)
  reloadNuxtApp({ path: useRoute().fullPath, ttl: 100 })
}

onMounted(async () => {
  await fetchBranches()
})
</script>

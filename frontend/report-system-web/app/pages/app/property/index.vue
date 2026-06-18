<template>
  <div>
    <AdminPageHeader
      title="Properties"
      :subtitle="`${store.properties.length} property on file`"
    >
      <template #actions>
        <button
          v-if="can('property.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="drawer.openFor(null)"
        >
          <Icon icon="ant-design:plus-outlined" />
          Add Property
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by name, address, or city…"
      :has-active-filters="list.filters.value.type || list.filters.value.status"
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
          <option value="apartment">
            Apartment
          </option>
          <option value="house">
            House
          </option>
          <option value="condo">
            Condo
          </option>
          <option value="commercial">
            Commercial
          </option>
        </select>
        <select
          v-model="list.filters.value.status"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All statuses
          </option>
          <option value="active">
            Active
          </option>
          <option value="inactive">
            Inactive
          </option>
        </select>
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
      <template #cell-name="{ row, value }">
        <NuxtLink
          :to="`/app/property/${row.id}`"
          class="font-medium text-primary hover:text-primary-hover inline-flex items-center gap-1"
        >
          {{ value }}
          <Icon icon="ant-design:arrow-right-outlined" class="text-xs" />
        </NuxtLink>
      </template>
      <template #cell-type="{ value }">
        <span class="inline-flex items-center gap-1 text-xs text-text-secondary">
          <Icon :icon="typeIcon(value)" />
          {{ value }}
        </span>
      </template>
      <template #cell-status="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', statusClass(value)]">
          <span :class="['w-1.5 h-1.5 rounded-full', value === 'active' ? 'bg-success' : 'bg-text-disabled']" />
          {{ value }}
        </span>
      </template>
      <template #cell-totalUnits="{ value }">
        <span class="font-mono text-text-primary">{{ value }}</span>
        <span class="text-text-secondary text-xs ml-1">units</span>
      </template>
      <template #actions="{ row }">
        <NuxtLink
          :to="`/app/property/${row.id}`"
          class="text-sm text-text-secondary hover:text-primary px-2 py-0.5"
        >
          View
        </NuxtLink>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      :title="drawer.isEdit() ? 'Edit Property' : 'New Property'"
      :subtitle="drawer.isEdit() ? `Editing ${drawer.editing.value?.name}` : 'Add a new property to your portfolio'"
      width="md"
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
import type { Property } from '~/shared/types/property'
import type { FormGroup } from '~/shared/types/form'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import { useFormSchema } from '~/composables/useFormSchema'

definePageMeta({ middleware: 'auth' })

const store = usePropertyStore()
const { can } = usePermission()
const { user } = useAuth()
const { text, number, select, switchField, group, opt } = useFormSchema()

const list = useListPage<Property>({
  pageSize: 12,
  initialFilters: { type: '', status: '' }
})

const drawer = useDrawer<Property>()

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  group('Identity', [
    text('name', 'Name', { required: true, placeholder: 'Sunrise Apartments' }),
    select('type', 'Type', [
      opt('apartment', 'Apartment'),
      opt('house', 'House'),
      opt('condo', 'Condo'),
      opt('commercial', 'Commercial')
    ], { required: true, span: 6 }),
    number('totalUnits', 'Total units', { min: 1, span: 6 })
  ]),
  group('Location', [
    text('address', 'Address', { required: true, span: 12 }),
    text('city', 'City', { required: true, span: 6 }),
    text('district', 'District', { span: 6 })
  ]),
  group('Status', [
    switchField('status', 'Active', { hidden: () => !drawer.isEdit() })
  ])
])

const columns: ColumnDef[] = [
  { key: 'name', title: 'Name', sortable: true },
  { key: 'type', title: 'Type', width: '140px' },
  { key: 'address', title: 'Address' },
  { key: 'city', title: 'City', width: '160px' },
  { key: 'totalUnits', title: 'Units', align: 'right', width: '100px' },
  { key: 'status', title: 'Status', width: '120px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(p =>
      p.name.toLowerCase().includes(q) ||
      p.address?.toLowerCase().includes(q) ||
      p.city?.toLowerCase().includes(q)
    )
  }
  if (list.filters.value.type) {
    items = items.filter(p => p.type === list.filters.value.type)
  }
  if (list.filters.value.status) {
    items = items.filter(p => p.status === list.filters.value.status)
  }
  return items
})

function onSort (field: string) {
  list.setSort(field)
}

function typeIcon (type: string): string {
  switch (type) {
    case 'apartment': return 'ant-design:home-outlined'
    case 'house': return 'ant-design:bank-outlined'
    case 'condo': return 'ant-design:build-outlined'
    case 'commercial': return 'ant-design:shop-outlined'
    default: return 'ant-design:environment-outlined'
  }
}

function statusClass (status: string): string {
  return status === 'active'
    ? 'bg-success-light text-success'
    : 'bg-gray-100 text-text-secondary'
}

async function loadProperties () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  list.error.value = null
  try {
    await store.fetchProperties(user.value.tenantId)
    const items = store.properties
    list.setItems(items || [], (items || []).length)
  } catch (e: any) {
    list.error.value = e?.data?.message || 'Failed to load properties'
  } finally {
    list.loading.value = false
  }
}

function validate (): boolean {
  drawer.error.value = null
  const f = formData.value
  if (!f.name) { drawer.error.value = 'Name is required'; return false }
  if (!f.address) { drawer.error.value = 'Address is required'; return false }
  if (!f.city) { drawer.error.value = 'City is required'; return false }
  return true
}

function submitForm () {
  if (!validate()) { return }
  save(formData.value)
}

async function save (data: any) {
  drawer.saving.value = true
  drawer.error.value = null
  try {
    const body = {
      tenantId: user.value!.tenantId,
      ...data
    }
    if (drawer.isEdit() && drawer.editing.value) {
      // TODO: wire update endpoint
    } else {
      await store.createProperty(body as any)
    }
    await loadProperties()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.data?.message || 'Failed to save property'
  } finally {
    drawer.saving.value = false
  }
}

onMounted(async () => {
  await loadProperties()
})
</script>

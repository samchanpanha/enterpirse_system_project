<template>
  <div>
    <AdminPageHeader
      title="Suppliers"
      :subtitle="`${list.items.value.length} supplier on file`"
    >
      <template #actions>
        <button
          v-if="can('inventory.supplier.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="drawer.openFor(null)"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Supplier
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by name, phone, or email…"
      :has-active-filters="list.filters.value.status"
      @reset="list.resetFilters()"
    >
      <template #filters>
        <select
          v-model="list.filters.value.status"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All
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
      <template #cell-name="{ value }">
        <span class="font-medium text-text-primary">{{ value }}</span>
      </template>
      <template #cell-phone="{ value }">
        <a :href="`tel:${value}`" class="text-primary hover:text-primary-hover">{{ value }}</a>
      </template>
      <template #cell-email="{ value }">
        <a v-if="value" :href="`mailto:${value}`" class="text-primary hover:text-primary-hover">{{ value }}</a>
        <span v-else class="text-text-disabled">—</span>
      </template>
      <template #cell-active="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', value ? 'bg-success-light text-success' : 'bg-gray-100 text-text-secondary']">
          <span :class="['w-1.5 h-1.5 rounded-full', value ? 'bg-success' : 'bg-text-disabled']" />
          {{ value ? 'Active' : 'Inactive' }}
        </span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('inventory.supplier.update')"
          class="text-sm text-text-secondary hover:text-primary px-2 py-0.5"
          @click="drawer.openFor(row)"
        >
          Edit
        </button>
        <button
          v-if="can('inventory.supplier.delete')"
          class="text-sm text-danger hover:opacity-80 px-2 py-0.5"
          @click="del(row)"
        >
          Delete
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      :title="drawer.isEdit() ? 'Edit Supplier' : 'New Supplier'"
      :subtitle="drawer.isEdit() ? `Editing ${drawer.editing.value?.name}` : 'Add a new supplier'"
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
import type { Supplier } from '~/shared/types/inventory'
import type { FormGroup } from '~/shared/types/form'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import { useFormSchema } from '~/composables/useFormSchema'

definePageMeta({ middleware: 'auth' })

const store = useInventoryStore()
const { can } = usePermission()
const { user } = useAuth()
const { text, email, tel, textarea, switchField, group } = useFormSchema()

const list = useListPage<Supplier>({
  pageSize: 20,
  initialFilters: { status: '' }
})

const drawer = useDrawer<Supplier>()

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  group('Identity', [
    text('name', 'Name', { required: true, placeholder: 'Acme Supplies Co.' }),
    text('contactPerson', 'Contact person', { span: 12 })
  ]),
  group('Contact', [
    tel('phone', 'Phone', { required: true, span: 6 }),
    email('email', 'Email', { span: 6 })
  ]),
  group('Additional', [
    textarea('address', 'Address', { span: 12 }),
    text('taxNumber', 'Tax number', { placeholder: 'TIN-12345', span: 6 }),
    text('paymentTerms', 'Payment terms', { placeholder: 'Net 30', span: 6 }),
    switchField('active', 'Active')
  ])
])

const columns: ColumnDef[] = [
  { key: 'name', title: 'Name', sortable: true },
  { key: 'contactPerson', title: 'Contact person', width: '200px' },
  { key: 'phone', title: 'Phone', width: '160px' },
  { key: 'email', title: 'Email', width: '200px' },
  { key: 'active', title: 'Status', width: '120px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(s =>
      s.name.toLowerCase().includes(q) ||
      s.phone?.toLowerCase().includes(q) ||
      s.email?.toLowerCase().includes(q)
    )
  }
  if (list.filters.value.status === 'active') { items = items.filter(s => s.active) }
  if (list.filters.value.status === 'inactive') { items = items.filter(s => !s.active) }
  return items
})

function onSort (field: string) {
  list.setSort(field)
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  list.error.value = null
  try {
    await store.fetchSuppliers(user.value.tenantId)
    const items = store.suppliers
    list.setItems(items || [], (items || []).length)
  } catch (e: any) {
    list.error.value = e?.data?.message || 'Failed to load suppliers'
  } finally {
    list.loading.value = false
  }
}

function validate (): boolean {
  drawer.error.value = null
  const f = formData.value
  if (!f.name) { drawer.error.value = 'Name is required'; return false }
  if (!f.phone) { drawer.error.value = 'Phone is required'; return false }
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
    const body = { tenantId: user.value!.tenantId, ...data }
    if (drawer.isEdit() && drawer.editing.value) {
      // TODO: wire update endpoint
    } else {
      await store.createSupplier(body as any)
    }
    await load()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.data?.message || 'Failed to save supplier'
  } finally {
    drawer.saving.value = false
  }
}

async function del (s: Supplier) {
  if (!confirm(`Delete supplier "${s.name}"?`)) { return }
  try {
    // TODO: wire delete endpoint
    await load()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to delete supplier')
  }
}

onMounted(load)
</script>

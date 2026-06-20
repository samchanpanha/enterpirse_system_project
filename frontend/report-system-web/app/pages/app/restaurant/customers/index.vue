<template>
  <div>
    <AdminPageHeader title="Customers" :subtitle="`${list.items.value.length} customer on file`">
      <template #actions>
        <button
          v-if="can('customer.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="drawer.openFor(null)"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Customer
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by name, phone, or email…"
      :has-active-filters="list.filters.value.vip"
      @reset="list.resetFilters()"
    >
      <template #filters>
        <select
          v-model="list.filters.value.vip"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All
          </option>
          <option value="true">
            VIP only
          </option>
          <option value="false">
            Regular only
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
      <template #cell-name="{ value, row }">
        <div class="flex items-center gap-2">
          <span class="font-medium text-text-primary">{{ value }}</span>
          <span
            v-if="row.vip"
            class="text-xs px-1.5 py-0.5 rounded bg-warning-light text-warning inline-flex items-center gap-0.5"
          >
            <Icon icon="ant-design:star-filled" class="text-xs" />
            VIP
          </span>
        </div>
      </template>
      <template #cell-phone="{ value }">
        <a v-if="value" :href="`tel:${value}`" class="text-primary hover:text-primary-hover">{{ value }}</a>
        <span v-else class="text-text-disabled">—</span>
      </template>
      <template #cell-totalSpent="{ value }">
        <span class="font-medium text-text-primary">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #cell-lastVisitAt="{ value }">
        <span class="text-text-secondary text-xs">
          {{ value ? new Date(value).toLocaleDateString() : '—' }}
        </span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('customer.update')"
          class="text-sm text-text-secondary hover:text-primary px-2 py-0.5"
          @click="drawer.openFor(row)"
        >
          Edit
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      :title="drawer.isEdit() ? 'Edit Customer' : 'New Customer'"
      :subtitle="drawer.isEdit() ? `Editing ${drawer.editing.value?.name}` : 'Add a new customer'"
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
import type { Customer } from '~/shared/types/restaurant'
import type { FormGroup } from '~/shared/types/form'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import { useFormSchema } from '~/composables/useFormSchema'

definePageMeta({ middleware: 'auth' })

const store = useRestaurantStore()
const { can } = usePermission()
const { user } = useAuth()
const { text, tel, email, switchField, group } = useFormSchema()

const list = useListPage<Customer>({
  pageSize: 20,
  initialFilters: { vip: '' }
})

const drawer = useDrawer<Customer>()

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  group('Identity', [
    text('name', 'Name', { required: true, span: 12 }),
    tel('phone', 'Phone', { required: true, span: 6 }),
    email('email', 'Email', { span: 6 })
  ]),
  group('Preferences', [
    switchField('vip', 'Mark as VIP', { span: 12 })
  ])
])

const columns: ColumnDef[] = [
  { key: 'name', title: 'Name', sortable: true },
  { key: 'phone', title: 'Phone', width: '160px' },
  { key: 'email', title: 'Email', width: '200px' },
  { key: 'totalVisits', title: 'Visits', align: 'right', width: '100px' },
  { key: 'totalSpent', title: 'Total spent', align: 'right', width: '140px' },
  { key: 'lastVisitAt', title: 'Last visit', width: '140px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(c =>
      c.name.toLowerCase().includes(q) ||
      c.phone?.toLowerCase().includes(q) ||
      c.email?.toLowerCase().includes(q)
    )
  }
  if (list.filters.value.vip === 'true') { items = items.filter(c => c.vip) }
  if (list.filters.value.vip === 'false') { items = items.filter(c => !c.vip) }
  return items
})

function onSort (field: string) {
  list.setSort(field)
}

let selectedOutletId = ''

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  list.error.value = null
  try {
    await store.fetchOutlets(user.value.tenantId)
    if (store.outlets.length) {
      selectedOutletId = store.outlets[0].id
      await store.fetchCustomers(selectedOutletId)
      const items = store.customers
      list.setItems(items || [], (items || []).length)
    } else {
      list.setItems([], 0)
    }
  } catch (e: any) {
    list.error.value = e?.data?.message || 'Failed to load customers'
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
    const body = {
      tenantId: user.value!.tenantId,
      outletId: selectedOutletId,
      ...data
    }
    if (drawer.isEdit() && drawer.editing.value) {
      await store.updateCustomer(drawer.editing.value.id, {
        name: data.name, phone: data.phone, email: data.email, vip: data.vip
      })
    } else {
      await store.createCustomer(body as any)
    }
    await load()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.data?.message || 'Failed to save customer'
  } finally {
    drawer.saving.value = false
  }
}

onMounted(load)
</script>

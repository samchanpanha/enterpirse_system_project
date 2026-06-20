<template>
  <div>
    <AdminPageHeader
      title="Purchase Orders"
      :subtitle="`${list.items.value.length} PO on file`"
    >
      <template #actions>
        <button
          v-if="can('inventory.po.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="openCreate()"
        >
          <Icon icon="ant-design:plus-outlined" />
          New PO
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by PO number, supplier, or notes…"
      :has-active-filters="list.filters.value.status"
      @reset="list.resetFilters()"
    >
      <template #filters>
        <select
          v-model="list.filters.value.status"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All statuses
          </option>
          <option value="DRAFT">
            Draft
          </option>
          <option value="ORDERED">
            Ordered
          </option>
          <option value="RECEIVED">
            Received
          </option>
          <option value="CANCELLED">
            Cancelled
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
      <template #cell-poNumber="{ value }">
        <span class="font-mono font-medium text-text-primary">{{ value }}</span>
      </template>
      <template #cell-supplierId="{ value }">
        <span class="text-text-primary">{{ supplierName(value) }}</span>
      </template>
      <template #cell-status="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', poStatusClass(value)]">
          <span :class="['w-1.5 h-1.5 rounded-full', poStatusDot(value)]" />
          {{ value }}
        </span>
      </template>
      <template #cell-total="{ value }">
        <span class="font-medium text-text-primary">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #cell-orderDate="{ value }">
        <span class="text-text-secondary text-xs">{{ new Date(value).toLocaleDateString() }}</span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('inventory.po.update') && (row.status === 'DRAFT' || row.status === 'ORDERED')"
          class="text-sm text-success hover:opacity-80 px-2 py-0.5"
          @click="handleReceive(row.id)"
        >
          Receive
        </button>
        <button
          v-if="can('inventory.po.update') && (row.status === 'DRAFT' || row.status === 'ORDERED')"
          class="text-sm text-danger hover:opacity-80 px-2 py-0.5"
          @click="handleCancel(row.id)"
        >
          Cancel
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      title="New Purchase Order"
      subtitle="Create a new PO from a supplier"
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
          class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50"
          @click="drawer.close()"
        >
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50 inline-flex items-center gap-1.5"
          :disabled="drawer.saving.value"
          @click="submitForm"
        >
          <Icon v-if="drawer.saving.value" icon="ant-design:loading-outlined" class="animate-spin" />
          Create
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { PurchaseOrder } from '~/shared/types/inventory'
import type { FormGroup } from '~/shared/types/form'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import { useFormSchema } from '~/composables/useFormSchema'

definePageMeta({ middleware: 'auth' })

const store = useInventoryStore()
const { can } = usePermission()
const { user } = useAuth()
const { select, date, textarea, group, opt } = useFormSchema()

const list = useListPage<PurchaseOrder>({
  pageSize: 20,
  initialFilters: { status: '' }
})

const drawer = useDrawer<PurchaseOrder>()

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  group('Order details', [
    select('supplierId', 'Supplier', store.suppliers.map(s => opt(s.id, s.name)), { required: true, span: 12 }),
    date('expectedDate', 'Expected date', { span: 6 }),
    date('orderDate', 'Order date', { span: 6 })
  ]),
  group('Additional', [
    textarea('notes', 'Notes', { span: 12 })
  ])
])

const columns: ColumnDef[] = [
  { key: 'poNumber', title: 'PO #', sortable: true, width: '140px' },
  { key: 'supplierId', title: 'Supplier', width: '200px' },
  { key: 'orderDate', title: 'Order date', width: '120px' },
  { key: 'status', title: 'Status', width: '140px' },
  { key: 'total', title: 'Total', align: 'right', width: '140px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(p =>
      p.poNumber?.toLowerCase().includes(q) ||
      supplierName(p.supplierId).toLowerCase().includes(q) ||
      p.notes?.toLowerCase().includes(q)
    )
  }
  if (list.filters.value.status) {
    items = items.filter(p => p.status === list.filters.value.status)
  }
  return items
})

function onSort (field: string) {
  list.setSort(field)
}

function supplierName (id: string): string {
  return store.suppliers.find(s => s.id === id)?.name || id.slice(0, 8)
}

function poStatusClass (status: string): string {
  switch (status) {
    case 'DRAFT': return 'bg-gray-100 text-text-secondary'
    case 'ORDERED': return 'bg-primary-light text-primary'
    case 'RECEIVED': return 'bg-success-light text-success'
    case 'CANCELLED': return 'bg-danger-light text-danger'
    default: return 'bg-warning-light text-warning'
  }
}

function poStatusDot (status: string): string {
  switch (status) {
    case 'DRAFT': return 'bg-text-disabled'
    case 'ORDERED': return 'bg-primary'
    case 'RECEIVED': return 'bg-success'
    case 'CANCELLED': return 'bg-danger'
    default: return 'bg-warning'
  }
}

function openCreate () {
  drawer.openFor(null)
  drawer.formData.value = {
    supplierId: store.suppliers[0]?.id || '',
    orderDate: new Date().toISOString().slice(0, 10),
    expectedDate: '',
    notes: ''
  }
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await Promise.all([
      store.fetchPurchaseOrders(user.value.tenantId),
      store.fetchSuppliers(user.value.tenantId)
    ])
    const pos = store.purchaseOrders
    list.setItems(pos || [], (pos || []).length)
  } catch (e: any) {
    list.error.value = e?.data?.message || 'Failed to load POs'
  } finally {
    list.loading.value = false
  }
}

function submitForm () {
  if (!formData.value.supplierId) {
    drawer.error.value = 'Supplier is required'
    return
  }
  save(formData.value)
}

async function save (data: any) {
  drawer.saving.value = true
  drawer.error.value = null
  try {
    const body = { tenantId: user.value!.tenantId, ...data }
    await store.createPurchaseOrder(body as any)
    await load()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.data?.message || 'Failed to create PO'
  } finally {
    drawer.saving.value = false
  }
}

async function handleReceive (id: string) {
  if (!confirm('Mark this PO as received?')) { return }
  try {
    await store.receivePurchaseOrder(id)
    await load()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to receive PO')
  }
}

async function handleCancel (id: string) {
  if (!confirm('Cancel this PO?')) { return }
  try {
    await store.cancelPurchaseOrder(id)
    await load()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to cancel PO')
  }
}

onMounted(load)
</script>

<template>
  <div>
    <AdminPageHeader title="Inventory" :subtitle="`${list.items.value.length} products on file`">
      <template #actions>
        <NuxtLink
          to="/app/inventory/transfers"
          class="text-sm text-primary hover:text-primary-hover inline-flex items-center gap-1"
        >
          <Icon icon="ant-design:swap-outlined" />
          Transfers
        </NuxtLink>
        <NuxtLink
          to="/app/inventory/purchase-orders"
          class="text-sm text-primary hover:text-primary-hover inline-flex items-center gap-1"
        >
          <Icon icon="ant-design:file-text-outlined" />
          POs
        </NuxtLink>
        <button
          v-if="can('inventory.product.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="productDrawer.openFor(null); resetProductForm()"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Product
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by name or SKU…"
      :has-active-filters="list.filters.value.active"
      @reset="list.resetFilters()"
    >
      <template #filters>
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
        <div>
          <p class="font-medium text-text-primary">
            {{ value }}
          </p>
          <p v-if="row.nameKh" class="text-xs text-text-secondary">
            {{ row.nameKh }}
          </p>
        </div>
      </template>
      <template #cell-unitPrice="{ value }">
        <span class="font-medium text-text-primary">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #cell-costPrice="{ value }">
        <span class="text-text-secondary">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #cell-stock="{ row }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded font-mono', stockClass(row)]">
          {{ stockLevels[row.id] ?? '…' }}
        </span>
      </template>
      <template #cell-active="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', value ? 'bg-success-light text-success' : 'bg-gray-100 text-text-secondary']">
          <span :class="['w-1.5 h-1.5 rounded-full', value ? 'bg-success' : 'bg-text-disabled']" />
          {{ value ? 'Active' : 'Inactive' }}
        </span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('inventory.stock.create')"
          class="text-sm text-primary hover:text-primary-hover px-2 py-0.5"
          @click="openStock(row)"
        >
          Stock
        </button>
        <button
          v-if="can('inventory.product.update')"
          class="text-sm text-text-secondary hover:text-primary px-2 py-0.5"
          @click="editProduct(row)"
        >
          Edit
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="productDrawer.open.value"
      :title="productDrawer.isEdit() ? 'Edit Product' : 'New Product'"
      :subtitle="productDrawer.isEdit() ? productDrawer.editing.value?.name : 'Add a new product to inventory'"
      width="md"
    >
      <AdminForm
        v-if="productDrawer.open.value"
        v-model="formData"
        :groups="formGroups"
        @submit="saveProduct"
      />
      <p
        v-if="productDrawer.error.value"
        class="mt-3 text-sm text-danger flex items-center gap-1 bg-danger-light p-2 rounded"
      >
        <Icon icon="ant-design:exclamation-circle-outlined" />
        {{ productDrawer.error.value }}
      </p>
      <template #footer>
        <button
          class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50"
          @click="productDrawer.close()"
        >
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50 inline-flex items-center gap-1.5"
          :disabled="productDrawer.saving.value"
          @click="submitProduct"
        >
          <Icon v-if="productDrawer.saving.value" icon="ant-design:loading-outlined" class="animate-spin" />
          {{ productDrawer.saving.value ? 'Saving…' : (productDrawer.isEdit() ? 'Update' : 'Create') }}
        </button>
      </template>
    </AdminDrawer>

    <AdminDrawer
      v-model="stockDrawer.open.value"
      title="Stock Adjustment"
      :subtitle="stockDrawer.editing.value?.name"
      width="sm"
    >
      <div class="flex gap-2 mb-4">
        <button
          :class="['flex-1 py-2 text-sm rounded transition-colors', stockMode === 'in' ? 'bg-success text-white' : 'bg-gray-100 text-text-primary']"
          @click="stockMode = 'in'"
        >
          Stock In
        </button>
        <button
          :class="['flex-1 py-2 text-sm rounded transition-colors', stockMode === 'out' ? 'bg-danger text-white' : 'bg-gray-100 text-text-primary']"
          @click="stockMode = 'out'"
        >
          Stock Out
        </button>
      </div>
      <AdminForm
        v-if="stockDrawer.open.value"
        v-model="stockFormData"
        :groups="stockFormGroups"
        @submit="saveStock"
      />
      <p
        v-if="stockDrawer.error.value"
        class="mt-3 text-sm text-danger flex items-center gap-1 bg-danger-light p-2 rounded"
      >
        <Icon icon="ant-design:exclamation-circle-outlined" />
        {{ stockDrawer.error.value }}
      </p>
      <template #footer>
        <button
          class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50"
          @click="stockDrawer.close()"
        >
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50"
          :disabled="stockDrawer.saving.value"
          @click="saveStock(stockFormData)"
        >
          {{ stockMode === 'in' ? 'Add stock' : 'Remove stock' }}
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { Product } from '~/shared/types/inventory'
import type { FormGroup } from '~/shared/types/form'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import { useFormSchema } from '~/composables/useFormSchema'

definePageMeta({ middleware: 'auth' })

const store = useInventoryStore()
const { can } = usePermission()
const { user } = useAuth()
const { text, number, switchField, group } = useFormSchema()

const list = useListPage<Product>({
  pageSize: 20,
  initialFilters: { active: '' }
})

const productDrawer = useDrawer<Product>()
const stockDrawer = useDrawer<Product>()
const stockLevels = ref<Record<string, number>>({})
const stockMode = ref<'in' | 'out'>('in')

const formData = computed({
  get: () => productDrawer.formData.value,
  set: (val) => { productDrawer.formData.value = val }
})
const stockFormData = computed({
  get: () => stockDrawer.formData.value,
  set: (val) => { stockDrawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  group('Identity', [
    text('name', 'Name', { required: true, span: 12 }),
    text('nameKh', 'Khmer name', { placeholder: 'ឈ្មោះ', span: 6 }),
    text('sku', 'SKU', { required: true, span: 6 }),
    text('barcode', 'Barcode', { span: 6 }),
    text('unit', 'Unit', { placeholder: 'pcs, kg, L', span: 6 })
  ]),
  group('Pricing', [
    number('unitPrice', 'Unit price', { required: true, min: 0, step: 0.01, span: 6 }),
    number('costPrice', 'Cost price', { min: 0, step: 0.01, span: 6 })
  ]),
  group('Inventory', [
    number('minStock', 'Min stock', { min: 0, span: 6 }),
    number('maxStock', 'Max stock', { min: 0, span: 6 }),
    switchField('tracked', 'Track stock', { span: 6 }),
    switchField('active', 'Active', { span: 6 })
  ])
])

const stockFormGroups = computed<FormGroup[]>(() => [
  group('Adjustment', [
    number('quantity', 'Quantity', { required: true, min: 1, step: 1, span: 12 }),
    number('unitCost', 'Unit cost', { min: 0, step: 0.01, span: 6 }),
    text('reference', 'Reference', { placeholder: 'PO #, transfer ID, etc.', span: 6 }),
    text('notes', 'Notes', { span: 12 })
  ])
])

const columns: ColumnDef[] = [
  { key: 'name', title: 'Name', sortable: true },
  { key: 'sku', title: 'SKU', width: '120px' },
  { key: 'unitPrice', title: 'Price', align: 'right', width: '120px' },
  { key: 'costPrice', title: 'Cost', align: 'right', width: '120px' },
  { key: 'stock', title: 'Stock', align: 'right', width: '100px' },
  { key: 'active', title: 'Status', width: '120px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(p =>
      p.name.toLowerCase().includes(q) ||
      p.sku?.toLowerCase().includes(q)
    )
  }
  if (list.filters.value.active === 'true') { items = items.filter(p => p.active) }
  if (list.filters.value.active === 'false') { items = items.filter(p => !p.active) }
  return items
})

function onSort (field: string) {
  list.setSort(field)
}

function stockClass (p: Product): string {
  const qty = stockLevels.value[p.id]
  if (qty === undefined) { return 'bg-gray-100 text-text-secondary' }
  if (p.minStock && qty <= p.minStock) { return 'bg-danger-light text-danger' }
  if (p.maxStock && qty >= p.maxStock) { return 'bg-warning-light text-warning' }
  return 'bg-success-light text-success'
}

function resetProductForm () {
  productDrawer.formData.value = {
    name: '',
    nameKh: '',
    sku: '',
    barcode: '',
    unit: 'pcs',
    unitPrice: 0,
    costPrice: 0,
    minStock: 0,
    maxStock: 0,
    tracked: true,
    active: true
  }
}

function editProduct (p: Product) {
  productDrawer.openFor(p)
}

function openStock (p: Product) {
  stockDrawer.openFor(p)
  stockMode.value = 'in'
  stockDrawer.formData.value = { quantity: 1, unitCost: p.costPrice, reference: '', notes: '' }
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchProducts(user.value.tenantId)
    const items = store.products
    list.setItems(items || [], (items || []).length)
    await loadStockLevels()
  } catch (e: any) {
    list.error.value = e?.data?.message || 'Failed to load products'
  } finally {
    list.loading.value = false
  }
}

async function loadStockLevels () {
  for (const p of list.items.value) {
    try {
      const s: any = await store.getStock(user.value!.tenantId, p.id)
      stockLevels.value[p.id] = s?.quantity || 0
    } catch (e) { stockLevels.value[p.id] = 0 }
  }
}

function submitProduct () {
  const f = formData.value
  if (!f.name) { productDrawer.error.value = 'Name is required'; return }
  if (!f.sku) { productDrawer.error.value = 'SKU is required'; return }
  productDrawer.error.value = null
  saveProduct(f)
}

async function saveProduct (data: any) {
  productDrawer.saving.value = true
  try {
    const body = { tenantId: user.value!.tenantId, ...data }
    if (productDrawer.isEdit() && productDrawer.editing.value) {
      await store.updateProduct(productDrawer.editing.value.id, body as any)
    } else {
      await store.createProduct(body as any)
    }
    await load()
    productDrawer.close()
  } catch (e: any) {
    productDrawer.error.value = e?.data?.message || 'Failed to save product'
  } finally {
    productDrawer.saving.value = false
  }
}

async function saveStock (data: any) {
  if (!data.quantity) { stockDrawer.error.value = 'Quantity is required'; return }
  stockDrawer.saving.value = true
  try {
    const product = stockDrawer.editing.value
    const body = {
      tenantId: user.value!.tenantId,
      productId: product!.id,
      quantity: stockMode.value === 'in' ? data.quantity : -data.quantity,
      unitCost: data.unitCost || 0,
      reference: data.reference,
      notes: data.notes
    }
    if (stockMode.value === 'in') {
      await store.createStockEntry(body as any)
    } else {
      await store.createStockExit(body as any)
    }
    stockLevels.value[product!.id] = (stockLevels.value[product!.id] || 0) + (stockMode.value === 'in' ? 1 : -1) * data.quantity
    stockDrawer.close()
  } catch (e: any) {
    stockDrawer.error.value = e?.data?.message || 'Failed to save stock'
  } finally {
    stockDrawer.saving.value = false
  }
}

onMounted(load)
</script>

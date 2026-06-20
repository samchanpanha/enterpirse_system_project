<template>
  <div>
    <AdminPageHeader title="Invoices" :subtitle="`${list.items.value.length} invoices`">
      <template #actions>
        <button
          v-if="can('finance.write')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="openCreate()"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Invoice
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by invoice number or customer…"
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
          <option value="pending">
            Pending
          </option>
          <option value="paid">
            Paid
          </option>
          <option value="partial">
            Partial
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
      @page-change="list.setPage"
      @page-size-change="list.setPageSize"
      @toggle-select="(row) => list.toggleSelect(row.id)"
      @toggle-all="list.isAllSelected() ? list.clearSelection() : list.selectAll()"
    >
      <template #cell-invoiceNumber="{ value }">
        <span class="font-mono text-text-primary">{{ value }}</span>
      </template>
      <template #cell-customerName="{ value }">
        <span class="text-text-primary">{{ value }}</span>
      </template>
      <template #cell-totalAmount="{ value }">
        <span class="font-medium text-text-primary">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #cell-balanceDue="{ value }">
        <span :class="Number(value) > 0 ? 'font-medium text-danger' : 'text-text-secondary'">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #cell-status="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', statusClass(value)]">
          <span :class="['w-1.5 h-1.5 rounded-full', statusDot(value)]" />
          {{ value }}
        </span>
      </template>
      <template #cell-issueDate="{ value }">
        <span class="text-text-secondary text-xs">{{ new Date(value).toLocaleDateString() }}</span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('finance.write') && row.status !== 'paid'"
          class="text-sm text-success hover:opacity-80 px-2 py-0.5"
          @click="openPay(row)"
        >
          Mark paid
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="createDrawer.open.value"
      title="New Invoice"
      width="lg"
    >
      <AdminForm
        v-model="createForm"
        :groups="createFormGroups"
      />
      <div class="mt-4">
        <div class="flex items-center justify-between mb-2">
          <h4 class="text-sm font-semibold text-text-primary">
            Line Items
          </h4>
          <button
            type="button"
            class="text-xs text-primary hover:text-primary-hover"
            @click="addItem"
          >
            + Add item
          </button>
        </div>
        <div
          v-for="(item, idx) in createForm.items"
          :key="idx"
          class="grid grid-cols-12 gap-2 mb-2 items-end"
        >
          <div class="col-span-5">
            <input
              v-model="item.description"
              type="text"
              placeholder="Description"
              class="w-full text-sm border border-border rounded px-2 py-1.5 focus:outline-none focus:border-primary"
            >
          </div>
          <div class="col-span-2">
            <input
              v-model.number="item.quantity"
              type="number"
              min="1"
              placeholder="Qty"
              class="w-full text-sm border border-border rounded px-2 py-1.5 focus:outline-none focus:border-primary"
            >
          </div>
          <div class="col-span-3">
            <input
              v-model.number="item.unitPrice"
              type="number"
              step="0.01"
              placeholder="Price"
              class="w-full text-sm border border-border rounded px-2 py-1.5 focus:outline-none focus:border-primary"
            >
          </div>
          <div class="col-span-2">
            <button
              type="button"
              class="text-xs text-danger hover:opacity-80"
              @click="removeItem(idx)"
            >
              Remove
            </button>
          </div>
        </div>
        <div class="flex justify-end text-sm font-medium text-text-primary mt-2">
          Total: ${{ invoiceTotal.toFixed(2) }}
        </div>
      </div>
      <template #footer>
        <button
          class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50"
          @click="createDrawer.close()"
        >
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover"
          :disabled="!canCreate"
          @click="submitCreate"
        >
          Create
        </button>
      </template>
    </AdminDrawer>

    <AdminDrawer
      v-model="payDrawer.open.value"
      title="Record Payment"
      width="md"
    >
      <AdminForm
        v-model="payForm"
        :groups="payFormGroups"
      />
      <template #footer>
        <button
          class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50"
          @click="payDrawer.close()"
        >
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-success text-white rounded hover:opacity-80"
          :disabled="!payForm.amount || payForm.amount <= 0"
          @click="submitPay"
        >
          Record Payment
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { Invoice, InvoiceItemInput } from '~/shared/types/finance'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import type { FormGroup } from '~/shared/types/form'

definePageMeta({ middleware: 'auth' })

const store = useFinanceStore()
const { can } = usePermission()
const { user } = useAuth()
const { text, number, date, select, group, opt } = useFormSchema()

const list = useListPage<Invoice>({
  pageSize: 20,
  initialFilters: { status: '' }
})
const createDrawer = useDrawer()
const payDrawer = useDrawer<Invoice>()

const createForm = ref({
  invoiceType: 'sales',
  customerName: '',
  issueDate: new Date().toISOString().slice(0, 10),
  dueDate: '',
  items: [] as InvoiceItemInput[]
})
const payForm = ref({ amount: 0 })

const createFormGroups = computed<FormGroup[]>(() => [
  group('', [
    select('invoiceType', 'Type', [
      opt('sales', 'Sales'),
      opt('purchase', 'Purchase')
    ], { required: true }),
    text('customerName', 'Customer / Vendor', { required: true }),
    date('issueDate', 'Issue Date', { required: true }),
    date('dueDate', 'Due Date', { required: true })
  ])
])

const payFormGroups = computed<FormGroup[]>(() => [
  group('', [
    number('amount', 'Amount', { required: true, min: 0.01 })
  ])
])

const columns: ColumnDef[] = [
  { key: 'invoiceNumber', title: 'Invoice #', sortable: true, width: '140px' },
  { key: 'customerName', title: 'Customer', width: '200px' },
  { key: 'issueDate', title: 'Issued', width: '120px' },
  { key: 'dueDate', title: 'Due', width: '120px' },
  { key: 'totalAmount', title: 'Total', align: 'right', width: '120px' },
  { key: 'balanceDue', title: 'Balance', align: 'right', width: '120px' },
  { key: 'status', title: 'Status', width: '120px' }
]

const invoiceTotal = computed(() => {
  return createForm.value.items.reduce((sum, item) => {
    const qty = Number(item.quantity) || 0
    const price = Number(item.unitPrice) || 0
    return sum + (qty * price)
  }, 0)
})

const canCreate = computed(() =>
  createForm.value.customerName &&
  createForm.value.issueDate &&
  createForm.value.dueDate &&
  createForm.value.items.length > 0 &&
  createForm.value.items.every(i => i.description && i.quantity > 0 && i.unitPrice > 0)
)

const filteredItems = computed(() => {
  let items = list.items.value
  if (list.filters.value.status) { items = items.filter(i => i.status === list.filters.value.status) }
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(i =>
      i.invoiceNumber?.toLowerCase().includes(q) ||
      i.customerName?.toLowerCase().includes(q)
    )
  }
  return items
})

function statusClass (status: string): string {
  switch (status) {
    case 'pending': return 'bg-warning-light text-warning'
    case 'partial': return 'bg-primary-light text-primary'
    case 'paid': return 'bg-success-light text-success'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

function statusDot (status: string): string {
  switch (status) {
    case 'pending': return 'bg-warning'
    case 'partial': return 'bg-primary'
    case 'paid': return 'bg-success'
    default: return 'bg-text-disabled'
  }
}

function openCreate () {
  createForm.value = {
    invoiceType: 'sales',
    customerName: '',
    issueDate: new Date().toISOString().slice(0, 10),
    dueDate: '',
    items: [{ description: '', quantity: 1, unitPrice: 0 }]
  }
  createDrawer.openFor(null)
}

function addItem () {
  createForm.value.items.push({ description: '', quantity: 1, unitPrice: 0 })
}

function removeItem (idx: number) {
  createForm.value.items.splice(idx, 1)
}

async function submitCreate () {
  if (!user.value?.tenantId) { return }
  try {
    await store.createInvoice({
      tenantId: user.value.tenantId,
      ...createForm.value
    })
    createDrawer.close()
    await load()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to create invoice')
  }
}

function openPay (row: Invoice) {
  payDrawer.openFor(row)
  payForm.value = { amount: Number(row.balanceDue) || 0 }
}

async function submitPay () {
  const row = payDrawer.editing.value
  if (!row) { return }
  try {
    await store.payInvoice(row.id, payForm.value.amount)
    payDrawer.close()
    await load()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to record payment')
  }
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchInvoices(user.value.tenantId)
    list.setItems(store.invoices || [], (store.invoices || []).length)
  } finally {
    list.loading.value = false
  }
}

onMounted(load)
</script>

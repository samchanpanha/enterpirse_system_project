<template>
  <div>
    <AdminPageHeader
      title="Chart of Accounts"
      :subtitle="`${list.items.value.length} accounts`"
    >
      <template #actions>
        <button
          v-if="can('finance.account.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="openCreate()"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Account
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by code or name…"
      :has-active-filters="list.filters.value.type"
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
          <option value="ASSET">
            Asset
          </option>
          <option value="LIABILITY">
            Liability
          </option>
          <option value="EQUITY">
            Equity
          </option>
          <option value="REVENUE">
            Revenue
          </option>
          <option value="EXPENSE">
            Expense
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
        <span class="font-mono text-text-primary">{{ value }}</span>
      </template>
      <template #cell-type="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', typeClass(value)]">
          {{ value }}
        </span>
      </template>
      <template #cell-balance="{ value }">
        <span class="font-medium text-text-primary">${{ Number(value).toFixed(2) }}</span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('finance.account.update')"
          class="text-sm text-text-secondary hover:text-primary px-2 py-0.5"
          @click="openEdit(row)"
        >
          Edit
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      :title="drawer.isEdit() ? 'Edit Account' : 'New Account'"
      :subtitle="drawer.isEdit() ? `Editing ${drawer.editing.value?.name}` : 'Add a new account'"
      width="sm"
    >
      <AdminForm
        v-if="drawer.open.value"
        v-model="formData"
        :groups="formGroups"
        @submit="submitForm"
      />
      <p
        v-if="drawer.error.value"
        class="mt-3 text-sm text-danger flex items-center gap-1 bg-danger-light p-2 rounded"
      >
        <Icon icon="ant-design:exclamation-circle-outlined" />
        {{ drawer.error.value }}
      </p>
      <template #footer>
        <button class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50" @click="drawer.close()">
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50 inline-flex items-center gap-1.5"
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
import type { Account } from '~/shared/types/finance'
import type { FormGroup } from '~/shared/types/form'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import { useFormSchema } from '~/composables/useFormSchema'

definePageMeta({ middleware: 'auth' })

const store = useFinanceStore()
const { can } = usePermission()
const { user } = useAuth()
const { text, select, group, opt } = useFormSchema()

const list = useListPage<Account>({
  pageSize: 25,
  initialFilters: { type: '' }
})

const drawer = useDrawer<Account>()

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  group('Identity', [
    text('code', 'Code', { required: true, placeholder: '1000', span: 6 }),
    text('name', 'Name', { required: true, placeholder: 'Cash on Hand', span: 6 })
  ]),
  group('Classification', [
    select('type', 'Type', [
      opt('ASSET', 'Asset'),
      opt('LIABILITY', 'Liability'),
      opt('EQUITY', 'Equity'),
      opt('REVENUE', 'Revenue'),
      opt('EXPENSE', 'Expense')
    ], { required: true, span: 6 }),
    select('parentId', 'Parent account', [
      opt('', '(None)'),
      ...(store.accounts.filter(a => !a.parentId).map(a => opt(a.id, `${a.code} – ${a.name}`)) || [])
    ], { span: 6 })
  ])
])

const columns: ColumnDef[] = [
  { key: 'code', title: 'Code', width: '120px' },
  { key: 'name', title: 'Name', sortable: true },
  { key: 'type', title: 'Type', width: '120px' },
  { key: 'balance', title: 'Balance', align: 'right', width: '160px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  if (list.filters.value.type) { items = items.filter(a => a.type === list.filters.value.type) }
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(a =>
      a.code?.toLowerCase().includes(q) ||
      a.name.toLowerCase().includes(q)
    )
  }
  return items
})

function typeClass (t: string): string {
  switch (t) {
    case 'ASSET': return 'bg-primary-light text-primary'
    case 'LIABILITY': return 'bg-danger-light text-danger'
    case 'EQUITY': return 'bg-info-light text-info'
    case 'REVENUE': return 'bg-success-light text-success'
    case 'EXPENSE': return 'bg-warning-light text-warning'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

function openCreate () {
  drawer.openFor(null)
  drawer.formData.value = { code: '', name: '', type: '', parentId: '', active: true }
}

function openEdit (a: Account) {
  drawer.openFor(a)
  drawer.formData.value = { code: a.code, name: a.name, type: a.type, parentId: a.parentId || '', active: a.active }
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchAccounts(user.value.tenantId)
    list.setItems(store.accounts || [], (store.accounts || []).length)
  } finally { list.loading.value = false }
}

async function submitForm () {
  if (!formData.value.code || !formData.value.name || !formData.value.type) {
    drawer.error.value = 'Code, name, and type are required'
    return
  }
  drawer.saving.value = true
  drawer.error.value = null
  try {
    if (drawer.isEdit() && drawer.editing.value) {
      await store.updateAccount(drawer.editing.value.id, formData.value)
    } else {
      await store.createAccount({ tenantId: user.value!.tenantId, ...formData.value })
    }
    await load()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.data?.message || 'Failed to save account'
  } finally { drawer.saving.value = false }
}

onMounted(load)
</script>

<template>
  <div>
    <AdminPageHeader
      title="Clients"
      subtitle="SaaS tenants managed on this instance."
    >
      <template #actions>
        <button
          v-if="can('clients.write')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="drawer.openFor(null)"
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
      :items="filteredItems"
      :columns="columns"
      :loading="list.loading.value"
      :pagination="list.pagination"
      @page-change="list.setPage"
      @page-size-change="list.setPageSize"
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
      <template #cell-subscription="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', tierClass(value)]">
          <Icon :icon="tierIcon(value)" />
          {{ value || 'TRIAL' }}
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
          v-if="can('clients.write')"
          class="text-sm text-primary hover:text-primary-hover px-2 py-0.5"
          @click="tierDrawerOpenFor(row)"
        >
          Change Tier
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      title="New Client"
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

    <AdminDrawer
      v-model="tierDrawer.open.value"
      :title="`Change tier for ${tierDrawer.editing.value?.name}`"
      width="sm"
    >
      <div class="space-y-2">
        <label
          v-for="tier in tiers"
          :key="tier"
          class="flex items-center gap-3 p-3 border rounded hover:bg-gray-50 cursor-pointer"
        >
          <input
            v-model="selectedTier"
            type="radio"
            :value="tier"
            class="w-4 h-4 text-primary focus:ring-primary border-border"
          >
          <span class="text-sm font-medium text-text-primary">{{ tier }}</span>
        </label>
      </div>
      <template #footer>
        <button class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50" @click="tierDrawer.close()">
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50"
          :disabled="tierDrawer.saving.value"
          @click="saveTier"
        >
          {{ tierDrawer.saving.value ? 'Saving…' : 'Save Tier' }}
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import type { FormGroup } from '~/shared/types/form'
import type { Tenant } from '~/shared/types/auth'

definePageMeta({ middleware: 'auth' })

const { can } = usePermission()
const { token } = useAuth()
const config = useRuntimeConfig()
const list = useListPage<Tenant>({ pageSize: 20, initialFilters: { tier: '' } })
const drawer = useDrawer<Tenant>()
const tierDrawer = useDrawer<Tenant>()
const selectedTier = ref('BASIC')
const tiers = ['BASIC', 'PRO', 'ENTERPRISE']

const columns: ColumnDef[] = [
  { key: 'name', title: 'Name', sortable: true },
  { key: 'subscription', title: 'Tier', width: '140px' },
  { key: 'active', title: 'Status', width: '120px' },
  { key: 'createdAt', title: 'Created', width: '180px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  if (list.search.value) {
    const q = list.search.value.toLowerCase()
    items = items.filter(t =>
      t.name.toLowerCase().includes(q) || t.slug.toLowerCase().includes(q)
    )
  }
  if (list.filters.value.tier) {
    items = items.filter(t => (t.subscription || 'TRIAL') === list.filters.value.tier)
  }
  return items
})

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  {
    title: 'Client',
    fields: [
      { key: 'name', label: 'Name', type: 'text', required: true, span: 12 },
      { key: 'slug', label: 'Slug', type: 'text', required: true, span: 6 },
      { key: 'domain', label: 'Domain', type: 'text', span: 6 }
    ]
  }
])

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

async function fetchTenants () {
  list.loading.value = true
  try {
    const res: any = await $fetch(`${config.public.apiBaseUrl}/api/tenants`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    const items = Array.isArray(res) ? res : []
    list.setItems(items, items.length)
  } catch (e: any) {
    list.error.value = e?.data?.message || 'Failed to load clients'
  } finally {
    list.loading.value = false
  }
}

async function submitForm () {
  const f = drawer.formData.value
  if (!f.name || !f.slug) {
    drawer.error.value = 'Name and slug are required'
    return
  }
  drawer.saving.value = true
  drawer.error.value = null
  try {
    await $fetch(`${config.public.apiBaseUrl}/api/tenants`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}`, 'Content-Type': 'application/json' },
      body: {
        name: f.name,
        slug: f.slug,
        domain: f.domain || null
      }
    })
    await fetchTenants()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.data?.message || 'Failed to create client'
  } finally {
    drawer.saving.value = false
  }
}

function tierDrawerOpenFor (tenant: Tenant) {
  tierDrawer.openFor(tenant)
  selectedTier.value = tenant.subscription || 'BASIC'
}

async function saveTier () {
  if (!tierDrawer.editing.value) { return }
  tierDrawer.saving.value = true
  try {
    await $fetch(`${config.public.apiBaseUrl}/api/tenants/${tierDrawer.editing.value.id}/tier`, {
      method: 'PATCH',
      headers: { Authorization: `Bearer ${token.value}`, 'Content-Type': 'application/json' },
      body: { tier: selectedTier.value }
    })
    await fetchTenants()
    tierDrawer.close()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to update tier')
  } finally {
    tierDrawer.saving.value = false
  }
}

onMounted(fetchTenants)
</script>

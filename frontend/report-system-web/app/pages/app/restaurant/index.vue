<template>
  <div>
    <AdminPageHeader title="Restaurant POS" :subtitle="`${list.items.value.length} tables · ${store.outlets.length} outlets`">
      <template #actions>
        <select
          v-if="store.outlets.length"
          v-model="selectedOutletId"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
          @change="onOutletChange"
        >
          <option v-for="o in store.outlets" :key="o.id" :value="o.id">
            {{ o.name }}
          </option>
        </select>
        <NuxtLink
          to="/app/restaurant/orders"
          class="text-sm text-primary hover:text-primary-hover inline-flex items-center gap-1"
        >
          <Icon icon="ant-design:file-text-outlined" />
          Orders
        </NuxtLink>
        <NuxtLink
          to="/app/restaurant/kds"
          class="text-sm text-primary hover:text-primary-hover inline-flex items-center gap-1"
        >
          <Icon icon="ant-design:desktop-outlined" />
          KDS
        </NuxtLink>
        <button
          v-if="can('outlet.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="openOutletForm()"
        >
          <Icon icon="ant-design:plus-outlined" />
          Outlet
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search tables…"
    >
      <template #filters>
        <button
          v-for="s in sections"
          :key="s"
          :class="[
            'px-3 py-1 text-xs rounded transition-colors',
            activeSection === s ? 'bg-primary text-white' : 'bg-white text-text-secondary border border-border hover:bg-gray-50'
          ]"
          @click="activeSection = s"
        >
          {{ s }}
        </button>
      </template>
    </AdminSearchBar>

    <div v-if="!selectedOutletId && store.outlets.length === 0" class="p-6">
      <AdminEmpty
        icon="ant-design:shop-outlined"
        title="No outlets yet"
        description="Create your first outlet to get started with the POS."
      >
        <button
          class="inline-flex items-center gap-1.5 px-4 py-2 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="openOutletForm()"
        >
          <Icon icon="ant-design:plus-outlined" />
          Create Outlet
        </button>
      </AdminEmpty>
    </div>

    <div v-else-if="selectedOutletId" class="p-6">
      <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
        <div
          v-for="t in filteredTables"
          :key="t.id"
          :class="[
            'bg-white rounded-lg shadow-card p-4 text-center cursor-pointer hover:shadow-popover transition-shadow border-2',
            statusBorder(t.status)
          ]"
          @click="openPos(t)"
        >
          <Icon
            :icon="statusIcon(t.status)"
            :class="['text-2xl mb-1', statusTextColor(t.status)]"
          />
          <p class="font-medium text-sm text-text-primary">
            {{ t.label }}
          </p>
          <p class="text-xs text-text-secondary mb-2">
            {{ t.capacity }} seats
          </p>
          <span :class="['inline-block px-2 py-0.5 rounded-full text-xs', statusClass(t.status)]">
            {{ t.status }}
          </span>
        </div>
      </div>
    </div>

    <AdminDrawer
      v-model="posDrawer.open.value"
      :title="posDrawer.editing.value?.label || 'POS'"
      :subtitle="`Table ${posDrawer.editing.value?.label} · ${posDrawer.editing.value?.capacity} seats`"
      width="2xl"
    >
      <div v-if="posDrawer.editing.value" class="grid grid-cols-12 gap-4">
        <div class="col-span-4">
          <h3 class="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-2">
            Menu
          </h3>
          <div class="space-y-1 max-h-96 overflow-y-auto">
            <button
              v-for="cat in store.categories"
              :key="cat.id"
              :class="[
                'w-full text-left px-2 py-1.5 text-sm rounded transition-colors',
                activeCategoryId === cat.id ? 'bg-primary-light text-primary font-medium' : 'text-text-primary hover:bg-gray-50'
              ]"
              @click="activeCategoryId = cat.id"
            >
              {{ cat.name }}
            </button>
          </div>
        </div>
        <div class="col-span-8">
          <h3 class="text-sm font-semibold text-text-secondary uppercase tracking-wide mb-2">
            Items
          </h3>
          <div class="grid grid-cols-2 gap-2 max-h-96 overflow-y-auto">
            <button
              v-for="item in itemsForCategory"
              :key="item.id"
              class="bg-white border border-border rounded p-2 text-left hover:border-primary hover:bg-primary-light transition-colors"
              @click="addToCart(item)"
            >
              <p class="text-sm font-medium text-text-primary">
                {{ item.name }}
              </p>
              <p class="text-xs text-primary font-semibold">
                ${{ item.price.toFixed(2) }}
              </p>
            </button>
          </div>
        </div>
      </div>

      <h3 class="text-sm font-semibold text-text-secondary uppercase tracking-wide mt-6 mb-2">
        Cart
      </h3>
      <div v-if="cart.length === 0" class="text-center text-text-disabled py-6 text-sm">
        Cart is empty
      </div>
      <ul v-else class="space-y-1">
        <li
          v-for="(line, idx) in cart"
          :key="idx"
          class="flex items-center gap-2 px-2 py-1.5 bg-gray-50 rounded"
        >
          <span class="flex-1 text-sm text-text-primary">{{ line.name }}</span>
          <span class="text-xs text-text-secondary">${{ line.price.toFixed(2) }}</span>
          <button
            class="text-danger hover:opacity-80"
            @click="removeFromCart(idx)"
          >
            <Icon icon="ant-design:close-outlined" />
          </button>
        </li>
      </ul>

      <div v-if="cart.length > 0" class="mt-4 pt-3 border-t border-border flex items-center justify-between">
        <span class="text-sm text-text-secondary">Total</span>
        <span class="text-lg font-semibold text-primary">${{ cartTotal.toFixed(2) }}</span>
      </div>

      <template #footer>
        <button
          class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50"
          @click="posDrawer.close()"
        >
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50"
          :disabled="cart.length === 0"
          @click="placeOrder"
        >
          Place Order (${{ cartTotal.toFixed(2) }})
        </button>
      </template>
    </AdminDrawer>

    <AdminDrawer
      v-model="outletDrawer.open.value"
      :title="outletDrawer.isEdit() ? 'Edit Outlet' : 'New Outlet'"
      width="sm"
    >
      <AdminForm
        v-if="outletDrawer.open.value"
        v-model="outletFormData"
        :groups="outletFormGroups"
        @submit="saveOutlet"
      />
      <template #footer>
        <button
          class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50"
          @click="outletDrawer.close()"
        >
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover"
          @click="submitOutlet"
        >
          {{ outletDrawer.isEdit() ? 'Update' : 'Create' }}
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { RestaurantTable, MenuItem } from '~/shared/types/restaurant'
import type { FormGroup } from '~/shared/types/form'
import { useFormSchema } from '~/composables/useFormSchema'

definePageMeta({ middleware: 'auth' })

const store = useRestaurantStore()
const branchStore = useBranchStore()
const { can } = usePermission()
const { user } = useAuth()
const { text, select, group, opt } = useFormSchema()

const list = useListPage<RestaurantTable>({ pageSize: 30 })
const posDrawer = useDrawer<RestaurantTable>()
const outletDrawer = useDrawer()

const selectedOutletId = ref('')
const activeSection = ref<string>('All')
const activeCategoryId = ref<string>('')
const cart = ref<{ id: string; name: string; price: number }[]>([])

const sections = computed(() => {
  const all = ['All', ...new Set(list.items.value.map(t => t.section || 'Main'))]
  return all
})

const filteredTables = computed(() => {
  let items = list.items.value
  if (activeSection.value !== 'All') {
    items = items.filter(t => (t.section || 'Main') === activeSection.value)
  }
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(t => t.label.toLowerCase().includes(q))
  }
  return items
})

const itemsForCategory = computed(() => {
  return store.menuItems.filter(i => i.categoryId === activeCategoryId.value && i.active)
})

const cartTotal = computed(() => cart.value.reduce((s, l) => s + l.price, 0))

const outletFormData = computed({
  get: () => outletDrawer.formData.value,
  set: (val) => { outletDrawer.formData.value = val }
})

const outletFormGroups = computed<FormGroup[]>(() => [
  group('', [
    text('name', 'Name', { required: true, placeholder: 'BKK1 Outlet' }),
    select('type', 'Type', [
      opt('restaurant', 'Restaurant'),
      opt('cafe', 'Cafe'),
      opt('bar', 'Bar')
    ], { required: true }),
    text('address', 'Address', { placeholder: '123 Main St' })
  ])
])

// POS uses cards, not table

function statusBorder (status: string): string {
  switch (status) {
    case 'available': return 'border-success'
    case 'occupied': return 'border-danger'
    case 'reserved': return 'border-warning'
    default: return 'border-border'
  }
}

function statusTextColor (status: string): string {
  switch (status) {
    case 'available': return 'text-success'
    case 'occupied': return 'text-danger'
    case 'reserved': return 'text-warning'
    default: return 'text-text-disabled'
  }
}

function statusClass (status: string): string {
  switch (status) {
    case 'available': return 'bg-success-light text-success'
    case 'occupied': return 'bg-danger-light text-danger'
    case 'reserved': return 'bg-warning-light text-warning'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

function statusIcon (status: string): string {
  switch (status) {
    case 'available': return 'ant-design:check-circle-outlined'
    case 'occupied': return 'ant-design:close-circle-outlined'
    case 'reserved': return 'ant-design:clock-circle-outlined'
    default: return 'ant-design:minus-circle-outlined'
  }
}

function openPos (t: RestaurantTable) {
  posDrawer.openFor(t)
  cart.value = []
  if (!activeCategoryId.value && store.categories.length) {
    activeCategoryId.value = store.categories[0].id
  }
}

function openOutletForm () {
  outletDrawer.openFor(null)
  outletDrawer.formData.value = { name: '', type: 'restaurant', address: '' }
}

function addToCart (item: MenuItem) {
  cart.value.push({ id: item.id, name: item.name, price: item.price })
}

function removeFromCart (idx: number) {
  cart.value.splice(idx, 1)
}

async function placeOrder () {
  if (cart.value.length === 0) { return }
  try {
    await branchStore.$apiWithBranch('/restaurant/orders/with-items', {
      method: 'POST',
      body: {
        tenantId: user.value!.tenantId,
        outletId: selectedOutletId.value,
        tableId: posDrawer.editing.value?.id,
        items: cart.value.map(l => ({ menuItemId: l.id, quantity: 1, unitPrice: l.price }))
      }
    })
    posDrawer.close()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to place order')
  }
}

function submitOutlet () {
  saveOutlet(outletFormData.value)
}

async function saveOutlet (data: any) {
  try {
    await store.createOutlet({
      tenantId: user.value!.tenantId,
      ...data
    } as any)
    if (!selectedOutletId.value && store.outlets.length > 0) {
      selectedOutletId.value = store.outlets[0].id
    }
    outletDrawer.close()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to save outlet')
  }
}

async function loadOutlets () {
  if (!user.value?.tenantId) { return }
  await store.fetchOutlets(user.value.tenantId)
  if (store.outlets.length && !selectedOutletId.value) {
    selectedOutletId.value = store.outlets[0].id
  }
}

async function loadTables () {
  if (!selectedOutletId.value || !user.value?.tenantId) { return }
  list.loading.value = true
  try {
    const items = await store.fetchTables(selectedOutletId.value) as any
    list.setItems(items || [], (items || []).length)
  } finally {
    list.loading.value = false
  }
}

async function loadMenu () {
  if (!selectedOutletId.value || !user.value?.tenantId) { return }
  await Promise.all([
    store.fetchCategories(user.value.tenantId, selectedOutletId.value),
    store.fetchMenuItems(user.value.tenantId)
  ])
}

async function onOutletChange () {
  await Promise.all([loadTables(), loadMenu()])
}

onMounted(async () => {
  await loadOutlets()
  await Promise.all([loadTables(), loadMenu()])
})
</script>

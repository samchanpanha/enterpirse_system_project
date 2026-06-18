<template>
  <div>
    <AdminPageHeader title="Menu Management" :subtitle="`${list.items.value.length} items · ${store.categories.length} categories`">
      <template #actions>
        <select
          v-if="store.outlets.length"
          v-model="selectedOutletId"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option v-for="o in store.outlets" :key="o.id" :value="o.id">
            {{ o.name }}
          </option>
        </select>
        <button
          v-if="can('menu.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 border border-primary text-primary text-sm rounded hover:bg-primary-light transition-colors"
          @click="openCategoryForm()"
        >
          <Icon icon="ant-design:plus-outlined" />
          Category
        </button>
        <button
          v-if="can('menu.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          :disabled="!selectedCategory"
          @click="openItemForm()"
        >
          <Icon icon="ant-design:plus-outlined" />
          Item
        </button>
      </template>
    </AdminPageHeader>

    <div v-if="selectedOutletId" class="grid grid-cols-1 lg:grid-cols-4 gap-0">
      <div class="lg:col-span-1 bg-white border-r border-border p-4">
        <h2 class="text-xs font-semibold text-text-secondary uppercase tracking-wide mb-3 flex items-center gap-2">
          <Icon icon="ant-design:appstore-outlined" />
          Categories
        </h2>
        <ul class="space-y-1">
          <li
            v-for="cat in store.categories"
            :key="cat.id"
          >
            <button
              :class="[
                'w-full text-left px-3 py-2 rounded text-sm transition-colors flex items-center justify-between',
                selectedCategory?.id === cat.id
                  ? 'bg-primary-light text-primary font-medium'
                  : 'text-text-primary hover:bg-gray-50'
              ]"
              @click="selectedCategory = cat"
            >
              <span>{{ cat.name }}</span>
              <span class="text-xs text-text-disabled">{{ itemCount(cat.id) }}</span>
            </button>
          </li>
        </ul>
      </div>

      <div class="lg:col-span-3">
        <AdminSearchBar
          v-model="list.search.value"
          :placeholder="`Search ${selectedCategory?.name || 'items'}…`"
        />
        <AdminTable
          v-if="selectedCategory"
          :items="filteredItems"
          :columns="itemColumns"
          :pagination="list.pagination"
          @page-change="list.setPage"
          @page-size-change="list.setPageSize"
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
          <template #cell-price="{ value }">
            <span class="font-medium text-primary">${{ Number(value).toFixed(2) }}</span>
          </template>
          <template #cell-taxRate="{ value }">
            <span v-if="value > 0" class="text-text-secondary text-xs">
              {{ (value * 100).toFixed(0) }}%
            </span>
            <span v-else class="text-text-disabled text-xs">—</span>
          </template>
          <template #cell-active="{ value }">
            <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', value ? 'bg-success-light text-success' : 'bg-gray-100 text-text-secondary']">
              <span :class="['w-1.5 h-1.5 rounded-full', value ? 'bg-success' : 'bg-text-disabled']" />
              {{ value ? 'Active' : 'Inactive' }}
            </span>
          </template>
          <template #actions="{ row }">
            <button
              v-if="can('menu.update')"
              class="text-sm text-text-secondary hover:text-primary px-2 py-0.5"
              @click="openItemForm(row)"
            >
              Edit
            </button>
            <button
              v-if="can('menu.delete')"
              class="text-sm text-danger hover:opacity-80 px-2 py-0.5"
              @click="delItem(row)"
            >
              Delete
            </button>
          </template>
        </AdminTable>
        <AdminEmpty
          v-else
          icon="ant-design:menu-outlined"
          title="Select a category"
          description="Choose a category on the left to view its menu items."
        />
      </div>
    </div>

    <AdminEmpty
      v-else
      icon="ant-design:shop-outlined"
      title="No outlets"
      description="Create an outlet first to start managing menu items."
    />

    <AdminDrawer
      v-model="categoryDrawer.open.value"
      :title="categoryDrawer.isEdit() ? 'Edit Category' : 'New Category'"
      width="sm"
    >
      <AdminForm
        v-if="categoryDrawer.open.value"
        v-model="categoryForm"
        :groups="categoryFormGroups"
        @submit="saveCategory"
      />
      <p
        v-if="categoryDrawer.error.value"
        class="mt-3 text-sm text-danger flex items-center gap-1 bg-danger-light p-2 rounded"
      >
        <Icon icon="ant-design:exclamation-circle-outlined" />
        {{ categoryDrawer.error.value }}
      </p>
      <template #footer>
        <button class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50" @click="categoryDrawer.close()">
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50"
          :disabled="categoryDrawer.saving.value"
          @click="submitCategory"
        >
          {{ categoryDrawer.isEdit() ? 'Update' : 'Create' }}
        </button>
      </template>
    </AdminDrawer>

    <AdminDrawer
      v-model="itemDrawer.open.value"
      :title="itemDrawer.isEdit() ? 'Edit Item' : 'New Item'"
      :subtitle="itemDrawer.isEdit() ? itemDrawer.editing.value?.name : 'Add a new menu item'"
      width="md"
    >
      <AdminForm
        v-if="itemDrawer.open.value"
        v-model="itemForm"
        :groups="itemFormGroups"
        @submit="saveItem"
      />
      <p
        v-if="itemDrawer.error.value"
        class="mt-3 text-sm text-danger flex items-center gap-1 bg-danger-light p-2 rounded"
      >
        <Icon icon="ant-design:exclamation-circle-outlined" />
        {{ itemDrawer.error.value }}
      </p>
      <template #footer>
        <button class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50" @click="itemDrawer.close()">
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50"
          :disabled="itemDrawer.saving.value"
          @click="submitItem"
        >
          {{ itemDrawer.isEdit() ? 'Update' : 'Create' }}
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { Category, MenuItem } from '~/shared/types/restaurant'
import type { FormGroup } from '~/shared/types/form'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import { useFormSchema } from '~/composables/useFormSchema'

definePageMeta({ middleware: 'auth' })

const store = useRestaurantStore()
const { can } = usePermission()
const { user } = useAuth()
const { text, number, textarea, switchField, group } = useFormSchema()

const list = useListPage<MenuItem>({ pageSize: 24 })

const categoryDrawer = useDrawer<Category>()
const itemDrawer = useDrawer<MenuItem>()

const selectedOutletId = ref('')
const selectedCategory = ref<Category | null>(null)

const itemForm = computed({
  get: () => itemDrawer.formData.value,
  set: (val) => { itemDrawer.formData.value = val }
})
const categoryForm = computed({
  get: () => categoryDrawer.formData.value,
  set: (val) => { categoryDrawer.formData.value = val }
})

const categoryFormGroups = computed<FormGroup[]>(() => [
  group('', [
    text('name', 'Category name', { required: true, placeholder: 'Mains' }),
    text('nameKh', 'Khmer name', { placeholder: 'ម្ហូបចម្បង' }),
    number('displayOrder', 'Display order', { min: 0, placeholder: '0' })
  ])
])

const itemFormGroups = computed<FormGroup[]>(() => [
  group('Item', [
    text('name', 'Name', { required: true, placeholder: 'Pad Krapow' }),
    text('nameKh', 'Khmer name', { placeholder: 'បាយក្រពើ', span: 6 }),
    textarea('description', 'Description', { span: 12 })
  ]),
  group('Pricing', [
    number('price', 'Price', { required: true, min: 0, step: 0.01, span: 6 }),
    number('costPrice', 'Cost price', { min: 0, step: 0.01, span: 6 }),
    number('taxRate', 'Tax rate (0-1)', { min: 0, max: 1, step: 0.01, placeholder: '0.10', span: 6 }),
    number('prepTimeMinutes', 'Prep time (min)', { min: 0, span: 6 })
  ]),
  group('Settings', [
    switchField('active', 'Active', { span: 6 }),
    switchField('available', 'Available', { span: 6 })
  ])
])

const itemColumns: ColumnDef[] = [
  { key: 'name', title: 'Name', sortable: true },
  { key: 'price', title: 'Price', align: 'right', width: '120px' },
  { key: 'taxRate', title: 'Tax', align: 'right', width: '100px' },
  { key: 'prepTimeMinutes', title: 'Prep (min)', align: 'right', width: '120px' },
  { key: 'active', title: 'Status', width: '120px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  if (selectedCategory.value) {
    items = items.filter(i => i.categoryId === selectedCategory.value!.id)
  }
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(i =>
      i.name.toLowerCase().includes(q) ||
      i.nameKh?.toLowerCase().includes(q)
    )
  }
  return items
})

function itemCount (categoryId: string): number {
  return list.items.value.filter(i => i.categoryId === categoryId).length
}

function openCategoryForm (cat?: Category) {
  categoryDrawer.openFor(cat || null)
  if (!cat) {
    categoryDrawer.formData.value = { name: '', nameKh: '', displayOrder: 0 }
  }
}

function openItemForm (item?: MenuItem) {
  if (!selectedCategory.value) { return }
  if (item) {
    itemDrawer.openFor(item)
  } else {
    itemDrawer.openFor(null)
    itemDrawer.formData.value = {
      categoryId: selectedCategory.value.id,
      name: '',
      nameKh: '',
      description: '',
      price: 0,
      costPrice: 0,
      taxRate: 0.1,
      prepTimeMinutes: 10,
      active: true,
      available: true
    }
  }
}

async function loadOutlets () {
  if (!user.value?.tenantId) { return }
  await store.fetchOutlets(user.value.tenantId)
  if (store.outlets.length && !selectedOutletId.value) {
    selectedOutletId.value = store.outlets[0].id
  }
}

async function loadMenu () {
  if (!selectedOutletId.value || !user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await Promise.all([
      store.fetchCategories(user.value.tenantId, selectedOutletId.value),
      store.fetchMenuItems(user.value.tenantId)
    ])
    const cats = store.categories
    const items = store.menuItems
    list.setItems(items || [], (items || []).length)
    if (cats?.length && !selectedCategory.value) {
      selectedCategory.value = cats[0]
    }
  } finally {
    list.loading.value = false
  }
}

function submitCategory () {
  if (!categoryForm.value.name) {
    categoryDrawer.error.value = 'Name is required'
    return
  }
  saveCategory(categoryForm.value)
}

async function saveCategory (data: any) {
  categoryDrawer.saving.value = true
  categoryDrawer.error.value = null
  try {
    const body = { tenantId: user.value!.tenantId, outletId: selectedOutletId.value, ...data }
    if (categoryDrawer.isEdit() && categoryDrawer.editing.value) {
      // TODO: update
    } else {
      await store.createCategory(body as any)
    }
    await loadMenu()
    categoryDrawer.close()
  } catch (e: any) {
    categoryDrawer.error.value = e?.data?.message || 'Failed to save category'
  } finally {
    categoryDrawer.saving.value = false
  }
}

function submitItem () {
  if (!itemForm.value.name) {
    itemDrawer.error.value = 'Name is required'
    return
  }
  if (!itemForm.value.price) {
    itemDrawer.error.value = 'Price is required'
    return
  }
  saveItem(itemForm.value)
}

async function saveItem (data: any) {
  itemDrawer.saving.value = true
  itemDrawer.error.value = null
  try {
    const body = { tenantId: user.value!.tenantId, outletId: selectedOutletId.value, ...data }
    if (itemDrawer.isEdit() && itemDrawer.editing.value) {
      // TODO: update
    } else {
      await store.createMenuItem(body as any)
    }
    await loadMenu()
    itemDrawer.close()
  } catch (e: any) {
    itemDrawer.error.value = e?.data?.message || 'Failed to save item'
  } finally {
    itemDrawer.saving.value = false
  }
}

async function delItem (i: MenuItem) {
  if (!confirm(`Delete "${i.name}"?`)) { return }
  try {
    // TODO: wire delete endpoint
    await loadMenu()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to delete item')
  }
}

watch(selectedOutletId, async () => {
  selectedCategory.value = null
  await loadMenu()
})

onMounted(async () => {
  await loadOutlets()
  await loadMenu()
})
</script>

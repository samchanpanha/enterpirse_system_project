<template>
  <div>
    <AdminPageHeader title="Reservations" :subtitle="`${list.items.value.length} reservations`">
      <template #actions>
        <input
          v-model="dateFilter"
          type="date"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
        <button
          v-if="can('reservation.create')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="drawer.openFor(null); resetForm()"
        >
          <Icon icon="ant-design:plus-outlined" />
          New
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by customer or phone…"
    />

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
      <template #cell-customerName="{ value }">
        <span class="font-medium text-text-primary">{{ value }}</span>
      </template>
      <template #cell-reservationDate="{ value }">
        <span class="text-text-primary">{{ new Date(value).toLocaleDateString() }}</span>
      </template>
      <template #cell-reservationTime="{ value }">
        <span class="text-text-secondary font-mono">{{ value }}</span>
      </template>
      <template #cell-partySize="{ value }">
        <span class="text-text-primary">{{ value }} guests</span>
      </template>
      <template #cell-status="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', statusClass(value)]">
          <span :class="['w-1.5 h-1.5 rounded-full', statusDot(value)]" />
          {{ value }}
        </span>
      </template>
      <template #actions="{ row }">
        <button
          v-if="row.status === 'CONFIRMED' || row.status === 'PENDING'"
          class="text-sm text-success hover:opacity-80 px-2 py-0.5"
          @click="updateStatus(row, 'SEATED')"
        >
          Seat
        </button>
        <button
          v-if="['SEATED', 'CONFIRMED', 'PENDING'].includes(row.status)"
          class="text-sm text-danger hover:opacity-80 px-2 py-0.5"
          @click="updateStatus(row, 'CANCELLED')"
        >
          Cancel
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      title="New Reservation"
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
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50"
          :disabled="drawer.saving.value"
          @click="submitForm"
        >
          Reserve
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { Reservation } from '~/shared/types/restaurant'
import type { FormGroup } from '~/shared/types/form'
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import { useFormSchema } from '~/composables/useFormSchema'

definePageMeta({ middleware: 'auth' })

const store = useRestaurantStore()
const { can } = usePermission()
const { user } = useAuth()
const { text, tel, number, datetime, textarea, group } = useFormSchema()

const list = useListPage<Reservation>({ pageSize: 20 })
const drawer = useDrawer<Reservation>()
const dateFilter = ref(new Date().toISOString().slice(0, 10))

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  group('Guest', [
    text('customerName', 'Customer name', { required: true, span: 12 }),
    tel('customerPhone', 'Phone', { required: true, span: 6 }),
    number('partySize', 'Party size', { required: true, min: 1, span: 6 })
  ]),
  group('Time', [
    datetime('reservationAt', 'Date & time', { required: true, span: 12 })
  ]),
  group('Additional', [
    textarea('notes', 'Notes / special requests', { span: 12 })
  ])
])

const columns: ColumnDef[] = [
  { key: 'customerName', title: 'Customer', sortable: true },
  { key: 'customerPhone', title: 'Phone', width: '160px' },
  { key: 'reservationDate', title: 'Date', width: '120px' },
  { key: 'reservationTime', title: 'Time', width: '100px' },
  { key: 'partySize', title: 'Party', align: 'right', width: '100px' },
  { key: 'status', title: 'Status', width: '140px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  const q = list.search.value.toLowerCase()
  if (q) {
    items = items.filter(r =>
      (r.customerName || '').toLowerCase().includes(q) ||
      (r.customerPhone || '').toLowerCase().includes(q)
    )
  }
  return items
})

function statusClass (status: string): string {
  switch (status) {
    case 'PENDING': return 'bg-warning-light text-warning'
    case 'CONFIRMED': return 'bg-primary-light text-primary'
    case 'SEATED': return 'bg-success-light text-success'
    case 'COMPLETED': return 'bg-gray-100 text-text-secondary'
    case 'CANCELLED': return 'bg-danger-light text-danger'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

function statusDot (status: string): string {
  switch (status) {
    case 'PENDING': return 'bg-warning'
    case 'CONFIRMED': return 'bg-primary'
    case 'SEATED': return 'bg-success'
    case 'COMPLETED': return 'bg-text-disabled'
    case 'CANCELLED': return 'bg-danger'
    default: return 'bg-text-disabled'
  }
}

function resetForm () {
  drawer.formData.value = {
    customerName: '',
    customerPhone: '',
    partySize: 2,
    reservationAt: new Date().toISOString().slice(0, 16),
    notes: ''
  }
}

function submitForm () {
  const f = formData.value
  if (!f.customerName) { drawer.error.value = 'Name is required'; return }
  if (!f.customerPhone) { drawer.error.value = 'Phone is required'; return }
  drawer.error.value = null
  save(f)
}

async function save (data: any) {
  drawer.saving.value = true
  try {
    const [date, time] = data.reservationAt.split('T')
    await store.createReservation({
      tenantId: user.value!.tenantId,
      customerName: data.customerName,
      customerPhone: data.customerPhone,
      partySize: data.partySize,
      reservationDate: date,
      reservationTime: time,
      notes: data.notes
    } as any)
    await load()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.data?.message || 'Failed to create reservation'
  } finally {
    drawer.saving.value = false
  }
}

async function updateStatus (r: Reservation, status: string) {
  try {
    const config = useRuntimeConfig()
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    if (user.value?.tenantId) { headers['X-Tenant-Id'] = user.value.tenantId }
    await $fetch(`${config.public.apiBaseUrl}/restaurant/reservations/${r.id}/status`, {
      method: 'PUT', headers, body: { status }
    })
    await load()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to update')
  }
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await store.fetchReservations(user.value.tenantId, dateFilter.value)
    list.setItems(store.reservations || [], (store.reservations || []).length)
  } finally {
    list.loading.value = false
  }
}

watch(dateFilter, () => load())

onMounted(load)
</script>

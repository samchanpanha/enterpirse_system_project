<template>
  <div>
    <AdminPageHeader
      title="Kitchen Display"
      subtitle="Live order feed for the kitchen. Auto-refreshes every 15s."
    >
      <template #actions>
        <button
          class="inline-flex items-center gap-1.5 px-3 py-1.5 border border-border text-text-primary text-sm rounded hover:bg-gray-50 transition-colors"
          :disabled="list.loading.value"
          @click="load"
        >
          <Icon icon="ant-design:reload-outlined" :class="list.loading.value ? 'animate-spin' : ''" />
          Refresh
        </button>
      </template>
    </AdminPageHeader>

    <div class="p-6">
      <div v-if="pendingOrders.length === 0" class="text-center py-12">
        <Icon icon="ant-design:check-circle-outlined" class="text-5xl text-success" />
        <h2 class="text-lg font-semibold text-text-primary mt-3">
          All caught up!
        </h2>
        <p class="text-sm text-text-secondary mt-1">
          No pending orders right now.
        </p>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        <div
          v-for="o in pendingOrders"
          :key="o.id"
          class="bg-white rounded-lg shadow-card p-4 border-l-4"
          :class="statusBorder(o.status)"
        >
          <div class="flex items-center justify-between mb-2">
            <span class="text-xs font-mono text-text-secondary">#{{ o.id.slice(0, 8) }}</span>
            <span :class="['text-xs px-2 py-0.5 rounded', statusClass(o.status)]">{{ o.status }}</span>
          </div>
          <h3 class="text-sm font-semibold text-text-primary mb-1">
            Table {{ o.tableId?.slice(0, 8) || 'Takeout' }}
          </h3>
          <p class="text-xs text-text-secondary mb-3">
            {{ relativeTime(o.createdAt) }}
          </p>
          <ul class="space-y-1 mb-3">
            <li
              v-for="(item, idx) in o.items || []"
              :key="idx"
              class="text-sm text-text-primary"
            >
              {{ item.quantity }}× {{ item.name || item.menuItemId?.slice(0, 8) }}
            </li>
          </ul>
          <div class="flex gap-2">
            <button
              v-if="o.status === 'PENDING'"
              class="flex-1 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover transition-colors"
              @click="markPreparing(o)"
            >
              Start
            </button>
            <button
              v-if="o.status === 'PREPARING'"
              class="flex-1 py-1.5 text-sm bg-success text-white rounded hover:opacity-90 transition-colors"
              @click="markReady(o)"
            >
              Ready
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Order } from '~/shared/types/restaurant'

definePageMeta({ middleware: 'auth' })

const store = useRestaurantStore()
const { user } = useAuth()

const list = useListPage<Order>({ pageSize: 100 })

const pendingOrders = computed(() => {
  return list.items.value.filter(o => o.status === 'PENDING' || o.status === 'PREPARING')
})

function statusBorder (status: string): string {
  switch (status) {
    case 'PENDING': return 'border-l-warning'
    case 'PREPARING': return 'border-l-primary'
    default: return 'border-l-border'
  }
}

function statusClass (status: string): string {
  switch (status) {
    case 'PENDING': return 'bg-warning-light text-warning'
    case 'PREPARING': return 'bg-primary-light text-primary'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

function relativeTime (iso: string): string {
  const d = new Date(iso)
  const minutes = Math.floor((Date.now() - d.getTime()) / 60000)
  if (minutes < 1) { return 'Just now' }
  if (minutes < 60) { return `${minutes}m ago` }
  const hours = Math.floor(minutes / 60)
  return `${hours}h ${minutes % 60}m ago`
}

async function markPreparing (o: Order) {
  try {
    const config = useRuntimeConfig()
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    if (user.value?.tenantId) { headers['X-Tenant-Id'] = user.value.tenantId }
    await $fetch(`${config.public.apiBaseUrl}/restaurant/orders/${o.id}/status`, {
      method: 'PUT', headers, body: { status: 'PREPARING' }
    })
    await load()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to update')
  }
}

async function markReady (o: Order) {
  try {
    const config = useRuntimeConfig()
    const headers: Record<string, string> = { 'Content-Type': 'application/json' }
    if (user.value?.tenantId) { headers['X-Tenant-Id'] = user.value.tenantId }
    await $fetch(`${config.public.apiBaseUrl}/restaurant/orders/${o.id}/status`, {
      method: 'PUT', headers, body: { status: 'READY' }
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
    await store.fetchOrders(user.value.tenantId)
    list.setItems(store.orders || [], (store.orders || []).length)
  } finally {
    list.loading.value = false
  }
}

let interval: any = null

onMounted(() => {
  load()
  if (import.meta.client) {
    interval = setInterval(load, 15000)
  }
})

onUnmounted(() => {
  if (interval) { clearInterval(interval) }
})
</script>

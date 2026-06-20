<template>
  <div class="p-6">
    <div class="bg-white rounded shadow-card p-6 mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-text-primary m-0">
            Welcome back, {{ user?.firstName || 'there' }}
          </h1>
          <p class="text-text-secondary mt-1 mb-0">
            {{ today }}
          </p>
          <p v-if="loadError" class="text-xs text-warning mt-1 mb-0">
            {{ loadError }}
          </p>
        </div>
        <Icon
          icon="ant-design:line-chart-outlined"
          class="text-4xl text-primary opacity-80"
        />
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div
        v-for="card in summaryCards"
        :key="card.key"
        class="bg-white rounded shadow-card p-5 hover:shadow-popover transition-shadow"
      >
        <div v-if="loading" class="animate-pulse">
          <div class="h-4 bg-gray-200 rounded w-24 mb-3" />
          <div class="h-8 bg-gray-200 rounded w-16 mb-2" />
          <div class="h-3 bg-gray-100 rounded w-32" />
        </div>
        <template v-else>
          <div class="flex items-start justify-between mb-2">
            <span class="text-text-secondary text-sm">
              {{ card.label }}
            </span>
            <div
              :class="['w-8 h-8 rounded flex items-center justify-center', card.bg]"
            >
              <Icon :icon="card.icon" class="text-white text-base" />
            </div>
          </div>
          <div class="text-2xl font-semibold text-text-primary">
            {{ card.value }}
          </div>
          <div class="text-xs text-text-disabled mt-1">
            {{ card.hint }}
          </div>
        </template>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="bg-white rounded shadow-card p-6">
        <h2 class="text-base font-medium text-text-primary mb-4 flex items-center gap-2">
          <Icon icon="ant-design:clock-circle-outlined" class="text-primary" />
          Recent Activity
        </h2>
        <div v-if="loading" class="space-y-3 animate-pulse">
          <div v-for="i in 4" :key="i" class="h-12 bg-gray-100 rounded" />
        </div>
        <div v-else-if="recentActivity.length === 0" class="text-center text-text-secondary py-10">
          <Icon icon="ant-design:inbox-outlined" class="text-3xl text-text-disabled" />
          <p class="mt-2 text-sm">
            No recent activity yet.
          </p>
        </div>
        <ul v-else class="space-y-3">
          <li
            v-for="(a, idx) in recentActivity.slice(0, 8)"
            :key="idx"
            class="flex items-start gap-3 text-sm border-b border-border pb-2 last:border-0"
          >
            <div :class="['w-8 h-8 rounded-full flex items-center justify-center shrink-0', a.bg]">
              <Icon :icon="a.icon" class="text-white text-sm" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-text-primary font-medium truncate">
                {{ a.title }}
              </p>
              <p class="text-text-secondary text-xs">
                {{ a.subtitle }}
              </p>
            </div>
            <span class="text-text-disabled text-xs whitespace-nowrap">{{ a.time }}</span>
          </li>
        </ul>
      </div>

      <div class="bg-white rounded shadow-card p-6">
        <h2 class="text-base font-medium text-text-primary mb-4 flex items-center gap-2">
          <Icon icon="ant-design:rocket-outlined" class="text-primary" />
          Quick Links
        </h2>
        <div class="grid grid-cols-2 gap-3">
          <NuxtLink
            v-for="link in quickLinks"
            :key="link.path"
            :to="link.path"
            class="border border-border rounded p-3 hover:border-primary hover:text-primary transition-colors text-text-secondary text-sm flex items-center gap-2"
          >
            <Icon :icon="link.icon" class="text-base" />
            {{ link.label }}
          </NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Invoice } from '~/shared/types/finance'
import type { Property, Lease } from '~/shared/types/property'
import type { Order } from '~/shared/types/restaurant'

definePageMeta({
  middleware: 'auth'
})

const { user } = useAuth()
const branchStore = useBranchStore()

const today = computed(() => {
  return new Date().toLocaleDateString(undefined, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
})

const propertyCount = ref(0)
const leaseCount = ref(0)
const todaySales = ref(0)
const outstandingRent = ref(0)
const recentInvoices = ref<Invoice[]>([])
const recentOrders = ref<Order[]>([])
const loading = ref(true)
const loadError = ref('')

const summaryCards = computed(() => [
  {
    key: 'properties',
    label: 'Properties',
    value: propertyCount.value,
    hint: 'Total managed properties',
    icon: 'ant-design:home-outlined',
    bg: 'bg-primary'
  },
  {
    key: 'leases',
    label: 'Active Leases',
    value: leaseCount.value,
    hint: 'Currently rented',
    icon: 'ant-design:file-text-outlined',
    bg: 'bg-success'
  },
  {
    key: 'sales',
    label: "Today's Sales",
    value: `$${todaySales.value.toFixed(2)}`,
    hint: 'Across all outlets',
    icon: 'ant-design:shopping-cart-outlined',
    bg: 'bg-warning'
  },
  {
    key: 'rent',
    label: 'Outstanding Rent',
    value: `$${outstandingRent.value.toFixed(2)}`,
    hint: 'Awaiting payment',
    icon: 'ant-design:dollar-outlined',
    bg: 'bg-danger'
  }
])

const recentActivity = computed(() => {
  const items: { title: string; subtitle: string; time: string; icon: string; bg: string }[] = []

  recentInvoices.value.slice(0, 5).forEach((i) => {
    items.push({
      title: `Invoice ${i.invoiceNumber}`,
      subtitle: `${i.customerName} — $${Number(i.total).toFixed(2)}`,
      time: new Date(i.issueDate).toLocaleDateString(),
      icon: 'ant-design:file-text-outlined',
      bg: 'bg-primary'
    })
  })

  recentOrders.value.slice(0, 5).forEach((o) => {
    items.push({
      title: `Order ${o.orderNumber}`,
      subtitle: `Table ${o.tableId?.slice(0, 8) || 'N/A'} — $${Number(o.total).toFixed(2)}`,
      time: o.createdAt ? new Date(o.createdAt).toLocaleString() : '',
      icon: 'ant-design:shopping-cart-outlined',
      bg: 'bg-warning'
    })
  })

  return items.sort((a, b) => new Date(b.time).getTime() - new Date(a.time).getTime())
})

const quickLinks = [
  { path: '/app/property', label: 'Properties', icon: 'ant-design:home-outlined' },
  { path: '/app/restaurant', label: 'POS', icon: 'ant-design:shop-outlined' },
  { path: '/app/inventory', label: 'Inventory', icon: 'ant-design:database-outlined' },
  { path: '/app/finance', label: 'Finance', icon: 'ant-design:account-book-outlined' },
  { path: '/app/payment', label: 'Payments', icon: 'ant-design:credit-card-outlined' },
  { path: '/app/reports', label: 'Reports', icon: 'ant-design:bar-chart-outlined' }
]

async function load () {
  if (!user.value?.tenantId) { return }
  loading.value = true
  const tenantId = user.value.tenantId
  const todayStart = new Date()
  todayStart.setHours(0, 0, 0, 0)

  try {
    const [properties, leases, orders, invoices] = await Promise.all([
      branchStore.$apiWithBranch<Property[]>(`/property/properties/by-tenant/${tenantId}`).catch(() => []),
      branchStore.$apiWithBranch<Lease[]>(`/property/leases/active/by-tenant/${tenantId}`).catch(() => []),
      branchStore.$apiWithBranch<Order[]>(`/restaurant/orders/by-tenant/${tenantId}`).catch(() => []),
      branchStore.$apiWithBranch<Invoice[]>(`/finance/invoices/by-tenant/${tenantId}`).catch(() => [])
    ])

    propertyCount.value = properties.length
    leaseCount.value = leases.length

    todaySales.value = orders
      .filter(o => o.createdAt && new Date(o.createdAt) >= todayStart)
      .reduce((sum, o) => sum + (Number(o.total) || 0), 0)

    outstandingRent.value = invoices
      .filter(i => i.status !== 'paid')
      .reduce((sum, i) => sum + (Number(i.balanceDue) || 0), 0)

    recentInvoices.value = invoices
      .sort((a, b) => new Date(b.issueDate).getTime() - new Date(a.issueDate).getTime())

    recentOrders.value = orders
      .sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
  } catch {
    loadError.value = 'Some dashboard data could not be loaded.'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

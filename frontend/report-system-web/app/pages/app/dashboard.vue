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
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="bg-white rounded shadow-card p-6">
        <h2 class="text-base font-medium text-text-primary mb-4 flex items-center gap-2">
          <Icon icon="ant-design:clock-circle-outlined" class="text-primary" />
          Recent Activity
        </h2>
        <div class="text-center text-text-secondary py-10">
          <Icon icon="ant-design:inbox-outlined" class="text-3xl text-text-disabled" />
          <p class="mt-2 text-sm">
            No recent activity yet. Connect services to see live data.
          </p>
        </div>
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
definePageMeta({
  middleware: 'auth'
})

const { user } = useAuth()

const today = computed(() => {
  return new Date().toLocaleDateString(undefined, {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
})

const summaryCards = [
  {
    key: 'properties',
    label: 'Properties',
    value: '—',
    hint: 'Total managed units',
    icon: 'ant-design:home-outlined',
    bg: 'bg-primary'
  },
  {
    key: 'leases',
    label: 'Active Leases',
    value: '—',
    hint: 'Currently rented',
    icon: 'ant-design:file-text-outlined',
    bg: 'bg-success'
  },
  {
    key: 'sales',
    label: "Today's Sales",
    value: '—',
    hint: 'Across all outlets',
    icon: 'ant-design:shopping-cart-outlined',
    bg: 'bg-warning'
  },
  {
    key: 'rent',
    label: 'Outstanding Rent',
    value: '—',
    hint: 'Awaiting payment',
    icon: 'ant-design:dollar-outlined',
    bg: 'bg-danger'
  }
]

const quickLinks = [
  { path: '/app/property', label: 'Properties', icon: 'ant-design:home-outlined' },
  { path: '/app/restaurant', label: 'POS', icon: 'ant-design:shop-outlined' },
  { path: '/app/inventory', label: 'Inventory', icon: 'ant-design:database-outlined' },
  { path: '/app/finance', label: 'Finance', icon: 'ant-design:account-book-outlined' },
  { path: '/app/payment', label: 'Payments', icon: 'ant-design:credit-card-outlined' },
  { path: '/app/reports', label: 'Reports', icon: 'ant-design:bar-chart-outlined' }
]
</script>

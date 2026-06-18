<template>
  <div class="p-6">
    <div class="bg-white rounded shadow-card p-8 mb-6">
      <div class="flex flex-col md:flex-row items-center gap-6">
        <div class="flex-1">
          <div class="inline-flex items-center gap-2 px-3 py-1 bg-primary-light text-primary rounded-full text-xs font-medium mb-3">
            <Icon icon="ant-design:thunderbolt-outlined" />
            Multi-tenant SaaS for Cambodia
          </div>
          <h1 class="text-3xl font-semibold text-text-primary mb-3">
            Welcome to Report System
          </h1>
          <p class="text-text-secondary mb-5 max-w-2xl">
            Property management, restaurant POS, accounting, inventory, and Cambodia tax compliance —
            all from a single admin console. Built for multi-branch operations with Keycloak SSO.
          </p>
          <div class="flex flex-wrap gap-3">
            <NuxtLink
              v-if="!user"
              to="/login"
              class="inline-flex items-center gap-2 px-5 py-2.5 bg-primary text-white rounded hover:bg-primary-hover transition-colors"
            >
              <Icon icon="ant-design:login-outlined" />
              Sign in
            </NuxtLink>
            <NuxtLink
              v-if="!user"
              to="/register"
              class="inline-flex items-center gap-2 px-5 py-2.5 border border-primary text-primary rounded hover:bg-primary-light transition-colors"
            >
              <Icon icon="ant-design:user-add-outlined" />
              Register
            </NuxtLink>
            <NuxtLink
              v-if="user"
              to="/app/dashboard"
              class="inline-flex items-center gap-2 px-5 py-2.5 bg-primary text-white rounded hover:bg-primary-hover transition-colors"
            >
              <Icon icon="ant-design:appstore-outlined" />
              Go to dashboard
            </NuxtLink>
          </div>
        </div>
        <div class="hidden md:flex w-40 h-40 rounded-full bg-primary-light items-center justify-center flex-shrink-0">
          <Icon icon="ant-design:line-chart-outlined" class="text-7xl text-primary" />
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="module in modules"
        :key="module.key"
        class="bg-white rounded shadow-card p-5 hover:shadow-popover transition-shadow"
      >
        <div class="flex items-center gap-3 mb-3">
          <div :class="['w-10 h-10 rounded flex items-center justify-center', module.bg]">
            <Icon :icon="module.icon" class="text-white text-xl" />
          </div>
          <h3 class="text-base font-medium text-text-primary m-0">
            {{ module.name }}
          </h3>
        </div>
        <p class="text-sm text-text-secondary mb-3">
          {{ module.description }}
        </p>
        <NuxtLink
          v-if="user"
          :to="module.path"
          class="text-sm text-primary hover:text-primary-hover inline-flex items-center gap-1"
        >
          Open
          <Icon icon="ant-design:arrow-right-outlined" />
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const { user } = useAuth()

const modules = [
  {
    key: 'property',
    name: 'Property Management',
    description: 'Properties, units, leases, rent collection, and maintenance tracking.',
    icon: 'ant-design:home-outlined',
    bg: 'bg-primary',
    path: '/app/property'
  },
  {
    key: 'restaurant',
    name: 'Restaurant POS',
    description: 'Floor plan, POS, kitchen display, menu, reservations, and customers.',
    icon: 'ant-design:shop-outlined',
    bg: 'bg-warning',
    path: '/app/restaurant'
  },
  {
    key: 'inventory',
    name: 'Inventory',
    description: 'Products, suppliers, purchase orders, multi-warehouse stock and transfers.',
    icon: 'ant-design:database-outlined',
    bg: 'bg-info',
    path: '/app/inventory'
  },
  {
    key: 'finance',
    name: 'Finance & Accounting',
    description: 'Chart of accounts, invoices, payroll, NSSF/TOS computation, and 14 Cambodia tax types.',
    icon: 'ant-design:account-book-outlined',
    bg: 'bg-success',
    path: '/app/finance'
  },
  {
    key: 'payment',
    name: 'Payments',
    description: 'ABA PayWay, Wing, Pi Pay, and cash. Refunds and reconciliation.',
    icon: 'ant-design:credit-card-outlined',
    bg: 'bg-danger',
    path: '/app/payment'
  },
  {
    key: 'reports',
    name: 'Reports & Dashboards',
    description: 'Custom report definitions, scheduled executions, and dashboard configs.',
    icon: 'ant-design:bar-chart-outlined',
    bg: 'bg-primary',
    path: '/app/reports'
  }
]
</script>

<template>
  <div>
    <AdminPageHeader title="Finance" subtitle="Overview of your financial health">
      <template #actions>
        <NuxtLink
          v-for="link in sublinks"
          :key="link.path"
          :to="link.path"
          class="text-sm text-primary hover:text-primary-hover inline-flex items-center gap-1"
        >
          <Icon :icon="link.icon" />
          {{ link.title }}
        </NuxtLink>
      </template>
    </AdminPageHeader>

    <div class="p-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white rounded shadow-card p-5">
        <div class="flex items-start justify-between mb-2">
          <span class="text-sm text-text-secondary">
            Total receivables
          </span>
          <div class="w-8 h-8 rounded bg-primary flex items-center justify-center">
            <Icon icon="ant-design:arrow-down-outlined" class="text-white" />
          </div>
        </div>
        <div class="text-2xl font-semibold text-text-primary">
          ${{ totalReceivables.toFixed(2) }}
        </div>
      </div>
      <div class="bg-white rounded shadow-card p-5">
        <div class="flex items-start justify-between mb-2">
          <span class="text-sm text-text-secondary">
            Total payables
          </span>
          <div class="w-8 h-8 rounded bg-warning flex items-center justify-center">
            <Icon icon="ant-design:arrow-up-outlined" class="text-white" />
          </div>
        </div>
        <div class="text-2xl font-semibold text-text-primary">
          ${{ totalPayables.toFixed(2) }}
        </div>
      </div>
      <div class="bg-white rounded shadow-card p-5">
        <div class="flex items-start justify-between mb-2">
          <span class="text-sm text-text-secondary">
            Overdue invoices
          </span>
          <div class="w-8 h-8 rounded bg-danger flex items-center justify-center">
            <Icon icon="ant-design:warning-outlined" class="text-white" />
          </div>
        </div>
        <div class="text-2xl font-semibold text-danger">
          {{ overdueCount }}
        </div>
      </div>
      <div class="bg-white rounded shadow-card p-5">
        <div class="flex items-start justify-between mb-2">
          <span class="text-sm text-text-secondary">
            Employees
          </span>
          <div class="w-8 h-8 rounded bg-success flex items-center justify-center">
            <Icon icon="ant-design:team-outlined" class="text-white" />
          </div>
        </div>
        <div class="text-2xl font-semibold text-text-primary">
          {{ employeeCount }}
        </div>
      </div>
    </div>

    <div class="px-6 grid grid-cols-1 lg:grid-cols-2 gap-4">
      <div class="bg-white rounded shadow-card p-6">
        <h2 class="text-base font-medium text-text-primary mb-4">
          Recent invoices
        </h2>
        <ul v-if="recentInvoices.length" class="space-y-2">
          <li
            v-for="i in recentInvoices"
            :key="i.id"
            class="flex items-center justify-between text-sm py-1.5 border-b border-border last:border-0"
          >
            <span class="font-mono text-text-primary">{{ i.invoiceNumber }}</span>
            <span class="text-text-secondary">{{ i.customerName }}</span>
            <span class="font-medium text-text-primary">${{ Number(i.totalAmount).toFixed(2) }}</span>
          </li>
        </ul>
        <p v-else class="text-text-secondary text-sm">
          No invoices yet.
        </p>
      </div>
      <div class="bg-white rounded shadow-card p-6">
        <h2 class="text-base font-medium text-text-primary mb-4">
          Quick links
        </h2>
        <div class="grid grid-cols-2 gap-2">
          <NuxtLink
            v-for="link in sublinks"
            :key="link.path"
            :to="link.path"
            class="border border-border rounded p-3 hover:border-primary hover:text-primary transition-colors text-text-secondary text-sm flex items-center gap-2"
          >
            <Icon :icon="link.icon" />
            {{ link.title }}
          </NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const store = useFinanceStore()
const { user } = useAuth()
const invoices = ref<any[]>([])
const employees = ref<any[]>([])

const sublinks = [
  { path: '/app/finance/accounts', title: 'Accounts', icon: 'ant-design:account-book-outlined' },
  { path: '/app/finance/invoices', title: 'Invoices', icon: 'ant-design:file-text-outlined' },
  { path: '/app/finance/tax', title: 'Tax', icon: 'ant-design:percentage-outlined' },
  { path: '/app/finance/employees', title: 'Employees', icon: 'ant-design:team-outlined' },
  { path: '/app/finance/payroll', title: 'Payroll', icon: 'ant-design:money-collect-outlined' }
]

const totalReceivables = computed(() =>
  invoices.value.filter(i => i.status === 'OPEN' || i.status === 'PARTIAL').reduce((s, i) => s + Number(i.balanceDue || 0), 0)
)
const totalPayables = computed(() => 0)
const overdueCount = computed(() => invoices.value.filter(i => i.status === 'OVERDUE').length)
const employeeCount = computed(() => employees.value.length)
const recentInvoices = computed(() => invoices.value.slice(0, 5))

onMounted(async () => {
  if (!user.value?.tenantId) { return }
  try {
    await Promise.all([
      store.fetchInvoices(user.value.tenantId),
      store.fetchEmployees(user.value.tenantId)
    ])
    invoices.value = store.invoices || []
    employees.value = store.employees || []
  } catch (e) { /* swallow */ }
})
</script>

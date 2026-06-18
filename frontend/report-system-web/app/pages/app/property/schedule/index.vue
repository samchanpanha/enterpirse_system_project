<template>
  <div>
    <AdminPageHeader
      title="Schedule"
      :subtitle="`${schedules.length} events for this property`"
    />

    <div class="p-6">
      <div v-if="loading" class="text-center py-12 text-text-secondary">
        <Icon icon="ant-design:loading-outlined" class="animate-spin text-2xl" />
      </div>
      <AdminEmpty
        v-else-if="schedules.length === 0"
        icon="ant-design:calendar-outlined"
        title="No scheduled events"
        description="Schedule maintenance, inspections, or rent collection dates."
      />
      <div v-else class="space-y-3">
        <div
          v-for="s in schedules"
          :key="s.id"
          class="bg-white rounded shadow-card p-4 flex items-start gap-4"
        >
          <div class="w-14 h-14 bg-primary-light rounded flex flex-col items-center justify-center text-primary flex-shrink-0">
            <span class="text-xs uppercase font-medium">{{ new Date(s.date).toLocaleString('default', { month: 'short' }) }}</span>
            <span class="text-xl font-bold leading-none">
              {{ new Date(s.date).getDate() }}
            </span>
          </div>
          <div class="flex-1 min-w-0">
            <p class="font-medium text-text-primary">
              {{ s.title }}
            </p>
            <p v-if="s.description" class="text-sm text-text-secondary mt-0.5">
              {{ s.description }}
            </p>
            <p class="text-xs text-text-disabled mt-1">
              {{ new Date(s.date).toLocaleDateString() }}
            </p>
          </div>
          <span :class="['text-xs px-2 py-0.5 rounded', typeClass(s.type)]">
            {{ s.type || 'event' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const route = useRoute()
const propertyId = computed(() => route.params.id as string)

const branchStore = useBranchStore()
const { user } = useAuth()

const schedules = ref<any[]>([])
const loading = ref(false)

function typeClass (type: string): string {
  switch (type) {
    case 'maintenance': return 'bg-warning-light text-warning'
    case 'inspection': return 'bg-primary-light text-primary'
    case 'rent': return 'bg-success-light text-success'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

async function load () {
  if (!user.value?.tenantId) { return }
  loading.value = true
  try {
    const config = useRuntimeConfig()
    const headers: Record<string, string> = {}
    if (user.value.tenantId) { headers['X-Tenant-Id'] = user.value.tenantId }
    if (branchStore.currentBranchId) { headers['X-Branch-Id'] = branchStore.currentBranchId }
    const res: any = await $fetch(`${config.public.apiBaseUrl}/property/schedules/by-property/${propertyId.value}`, { headers })
    schedules.value = Array.isArray(res) ? res : (res?.content || [])
  } catch (e) {
    schedules.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

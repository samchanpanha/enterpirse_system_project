<template>
  <div>
    <AdminPageHeader
      :title="store.currentProperty?.name || 'Property'"
      :subtitle="store.currentProperty ? `${store.currentProperty.address}, ${store.currentProperty.city}` : ''"
    >
      <template #actions>
        <span
          v-if="store.currentProperty"
          :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', store.currentProperty.status === 'active' ? 'bg-success-light text-success' : 'bg-gray-100 text-text-secondary']"
        >
          <span :class="['w-1.5 h-1.5 rounded-full', store.currentProperty.status === 'active' ? 'bg-success' : 'bg-text-disabled']" />
          {{ store.currentProperty.status }}
        </span>
        <NuxtLink
          :to="`/app/property/${id}/schedule`"
          class="text-sm text-primary hover:text-primary-hover inline-flex items-center gap-1"
        >
          <Icon icon="ant-design:calendar-outlined" />
          Schedule
        </NuxtLink>
        <NuxtLink
          :to="`/app/property/${id}/maintenance`"
          class="text-sm text-primary hover:text-primary-hover inline-flex items-center gap-1"
        >
          <Icon icon="ant-design:tool-outlined" />
          Maintenance
        </NuxtLink>
      </template>
    </AdminPageHeader>

    <div v-if="store.loading" class="p-6 text-center text-text-secondary">
      <Icon icon="ant-design:loading-outlined" class="animate-spin text-2xl" />
    </div>

    <div v-else-if="store.currentProperty" class="p-6 grid grid-cols-1 lg:grid-cols-3 gap-4">
      <div class="lg:col-span-2 space-y-4">
        <div class="bg-white rounded shadow-card p-6">
          <h2 class="text-base font-semibold text-text-primary mb-4 flex items-center gap-2">
            <Icon icon="ant-design:home-outlined" class="text-primary" />
            Units ({{ store.units.length }})
          </h2>
          <AdminEmpty
            v-if="store.units.length === 0"
            icon="ant-design:inbox-outlined"
            title="No units yet"
            description="Add units to start tracking leases and rent."
          />
          <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div
              v-for="unit in store.units"
              :key="unit.id"
              class="border border-border rounded p-3 hover:shadow-card cursor-pointer transition-shadow"
              @click="selectedUnit = unit"
            >
              <div class="flex items-start justify-between mb-1">
                <p class="font-medium text-text-primary">
                  {{ unit.label }}
                </p>
                <span :class="['text-xs px-1.5 py-0.5 rounded', unit.status === 'occupied' ? 'bg-warning-light text-warning' : 'bg-success-light text-success']">
                  {{ unit.status }}
                </span>
              </div>
              <p class="text-sm text-text-secondary">
                {{ unit.bedrooms }} bed · Floor {{ unit.floor }}
              </p>
              <p class="text-base font-semibold text-primary mt-1">
                ${{ unit.rentAmount.toLocaleString() }}/mo
              </p>
            </div>
          </div>
        </div>
      </div>

      <div class="space-y-4">
        <div class="bg-white rounded shadow-card p-6">
          <h2 class="text-base font-semibold text-text-primary mb-3">
            Details
          </h2>
          <dl class="space-y-2 text-sm">
            <div class="flex justify-between">
              <dt class="text-text-secondary">
                Address
              </dt>
              <dd class="text-text-primary">
                {{ store.currentProperty.address }}
              </dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-text-secondary">
                City
              </dt>
              <dd class="text-text-primary">
                {{ store.currentProperty.city }}
              </dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-text-secondary">
                District
              </dt>
              <dd class="text-text-primary">
                {{ store.currentProperty.district || '—' }}
              </dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-text-secondary">
                Type
              </dt>
              <dd class="text-text-primary capitalize">
                {{ store.currentProperty.type }}
              </dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-text-secondary">
                Total Units
              </dt>
              <dd class="text-text-primary font-mono">
                {{ store.currentProperty.totalUnits }}
              </dd>
            </div>
          </dl>
        </div>

        <div class="bg-white rounded shadow-card p-6">
          <h2 class="text-base font-semibold text-text-primary mb-3">
            Quick Links
          </h2>
          <div class="space-y-1">
            <NuxtLink
              :to="`/app/property/${id}/schedule`"
              class="flex items-center gap-2 px-2 py-1.5 text-sm text-text-primary hover:bg-primary-light hover:text-primary rounded transition-colors"
            >
              <Icon icon="ant-design:calendar-outlined" />
              Schedule
            </NuxtLink>
            <NuxtLink
              :to="`/app/property/${id}/maintenance`"
              class="flex items-center gap-2 px-2 py-1.5 text-sm text-text-primary hover:bg-primary-light hover:text-primary rounded transition-colors"
            >
              <Icon icon="ant-design:tool-outlined" />
              Maintenance
            </NuxtLink>
          </div>
        </div>
      </div>
    </div>

    <AdminDrawer
      v-model="unitDrawer.open.value"
      :title="unitDrawer.editing.value?.label || 'Unit'"
      :subtitle="unitDrawer.editing.value ? `${unitDrawer.editing.value.bedrooms} bed · Floor ${unitDrawer.editing.value.floor}` : ''"
      width="md"
    >
      <div v-if="unitDrawer.editing.value" class="space-y-3">
        <div class="flex items-center justify-between py-2 border-b border-border">
          <span class="text-text-secondary text-sm">Status</span>
          <span :class="['text-xs px-2 py-0.5 rounded', unitDrawer.editing.value.status === 'occupied' ? 'bg-warning-light text-warning' : 'bg-success-light text-success']">
            {{ unitDrawer.editing.value.status }}
          </span>
        </div>
        <div class="flex items-center justify-between py-2 border-b border-border">
          <span class="text-text-secondary text-sm">Rent</span>
          <span class="text-primary font-semibold">${{ unitDrawer.editing.value.rentAmount.toLocaleString() }}/mo</span>
        </div>
        <div class="flex items-center justify-between py-2 border-b border-border">
          <span class="text-text-secondary text-sm">Floor</span>
          <span class="text-text-primary font-mono">{{ unitDrawer.editing.value.floor }}</span>
        </div>
        <div class="flex items-center justify-between py-2 border-b border-border">
          <span class="text-text-secondary text-sm">Bedrooms</span>
          <span class="text-text-primary font-mono">{{ unitDrawer.editing.value.bedrooms }}</span>
        </div>
      </div>
      <template #footer>
        <button
          class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50"
          @click="unitDrawer.close()"
        >
          Close
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { Unit } from '~/shared/types/property'

definePageMeta({ middleware: 'auth' })

const route = useRoute()
const id = computed(() => route.params.id as string)
const store = usePropertyStore()
const selectedUnit = ref<Unit | null>(null)

const unitDrawer = useDrawer<Unit>()

watch(selectedUnit, (u) => {
  if (u) { unitDrawer.openView(u) }
})

onMounted(async () => {
  await store.fetchProperty(id.value)
  await store.fetchUnits(id.value)
})
</script>

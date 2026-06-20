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
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-base font-semibold text-text-primary flex items-center gap-2">
              <Icon icon="ant-design:home-outlined" class="text-primary" />
              Units ({{ store.units.length }})
            </h2>
            <button
              v-if="can('property.write')"
              class="inline-flex items-center gap-1 text-xs px-2 py-1 bg-primary text-white rounded hover:bg-primary-hover transition-colors"
              @click="openUnitCreate"
            >
              <Icon icon="ant-design:plus-outlined" />
              Add Unit
            </button>
          </div>
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
        <button
          v-if="can('property.write') && unitDrawer.editing.value.status !== 'occupied'"
          class="w-full mt-2 px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover"
          @click="openLeaseCreate(unitDrawer.editing.value)"
        >
          Create Lease
        </button>
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

    <AdminDrawer
      v-model="unitCreateDrawer.open.value"
      title="Add Unit"
      width="md"
    >
      <AdminForm
        v-model="unitCreateForm"
        :groups="unitCreateFormGroups"
      />
      <template #footer>
        <button
          class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50"
          @click="unitCreateDrawer.close()"
        >
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover"
          :disabled="!canCreateUnit"
          @click="submitUnitCreate"
        >
          Add Unit
        </button>
      </template>
    </AdminDrawer>

    <AdminDrawer
      v-model="leaseCreateDrawer.open.value"
      title="Create Lease"
      width="md"
    >
      <AdminForm
        v-model="leaseCreateForm"
        :groups="leaseCreateFormGroups"
      />
      <template #footer>
        <button
          class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50"
          @click="leaseCreateDrawer.close()"
        >
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover"
          :disabled="!canCreateLease"
          @click="submitLeaseCreate"
        >
          Create Lease
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { Unit } from '~/shared/types/property'
import type { FormGroup } from '~/shared/types/form'

definePageMeta({ middleware: 'auth' })

const route = useRoute()
const id = computed(() => route.params.id as string)
const store = usePropertyStore()
const { can } = usePermission()
const { user } = useAuth()
const { text, number, date, group } = useFormSchema()

const selectedUnit = ref<Unit | null>(null)
const unitDrawer = useDrawer<Unit>()
const unitCreateDrawer = useDrawer()
const leaseCreateDrawer = useDrawer<Unit>()

const unitCreateForm = ref({
  label: '',
  floor: 1,
  bedrooms: 1,
  bathrooms: 1,
  rentAmount: 0
})

const leaseCreateForm = ref({
  tenantName: '',
  tenantPhone: '',
  startDate: '',
  endDate: '',
  rentAmount: 0,
  depositAmount: 0
})

const unitCreateFormGroups = computed<FormGroup[]>(() => [
  group('', [
    text('label', 'Unit Label', { required: true, placeholder: 'A-101' }),
    number('floor', 'Floor', { required: true }),
    number('bedrooms', 'Bedrooms', { required: true }),
    number('bathrooms', 'Bathrooms', { required: true }),
    number('rentAmount', 'Monthly Rent', { required: true, min: 0 })
  ])
])

const leaseCreateFormGroups = computed<FormGroup[]>(() => [
  group('', [
    text('tenantName', 'Tenant Name', { required: true }),
    text('tenantPhone', 'Tenant Phone', { placeholder: '+855...' }),
    date('startDate', 'Start Date', { required: true }),
    date('endDate', 'End Date', { required: true }),
    number('rentAmount', 'Monthly Rent', { required: true, min: 0 }),
    number('depositAmount', 'Deposit', { min: 0 })
  ])
])

const canCreateUnit = computed(() =>
  unitCreateForm.value.label &&
  unitCreateForm.value.floor >= 0 &&
  unitCreateForm.value.bedrooms >= 0 &&
  unitCreateForm.value.bathrooms >= 0 &&
  unitCreateForm.value.rentAmount >= 0
)

const canCreateLease = computed(() =>
  leaseCreateForm.value.tenantName &&
  leaseCreateForm.value.startDate &&
  leaseCreateForm.value.endDate &&
  leaseCreateForm.value.rentAmount > 0
)

watch(selectedUnit, (u) => {
  if (u) { unitDrawer.openView(u) }
})

function openUnitCreate () {
  unitCreateForm.value = {
    label: '',
    floor: 1,
    bedrooms: 1,
    bathrooms: 1,
    rentAmount: 0
  }
  unitCreateDrawer.openFor(null)
}

async function submitUnitCreate () {
  if (!user.value?.tenantId) { return }
  try {
    await store.createUnit({
      tenantId: user.value.tenantId,
      propertyId: id.value,
      ...unitCreateForm.value
    })
    unitCreateDrawer.close()
    await store.fetchUnits(id.value)
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to add unit')
  }
}

function openLeaseCreate (unit: Unit) {
  leaseCreateForm.value = {
    tenantName: '',
    tenantPhone: '',
    startDate: new Date().toISOString().slice(0, 10),
    endDate: '',
    rentAmount: unit.rentAmount || 0,
    depositAmount: 0
  }
  leaseCreateDrawer.openFor(unit)
}

async function submitLeaseCreate () {
  const unit = leaseCreateDrawer.editing.value
  if (!unit || !user.value?.tenantId) { return }
  try {
    await store.createLease({
      tenantId: user.value.tenantId,
      unitId: unit.id,
      ...leaseCreateForm.value
    })
    leaseCreateDrawer.close()
    unitDrawer.close()
    await store.fetchUnits(id.value)
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to create lease')
  }
}

onMounted(async () => {
  await store.fetchProperty(id.value)
  await store.fetchUnits(id.value)
})
</script>

import { defineStore } from 'pinia'
import type { Property, Unit, Lease } from '~/shared/types/property'

export const usePropertyStore = defineStore('property', () => {
  const branchStore = useBranchStore()
  const properties = ref<Property[]>([])
  const currentProperty = ref<Property | null>(null)
  const units = ref<Unit[]>([])
  const currentUnit = ref<Unit | null>(null)
  const leases = ref<Lease[]>([])
  const loading = ref(false)

  const fetchProperties = async (tenantId: string) => {
    loading.value = true
    try {
      properties.value = await branchStore.$apiWithBranch<Property[]>(`/property/properties/by-tenant/${tenantId}`)
    } finally {
      loading.value = false
    }
  }

  const fetchProperty = async (id: string) => {
    loading.value = true
    try {
      currentProperty.value = await branchStore.$apiWithBranch<Property>(`/property/properties/${id}`)
    } finally {
      loading.value = false
    }
  }

  const createProperty = async (property: Partial<Property>) => {
    const created = await branchStore.$apiWithBranch<Property>('/property/properties', {
      method: 'POST',
      body: property
    })
    properties.value.push(created)
    return created
  }

  const fetchUnits = async (propertyId: string) => {
    loading.value = true
    try {
      units.value = await branchStore.$apiWithBranch<Unit[]>(`/property/units/by-property/${propertyId}`)
    } finally {
      loading.value = false
    }
  }

  const createUnit = async (unit: Partial<Unit>) => {
    const created = await branchStore.$apiWithBranch<Unit>('/property/units', {
      method: 'POST',
      body: unit
    })
    units.value.push(created)
    return created
  }

  const fetchLeases = async (unitId: string) => {
    loading.value = true
    try {
      leases.value = await branchStore.$apiWithBranch<Lease[]>(`/property/leases/by-unit/${unitId}`)
    } finally {
      loading.value = false
    }
  }

  const updateProperty = async (id: string, data: Partial<Property>) => {
    const updated = await branchStore.$apiWithBranch<Property>(`/property/properties/${id}`, { method: 'PUT', body: data })
    const idx = properties.value.findIndex(p => p.id === id)
    if (idx >= 0) { properties.value[idx] = updated }
    if (currentProperty.value?.id === id) { currentProperty.value = updated }
    return updated
  }

  const deleteProperty = async (id: string) => {
    await branchStore.$apiWithBranch(`/property/properties/${id}`, { method: 'DELETE' })
    properties.value = properties.value.filter(p => p.id !== id)
    if (currentProperty.value?.id === id) { currentProperty.value = null }
  }

  const createLease = async (lease: Partial<Lease>) => {
    const created = await branchStore.$apiWithBranch<Lease>('/property/leases', {
      method: 'POST',
      body: lease
    })
    leases.value.push(created)
    return created
  }

  return {
    properties,
    currentProperty,
    units,
    currentUnit,
    leases,
    loading,
    fetchProperties,
    fetchProperty,
    createProperty,
    fetchUnits,
    createUnit,
    fetchLeases,
    createLease,
    updateProperty,
    deleteProperty
  }
})

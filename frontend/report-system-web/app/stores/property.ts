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

  const fetchLeases = async (unitId: string) => {
    loading.value = true
    try {
      leases.value = await branchStore.$apiWithBranch<Lease[]>(`/property/leases/by-unit/${unitId}`)
    } finally {
      loading.value = false
    }
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
    fetchLeases
  }
})

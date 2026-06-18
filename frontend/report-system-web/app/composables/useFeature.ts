import { computed } from 'vue'
import { useFeatureStore } from '~/stores/feature'

/**
 * useFeature — gate UI elements by client-level feature code.
 *
 * Examples:
 *   const { has } = useFeature()
 *   const v = has('inventory.stock_transfers')
 *
 *   <button v-if="has('restaurant.loyalty')">Open loyalty</button>
 */
export const useFeature = () => {
  const store = useFeatureStore()

  function has (code: string): boolean {
    if (!code) { return true }
    return store.has(code)
  }

  function hasAny (codes: string[]): boolean {
    return store.hasAny(codes)
  }

  function hasAll (codes: string[]): boolean {
    return store.hasAll(codes)
  }

  return {
    has,
    hasAny,
    hasAll,
    enabled: computed(() => Array.from(store.enabled))
  }
}

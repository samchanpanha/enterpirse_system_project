import { computed } from 'vue'
import { usePermissionStore } from '~/stores/permission'

/**
 * usePermission — gate UI elements by permission code.
 *
 * The permission store is populated on login (from JWT claims + /api/me).
 * A permission code is dot-separated: "module.action" or "module.sub.action".
 *
 * Examples:
 *   const { can } = usePermission()
 *   const mayEdit = can('branches.update')
 *
 *   <button v-if="can('inventory:transfer:create')">Create transfer</button>
 */
export const usePermission = () => {
  const store = usePermissionStore()

  function can (code: string): boolean {
    if (!code) { return true }
    return store.has(code)
  }

  function cannot (code: string): boolean {
    return !can(code)
  }

  function canAny (codes: string[]): boolean {
    return codes.some(c => can(c))
  }

  function canAll (codes: string[]): boolean {
    return codes.every(c => can(c))
  }

  return {
    can,
    cannot,
    canAny,
    canAll,
    /** All loaded permission codes (for debugging) */
    codes: computed(() => Array.from(store.codes))
  }
}

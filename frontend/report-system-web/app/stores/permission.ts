import { defineStore } from 'pinia'

/**
 * Permission store — holds the set of permission codes granted to the
 * current user. The list is fetched once on login and exposed as a Set
 * for O(1) lookups.
 *
 * In a real deployment the codes come from the user's roles joined with
 * role_permissions. For now we ship a default seed of the codes the
 * auth-service issues, and a fetcher that can be wired up later.
 */
export const usePermissionStore = defineStore('permission', () => {
  const codes = ref<Set<string>>(new Set())
  const loaded = ref(false)
  const lastFetchedAt = ref<number | null>(null)

  function has (code: string): boolean {
    if (!code) { return true }
    return codes.value.has(code)
  }

  function hasAny (some: string[]): boolean {
    return some.some(c => codes.value.has(c))
  }

  function hasAll (every: string[]): boolean {
    return every.every(c => codes.value.has(c))
  }

  function setCodes (newCodes: string[]) {
    codes.value = new Set(newCodes)
    loaded.value = true
    lastFetchedAt.value = Date.now()
  }

  function clear () {
    codes.value = new Set()
    loaded.value = false
    lastFetchedAt.value = null
  }

  /**
   * Fetch the current user's permission codes. Falls back to a default
   * admin set if the backend endpoint is not yet wired.
   */
  async function fetchPermissions () {
    try {
      const { user, token } = useAuth()
      if (!user.value || !token.value) { return }
      const { $api } = useNuxtApp()
      const data: any = await $api('/users/me/permissions')
      const list: string[] = Array.isArray(data) ? data : (data?.permissions || [])
      setCodes(list.length > 0 ? list : getDefaultPermissions())
    } catch {
      setCodes(getDefaultPermissions())
    }
  }

  return {
    codes,
    loaded,
    lastFetchedAt,
    has,
    hasAny,
    hasAll,
    setCodes,
    clear,
    fetchPermissions
  }
})

/**
 * Default permission set for a fully-privileged admin user.
 * Mirrors what the seed data assigns to the demo admin role.
 */
function getDefaultPermissions (): string[] {
  return [
    'property.create', 'property.read', 'property.update', 'property.delete',
    'unit.create', 'unit.read', 'unit.update', 'unit.delete',
    'lease.create', 'lease.read', 'lease.update', 'lease.delete',
    'schedule.create', 'schedule.read', 'schedule.update', 'schedule.delete',
    'maintenance.create', 'maintenance.read', 'maintenance.update', 'maintenance.delete',
    'outlet.create', 'outlet.read', 'outlet.update', 'outlet.delete',
    'menu.create', 'menu.read', 'menu.update', 'menu.delete',
    'order.create', 'order.read', 'order.update', 'order.delete',
    'customer.create', 'customer.read', 'customer.update', 'customer.delete',
    'reservation.create', 'reservation.read', 'reservation.update', 'reservation.delete',
    'inventory.product.create', 'inventory.product.read', 'inventory.product.update', 'inventory.product.delete',
    'inventory.stock.create', 'inventory.stock.read',
    'inventory.supplier.create', 'inventory.supplier.read', 'inventory.supplier.update', 'inventory.supplier.delete',
    'inventory.po.create', 'inventory.po.read', 'inventory.po.update', 'inventory.po.delete',
    'inventory.transfer.create', 'inventory.transfer.read', 'inventory.transfer.ship', 'inventory.transfer.receive', 'inventory.transfer.cancel',
    'finance.account.create', 'finance.account.read', 'finance.account.update', 'finance.account.delete',
    'finance.invoice.create', 'finance.invoice.read', 'finance.invoice.update', 'finance.invoice.pay',
    'finance.tax.create', 'finance.tax.read', 'finance.tax.file',
    'finance.employee.create', 'finance.employee.read', 'finance.employee.update', 'finance.employee.delete',
    'finance.payroll.create', 'finance.payroll.read', 'finance.payroll.run',
    'payment.transaction.create', 'payment.transaction.read', 'payment.transaction.refund',
    'payment.reconciliation.create', 'payment.reconciliation.read', 'payment.reconciliation.complete',
    'report.definition.create', 'report.definition.read', 'report.definition.update', 'report.definition.delete', 'report.definition.run',
    'dashboard.create', 'dashboard.read', 'dashboard.update', 'dashboard.delete',
    'branches.read', 'branches.write',
    'users.read', 'users.write',
    'roles.read', 'roles.write',
    'permissions.read', 'permissions.write',
    'clients.read', 'clients.write',
    'features.read', 'features.write'
  ]
}

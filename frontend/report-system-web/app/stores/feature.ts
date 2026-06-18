import { defineStore } from 'pinia'

/**
 * Feature store — holds the set of feature codes enabled for the current
 * tenant. Mirrors the permission store but for client-level features.
 *
 * `enabled` is a Set<string> of feature codes like "inventory.stock_transfers".
 * `tree` is the module → features structure for admin UI.
 */
export const useFeatureStore = defineStore('feature', () => {
  const enabled = ref<Set<string>>(new Set())
  const tree = ref<Array<{ module: string; features: any[] }>>([])
  const tier = ref<string>('BASIC')
  const loaded = ref(false)

  function has (code: string): boolean {
    if (!code) { return true }
    return enabled.value.has(code)
  }

  function hasAny (codes: string[]): boolean {
    return codes.some(c => enabled.value.has(c))
  }

  function hasAll (codes: string[]): boolean {
    return codes.every(c => enabled.value.has(c))
  }

  function setEnabled (codes: string[]) {
    enabled.value = new Set(codes)
    loaded.value = true
  }

  function setTree (t: Array<{ module: string; features: any[] }>) {
    tree.value = t
  }

  function setTier (t: string) {
    tier.value = t
  }

  function clear () {
    enabled.value = new Set()
    tree.value = []
    loaded.value = false
  }

  /**
   * Fetch enabled features for the current tenant from the auth-service.
   * The gateway routes /api/features/** to auth-service.
   */
  async function fetchFeatures () {
    try {
      const config = useRuntimeConfig()
      const { user, token } = useAuth()
      if (!user.value?.tenantId) { return }
      const headers: Record<string, string> = {
        'X-Tenant-Id': user.value.tenantId
      }
      if (token.value) { headers.Authorization = `Bearer ${token.value}` }
      const data: any = await $fetch(`${config.public.apiBaseUrl}/api/features/enabled`, { headers })
      const codes: string[] = data ? Object.entries(data).filter(([_, v]) => v).map(([k]) => k) : []
      setEnabled(codes)
    } catch (e) {
      // Dev fallback: all features enabled
      setEnabled([
        'inventory.products', 'inventory.suppliers', 'inventory.purchase_orders', 'inventory.stock_transfers', 'inventory.multi_warehouse', 'inventory.barcode',
        'restaurant.pos', 'restaurant.kds', 'restaurant.menu', 'restaurant.orders', 'restaurant.reservations', 'restaurant.customers', 'restaurant.loyalty', 'restaurant.delivery',
        'property.properties', 'property.units', 'property.leases', 'property.rent_collection', 'property.maintenance', 'property.schedule', 'property.tenant_portal',
        'finance.accounts', 'finance.invoices', 'finance.tax', 'finance.employees', 'finance.payroll', 'finance.journal', 'finance.budgets', 'finance.audit',
        'payment.cash', 'payment.aba', 'payment.wing', 'payment.pi_pay', 'payment.reconciliation', 'payment.refunds',
        'reports.definitions', 'reports.scheduled', 'reports.dashboards', 'reports.export', 'reports.cross_branch',
        'platform.branches', 'platform.users', 'platform.roles', 'platform.audit_log', 'platform.api_access', 'platform.sso', 'platform.white_label'
      ])
    }
  }

  return {
    enabled,
    tree,
    tier,
    loaded,
    has,
    hasAny,
    hasAll,
    setEnabled,
    setTree,
    setTier,
    clear,
    fetchFeatures
  }
})

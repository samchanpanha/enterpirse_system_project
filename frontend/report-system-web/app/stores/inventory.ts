import { defineStore } from 'pinia'
import type { Product, Supplier, PurchaseOrder, StockEntry, StockExit, StockTransfer } from '~/shared/types/inventory'

export const useInventoryStore = defineStore('inventory', () => {
  const branchStore = useBranchStore()

  const products = ref<Product[]>([])
  const suppliers = ref<Supplier[]>([])
  const purchaseOrders = ref<PurchaseOrder[]>([])
  const transfers = ref<StockTransfer[]>([])
  const loading = ref(false)

  async function fetchProducts (tenantId: string) {
    loading.value = true
    try { products.value = await branchStore.$apiWithBranch(`/inventory/products/by-tenant/${tenantId}`) } finally { loading.value = false }
  }
  async function fetchProduct (id: string) {
    return await branchStore.$apiWithBranch<Product>(`/inventory/products/${id}`)
  }
  async function createProduct (data: Partial<Product>) {
    const p = await branchStore.$apiWithBranch('/inventory/products', { method: 'POST', body: data })
    products.value.push(p)
    return p
  }
  async function updateProduct (id: string, data: Partial<Product>) {
    const p = await branchStore.$apiWithBranch(`/inventory/products/${id}`, { method: 'PUT', body: data })
    const idx = products.value.findIndex(i => i.id === id)
    if (idx >= 0) { products.value[idx] = p }
    return p
  }

  async function fetchSuppliers (tenantId: string) {
    loading.value = true
    try { suppliers.value = await branchStore.$apiWithBranch(`/inventory/suppliers/by-tenant/${tenantId}`) } finally { loading.value = false }
  }
  async function createSupplier (data: Partial<Supplier>) {
    const s = await branchStore.$apiWithBranch('/inventory/suppliers', { method: 'POST', body: data })
    suppliers.value.push(s)
    return s
  }
  async function updateSupplier (id: string, data: Partial<Supplier>) {
    const s = await branchStore.$apiWithBranch<Supplier>(`/inventory/suppliers/${id}`, { method: 'PUT', body: data })
    const idx = suppliers.value.findIndex(x => x.id === id)
    if (idx >= 0) { suppliers.value[idx] = s }
    return s
  }
  async function deleteSupplier (id: string) {
    await branchStore.$apiWithBranch(`/inventory/suppliers/${id}`, { method: 'DELETE' })
    suppliers.value = suppliers.value.filter(s => s.id !== id)
  }

  async function fetchPurchaseOrders (tenantId: string) {
    loading.value = true
    try { purchaseOrders.value = await branchStore.$apiWithBranch(`/inventory/purchase-orders/by-tenant/${tenantId}`) } finally { loading.value = false }
  }
  async function createPurchaseOrder (data: Partial<PurchaseOrder>) {
    const po = await branchStore.$apiWithBranch('/inventory/purchase-orders', { method: 'POST', body: data })
    purchaseOrders.value.push(po)
    return po
  }
  async function addPoItem (poId: string, data: any) {
    return await branchStore.$apiWithBranch(`/inventory/purchase-orders/${poId}/items`, { method: 'POST', body: data })
  }
  async function receivePurchaseOrder (poId: string) {
    const po = await branchStore.$apiWithBranch(`/inventory/purchase-orders/${poId}/receive`, { method: 'POST' })
    const idx = purchaseOrders.value.findIndex(i => i.id === poId)
    if (idx >= 0) { purchaseOrders.value[idx] = po }
    return po
  }
  async function cancelPurchaseOrder (poId: string) {
    const po = await branchStore.$apiWithBranch(`/inventory/purchase-orders/${poId}/cancel`, { method: 'POST' })
    const idx = purchaseOrders.value.findIndex(i => i.id === poId)
    if (idx >= 0) { purchaseOrders.value[idx] = po }
    return po
  }

  async function createStockEntry (data: Partial<StockEntry>) {
    return await branchStore.$apiWithBranch('/inventory/stock/entry', { method: 'POST', body: data })
  }
  async function createStockExit (data: Partial<StockExit>) {
    return await branchStore.$apiWithBranch('/inventory/stock/exit', { method: 'POST', body: data })
  }
  async function getStock (tenantId: string, productId: string) {
    return await branchStore.$apiWithBranch<{ productId: string; quantity: number }>(`/inventory/stock/current/${tenantId}/${productId}`)
  }

  // ─── Stock Transfers ─────────────────────────────────────────────────────
  async function fetchTransfers (tenantId: string, status?: string) {
    loading.value = true
    try {
      const url = status
        ? `/inventory/transfers?tenantId=${tenantId}&status=${status}`
        : `/inventory/transfers?tenantId=${tenantId}`
      transfers.value = await branchStore.$apiWithBranch<StockTransfer[]>(url)
    } finally { loading.value = false }
  }

  async function fetchIncomingTransfers (branchId: string) {
    return await branchStore.$apiWithBranch<StockTransfer[]>(`/inventory/transfers/incoming/${branchId}`)
  }

  async function createTransfer (data: {
    tenantId: string
    fromBranchId: string
    toBranchId: string
    notes?: string
    items: { productId: string; quantity: number; unitCost: number }[]
  }) {
    const t = await branchStore.$apiWithBranch<StockTransfer>('/inventory/transfers', { method: 'POST', body: data })
    transfers.value.unshift(t)
    return t
  }

  async function shipTransfer (id: string) {
    const t = await branchStore.$apiWithBranch<StockTransfer>(`/inventory/transfers/${id}/ship`, { method: 'POST' })
    updateLocalTransfer(t)
    return t
  }

  async function receiveTransfer (id: string, lines?: { productId: string; receivedQuantity: number; discrepancyNotes?: string }[]) {
    const t = await branchStore.$apiWithBranch<StockTransfer>(`/inventory/transfers/${id}/receive`, { method: 'POST', body: { lines } })
    updateLocalTransfer(t)
    return t
  }

  async function cancelTransfer (id: string, reason: string) {
    const t = await branchStore.$apiWithBranch<StockTransfer>(`/inventory/transfers/${id}/cancel`, { method: 'POST', body: { reason } })
    updateLocalTransfer(t)
    return t
  }

  function updateLocalTransfer (t: StockTransfer) {
    const idx = transfers.value.findIndex(x => x.id === t.id)
    if (idx >= 0) { transfers.value[idx] = t } else { transfers.value.unshift(t) }
  }

  return {
    products,
    suppliers,
    purchaseOrders,
    transfers,
    loading,
    fetchProducts,
    fetchProduct,
    createProduct,
    updateProduct,
    fetchSuppliers,
    createSupplier,
    updateSupplier,
    deleteSupplier,
    fetchPurchaseOrders,
    createPurchaseOrder,
    addPoItem,
    receivePurchaseOrder,
    cancelPurchaseOrder,
    createStockEntry,
    createStockExit,
    getStock,
    fetchTransfers,
    fetchIncomingTransfers,
    createTransfer,
    shipTransfer,
    receiveTransfer,
    cancelTransfer
  }
})

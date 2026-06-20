import { defineStore } from 'pinia'
import type { Outlet, RestaurantTable, Category, MenuItem, Order, OrderItem, Customer, Reservation } from '~/shared/types/restaurant'

export const useRestaurantStore = defineStore('restaurant', () => {
  const branchStore = useBranchStore()

  const outlets = ref<Outlet[]>([])
  const currentOutlet = ref<Outlet | null>(null)
  const tables = ref<RestaurantTable[]>([])
  const categories = ref<Category[]>([])
  const menuItems = ref<MenuItem[]>([])
  const orders = ref<Order[]>([])
  const currentOrder = ref<Order | null>(null)
  const orderItems = ref<OrderItem[]>([])
  const customers = ref<Customer[]>([])
  const reservations = ref<Reservation[]>([])
  const loading = ref(false)

  async function fetchOutlets (tenantId: string) {
    loading.value = true
    try { outlets.value = await branchStore.$apiWithBranch(`/restaurant/outlets/by-tenant/${tenantId}`) } finally { loading.value = false }
  }
  async function fetchOutlet (id: string) {
    currentOutlet.value = await branchStore.$apiWithBranch(`/restaurant/outlets/${id}`)
  }
  async function createOutlet (data: Partial<Outlet>) {
    const o = await branchStore.$apiWithBranch('/restaurant/outlets', { method: 'POST', body: data })
    outlets.value.push(o)
    return o
  }

  async function fetchTables (outletId: string) {
    tables.value = await branchStore.$apiWithBranch(`/restaurant/tables/by-outlet/${outletId}`)
  }

  async function fetchCategories (tenantId: string, outletId: string) {
    loading.value = true
    try { categories.value = await branchStore.$apiWithBranch(`/restaurant/menu/categories/${tenantId}/${outletId}`) } finally { loading.value = false }
  }
  async function createCategory (data: Partial<Category>) {
    const c = await branchStore.$apiWithBranch('/restaurant/menu/categories', { method: 'POST', body: data })
    categories.value.push(c)
    return c
  }
  async function updateCategory (id: string, data: Partial<Category>) {
    const c = await branchStore.$apiWithBranch<Category>(`/restaurant/menu/categories/${id}`, { method: 'PUT', body: data })
    const idx = categories.value.findIndex(x => x.id === id)
    if (idx >= 0) { categories.value[idx] = c }
    return c
  }
  async function deleteCategory (id: string) {
    await branchStore.$apiWithBranch(`/restaurant/menu/categories/${id}`, { method: 'DELETE' })
    categories.value = categories.value.filter(c => c.id !== id)
  }

  async function fetchMenuItems (tenantId: string) {
    loading.value = true
    try { menuItems.value = await branchStore.$apiWithBranch(`/restaurant/menu/items/active/${tenantId}`) } finally { loading.value = false }
  }
  async function fetchItemsByCategory (categoryId: string) {
    return await branchStore.$apiWithBranch<MenuItem[]>(`/restaurant/menu/items/by-category/${categoryId}`)
  }
  async function createMenuItem (data: Partial<MenuItem>) {
    const m = await branchStore.$apiWithBranch('/restaurant/menu/items', { method: 'POST', body: data })
    menuItems.value.push(m)
    return m
  }
  async function updateMenuItem (id: string, data: Partial<MenuItem>) {
    const m = await branchStore.$apiWithBranch(`/restaurant/menu/items/${id}`, { method: 'PUT', body: data })
    const idx = menuItems.value.findIndex(i => i.id === id)
    if (idx >= 0) { menuItems.value[idx] = m }
    return m
  }
  async function deleteMenuItem (id: string) {
    await branchStore.$apiWithBranch(`/restaurant/menu/items/${id}`, { method: 'DELETE' })
    menuItems.value = menuItems.value.filter(i => i.id !== id)
  }

  async function createOrder (data: Partial<Order>) {
    const o = await branchStore.$apiWithBranch('/restaurant/orders', { method: 'POST', body: data })
    orders.value.push(o)
    return o
  }
  async function fetchOrders (outletId: string, status = 'pending') {
    loading.value = true
    try { orders.value = await branchStore.$apiWithBranch(`/restaurant/orders/by-outlet/${outletId}?status=${status}`) } finally { loading.value = false }
  }
  async function fetchOrder (id: string) {
    currentOrder.value = await branchStore.$apiWithBranch(`/restaurant/orders/${id}`)
  }
  async function addOrderItem (orderId: string, data: any) {
    return await branchStore.$apiWithBranch(`/restaurant/orders/${orderId}/items`, { method: 'POST', body: data })
  }
  async function removeOrderItem (orderId: string, itemId: string) {
    return await branchStore.$apiWithBranch(`/restaurant/orders/${orderId}/items/${itemId}`, { method: 'DELETE' })
  }
  async function updateOrderStatus (orderId: string, status: string) {
    const o = await branchStore.$apiWithBranch(`/restaurant/orders/${orderId}/status`, { method: 'PUT', body: { status } })
    const idx = orders.value.findIndex(i => i.id === orderId)
    if (idx >= 0) { orders.value[idx] = o }
    return o
  }
  async function completeOrder (orderId: string, data: any) {
    const o = await branchStore.$apiWithBranch(`/restaurant/orders/${orderId}/complete`, { method: 'POST', body: data })
    orders.value = orders.value.filter(i => i.id !== orderId)
    return o
  }
  async function fetchOrderItems (orderId: string) {
    orderItems.value = await branchStore.$apiWithBranch(`/restaurant/orders/${orderId}/items`)
  }

  async function fetchCustomers (outletId: string) {
    loading.value = true
    try { customers.value = await branchStore.$apiWithBranch(`/restaurant/customers/by-outlet/${outletId}`) } finally { loading.value = false }
  }
  async function createCustomer (data: Partial<Customer>) {
    const c = await branchStore.$apiWithBranch('/restaurant/customers', { method: 'POST', body: data })
    customers.value.push(c)
    return c
  }
  async function updateCustomer (id: string, data: Partial<Customer>) {
    const c = await branchStore.$apiWithBranch<Customer>(`/restaurant/customers/${id}`, { method: 'PUT', body: data })
    const idx = customers.value.findIndex(x => x.id === id)
    if (idx >= 0) { customers.value[idx] = c }
    return c
  }

  async function fetchReservations (outletId: string, date?: string) {
    loading.value = true
    try {
      reservations.value = await branchStore.$apiWithBranch(`/restaurant/reservations/by-outlet/${outletId}${date ? `?date=${date}` : ''}`)
    } finally { loading.value = false }
  }
  async function createReservation (data: Partial<Reservation>) {
    const r = await branchStore.$apiWithBranch('/restaurant/reservations', { method: 'POST', body: data })
    reservations.value.push(r)
    return r
  }
  async function updateReservationStatus (id: string, status: string) {
    const r = await branchStore.$apiWithBranch(`/restaurant/reservations/${id}/status`, { method: 'PUT', body: { status } })
    const idx = reservations.value.findIndex(i => i.id === id)
    if (idx >= 0) { reservations.value[idx] = r }
    return r
  }
  async function cancelReservation (id: string) {
    await branchStore.$apiWithBranch(`/restaurant/reservations/${id}/cancel`, { method: 'POST' })
    reservations.value = reservations.value.filter(i => i.id !== id)
  }

  return {
    outlets,
    currentOutlet,
    tables,
    categories,
    menuItems,
    orders,
    currentOrder,
    orderItems,
    customers,
    reservations,
    loading,
    fetchOutlets,
    fetchOutlet,
    createOutlet,
    fetchTables,
    fetchCategories,
    createCategory,
    updateCategory,
    deleteCategory,
    fetchMenuItems,
    fetchItemsByCategory,
    createMenuItem,
    updateMenuItem,
    deleteMenuItem,
    createOrder,
    fetchOrders,
    fetchOrder,
    addOrderItem,
    removeOrderItem,
    updateOrderStatus,
    completeOrder,
    fetchOrderItems,
    fetchCustomers,
    createCustomer,
    updateCustomer,
    fetchReservations,
    createReservation,
    updateReservationStatus,
    cancelReservation
  }
})

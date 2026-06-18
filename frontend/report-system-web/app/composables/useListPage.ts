import { ref, computed, reactive } from 'vue'

export interface ListPageOptions {
  /** Default page size */
  pageSize?: number
  /** Default sort field */
  defaultSort?: string
  /** Default sort direction */
  defaultOrder?: 'asc' | 'desc'
  /** Reset page to 1 when filters change */
  resetOnFilterChange?: boolean
  /** Auto-fetch on mount (default: true) */
  autoFetch?: boolean
  /** Initial filter values */
  initialFilters?: Record<string, any>
}

export interface PaginationInfo {
  page: number
  pageSize: number
  total: number
  totalPages: number
}

export interface SortInfo {
  field: string | null
  order: 'asc' | 'desc' | null
}

/**
 * useListPage — shared list-page state for the admin template.
 *
 * Returns reactive state for: search, filters, pagination, sort, selection,
 * loading, error, and exposes helpers (refresh, setFilters, reset).
 *
 * Usage:
 *   const list = useListPage({
 *     pageSize: 20,
 *     initialFilters: { status: 'ACTIVE' }
 *   })
 *
 *   const fetch = async () => {
 *     list.loading = true
 *     try {
 *       const result = await $api('/api/items', { params: { ...list.queryParams } })
 *       list.items = result.content
 *       list.pagination.total = result.totalElements
 *     } finally { list.loading = false }
 *   }
 *
 *   onMounted(fetch)
 */
export function useListPage<T = any> (options: ListPageOptions = {}) {
  const {
    pageSize = 10,
    defaultSort = null,
    defaultOrder = 'asc',
    resetOnFilterChange = true,
    initialFilters = {}
  } = options

  // Data
  const items = ref<T[]>([]) as Ref<T[]>
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Search
  const search = ref('')

  // Filters
  const filters = reactive<Record<string, any>>({ ...initialFilters })

  // Pagination
  const pagination = reactive<PaginationInfo>({
    page: 1,
    pageSize,
    total: 0,
    totalPages: 0
  })

  // Sort
  const sort = reactive<SortInfo>({
    field: defaultSort,
    order: defaultOrder
  })

  // Selection (multi-select)
  const selectedIds = ref<Set<string>>(new Set())

  const selectedItems = computed(() =>
    items.value.filter((it: any) => selectedIds.value.has(it.id))
  )

  const hasSelection = computed(() => selectedIds.value.size > 0)
  const selectionCount = computed(() => selectedIds.value.size)

  // Computed query params (handy for fetch calls)
  const queryParams = computed(() => {
    const params: Record<string, any> = {
      page: pagination.page - 1, // backend usually 0-indexed
      size: pagination.pageSize
    }
    if (search.value) { params.search = search.value }
    Object.entries(filters).forEach(([k, v]) => {
      if (v !== null && v !== undefined && v !== '') { params[k] = v }
    })
    if (sort.field) {
      params.sort = `${sort.field},${sort.order}`
    }
    return params
  })

  function setSearch (val: string) {
    search.value = val
    if (resetOnFilterChange) { pagination.page = 1 }
  }

  function setFilter (key: string, val: any) {
    filters[key] = val
    if (resetOnFilterChange) { pagination.page = 1 }
  }

  function setFilters (vals: Record<string, any>) {
    Object.assign(filters, vals)
    if (resetOnFilterChange) { pagination.page = 1 }
  }

  function resetFilters () {
    Object.keys(filters).forEach((k) => { filters[k] = initialFilters[k] ?? null })
    search.value = ''
    pagination.page = 1
  }

  function setPage (page: number) {
    if (page < 1) { return }
    if (pagination.totalPages && page > pagination.totalPages) { return }
    pagination.page = page
  }

  function setPageSize (size: number) {
    pagination.pageSize = size
    pagination.page = 1
  }

  function setSort (field: string, order?: 'asc' | 'desc') {
    if (sort.field === field) {
      sort.order = sort.order === 'asc' ? 'desc' : 'asc'
    } else {
      sort.field = field
      sort.order = order || 'asc'
    }
  }

  // Selection helpers
  function toggleSelect (id: string) {
    if (selectedIds.value.has(id)) {
      selectedIds.value.delete(id)
    } else {
      selectedIds.value.add(id)
    }
    selectedIds.value = new Set(selectedIds.value)
  }

  function selectAll () {
    items.value.forEach((it: any) => selectedIds.value.add(it.id))
    selectedIds.value = new Set(selectedIds.value)
  }

  function clearSelection () {
    selectedIds.value = new Set()
  }

  function isSelected (id: string): boolean {
    return selectedIds.value.has(id)
  }

  function isAllSelected (): boolean {
    return items.value.length > 0 &&
      items.value.every((it: any) => selectedIds.value.has(it.id))
  }

  function setItems (newItems: T[], total?: number) {
    items.value = newItems
    if (total !== undefined) {
      pagination.total = total
      pagination.totalPages = Math.ceil(total / pagination.pageSize) || 1
    }
    // Clear stale selection
    const validIds = new Set(newItems.map((it: any) => it.id))
    selectedIds.value.forEach((id) => {
      if (!validIds.has(id)) { selectedIds.value.delete(id) }
    })
    selectedIds.value = new Set(selectedIds.value)
  }

  return {
    // state
    items,
    loading,
    error,
    search,
    filters,
    pagination,
    sort,
    selectedIds,
    // computed
    selectedItems,
    hasSelection,
    selectionCount,
    queryParams,
    // pagination
    setPage,
    setPageSize,
    // search/filter
    setSearch,
    setFilter,
    setFilters,
    resetFilters,
    // sort
    setSort,
    // selection
    toggleSelect,
    selectAll,
    clearSelection,
    isSelected,
    isAllSelected,
    // items
    setItems
  }
}

export type ListPage<T = any> = ReturnType<typeof useListPage<T>>

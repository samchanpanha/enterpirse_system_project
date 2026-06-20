import { defineStore } from 'pinia'
import type { Branch } from '~/shared/types/branch'

export const useBranchStore = defineStore('branch', () => {
  const branches = ref<Branch[]>([])
  const currentBranchId = useState<string | null>('branch.currentId', () => null)
  const loading = ref(false)
  const { user, token } = useAuth()

  const currentBranch = computed(() =>
    branches.value.find(b => b.id === currentBranchId.value) || null
  )

  async function fetchBranches () {
    if (!user.value?.tenantId || !token.value) { return }
    loading.value = true
    try {
      branches.value = await $fetch<Branch[]>('/api/branches', {
        headers: { 'X-Tenant-Id': user.value.tenantId, Authorization: `Bearer ${token.value}` }
      })
      // Auto-select default branch if none selected
      if (!currentBranchId.value && branches.value.length > 0) {
        const def = branches.value.find(b => b.default) || branches.value[0]
        currentBranchId.value = def.id
        localStorage.setItem('branch.currentId', def.id)
      }
    } finally {
      loading.value = false
    }
  }

  function setCurrentBranch (branchId: string) {
    currentBranchId.value = branchId
    if (import.meta.client) {
      localStorage.setItem('branch.currentId', branchId)
    }
  }

  function init () {
    if (import.meta.client) {
      const saved = localStorage.getItem('branch.currentId')
      if (saved) { currentBranchId.value = saved }
    }
  }

  // Get a $api wrapper that injects ?branchId=X on every call
  function api (): any {
    const config = useRuntimeConfig()
    return $fetch.create({
      baseURL: config.public.apiBaseUrl,
      onRequest ({ options }: any) {
        if (token.value) {
          options.headers = new Headers(options.headers)
          options.headers.set('Authorization', `Bearer ${token.value}`)
        }
      }
    })
  }

  function $apiWithBranch<T = any> (url: string, opts: any = {}): Promise<T> {
    const fullUrl = `/api${url}`
    if (currentBranchId.value) {
      const sep = fullUrl.includes('?') ? '&' : '?'
      return api()(`${fullUrl}${sep}branchId=${currentBranchId.value}`, opts) as Promise<T>
    }
    return api()(fullUrl, opts) as Promise<T>
  }

  if (import.meta.client) {
    init()
  }

  return {
    branches,
    currentBranchId,
    currentBranch,
    loading,
    fetchBranches,
    setCurrentBranch,
    init,
    $apiWithBranch
  }
})

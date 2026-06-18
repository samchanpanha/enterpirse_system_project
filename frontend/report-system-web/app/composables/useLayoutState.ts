export interface MenuItem {
  path: string
  title: string
  icon?: string
  children?: MenuItem[]
  feature?: string
  meta?: {
    hidden?: boolean
    requireAuth?: boolean
    permission?: string
    feature?: string
  }
}

const STORAGE_KEY = 'layout.sidebar.collapsed'

export const useLayoutState = () => {
  const sidebarCollapsed = useState<boolean>('layout.sidebar.collapsed', () => false)
  const mobileSidebarOpen = useState<boolean>('layout.mobileSidebar.open', () => false)

  function initFromStorage () {
    if (!import.meta.client) { return }
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved !== null) {
      sidebarCollapsed.value = saved === 'true'
    }
  }

  function toggleSidebar () {
    sidebarCollapsed.value = !sidebarCollapsed.value
    if (import.meta.client) {
      localStorage.setItem(STORAGE_KEY, String(sidebarCollapsed.value))
    }
  }

  function setSidebar (collapsed: boolean) {
    sidebarCollapsed.value = collapsed
    if (import.meta.client) {
      localStorage.setItem(STORAGE_KEY, String(collapsed))
    }
  }

  function openMobileSidebar () {
    mobileSidebarOpen.value = true
  }

  function closeMobileSidebar () {
    mobileSidebarOpen.value = false
  }

  return {
    sidebarCollapsed,
    mobileSidebarOpen,
    initFromStorage,
    toggleSidebar,
    setSidebar,
    openMobileSidebar,
    closeMobileSidebar
  }
}

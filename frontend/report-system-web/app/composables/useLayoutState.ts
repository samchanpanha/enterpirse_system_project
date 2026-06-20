export interface MenuItem {
  path: string
  title: string
  icon?: string
  children?: MenuItem[]
  feature?: string
  permission?: string
  meta?: {
    hidden?: boolean
    requireAuth?: boolean
    permission?: string
    feature?: string
  }
}

const STORAGE_KEY = 'layout.sidebar.collapsed'
const DARK_MODE_KEY = 'layout.darkMode'

export const useLayoutState = () => {
  const sidebarCollapsed = useState<boolean>('layout.sidebar.collapsed', () => false)
  const mobileSidebarOpen = useState<boolean>('layout.mobileSidebar.open', () => false)
  const darkMode = useState<boolean>('layout.darkMode', () => false)

  function applyDarkMode (enabled: boolean) {
    if (import.meta.client) {
      document.documentElement.classList.toggle('dark', enabled)
    }
  }

  function initFromStorage () {
    if (!import.meta.client) { return }
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved !== null) {
      sidebarCollapsed.value = saved === 'true'
    }
    const savedDark = localStorage.getItem(DARK_MODE_KEY)
    if (savedDark !== null) {
      darkMode.value = savedDark === 'true'
    }
    applyDarkMode(darkMode.value)
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

  function toggleDarkMode () {
    darkMode.value = !darkMode.value
    if (import.meta.client) {
      localStorage.setItem(DARK_MODE_KEY, String(darkMode.value))
      applyDarkMode(darkMode.value)
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
    darkMode,
    initFromStorage,
    toggleSidebar,
    setSidebar,
    toggleDarkMode,
    openMobileSidebar,
    closeMobileSidebar
  }
}

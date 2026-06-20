<template>
  <header
    class="h-header bg-white border-b border-border flex items-center px-4 sticky top-0 z-30 shadow-sm"
  >
    <div class="flex items-center gap-3 flex-1 min-w-0">
      <button
        type="button"
        class="p-1.5 rounded hover:bg-gray-100 text-text-secondary transition-colors"
        :aria-label="sidebarCollapsed ? 'Expand sidebar' : 'Collapse sidebar'"
        @click="toggleSidebar"
      >
        <Icon
          :icon="sidebarCollapsed ? 'ant-design:menu-unfold-outlined' : 'ant-design:menu-fold-outlined'"
          class="text-lg"
        />
      </button>

      <AdminBreadcrumbs />
    </div>

    <div class="flex items-center gap-2 flex-shrink-0">
      <BranchSelector />

      <div class="h-6 w-px bg-border mx-1" />

      <button
        type="button"
        class="p-1.5 rounded hover:bg-gray-100 text-text-secondary transition-colors"
        :aria-label="darkMode ? 'Switch to light mode' : 'Switch to dark mode'"
        @click="toggleDarkMode"
      >
        <Icon :icon="darkMode ? 'ant-design:sun-outlined' : 'ant-design:moon-outlined'" class="text-lg" />
      </button>

      <div class="relative">
        <button
          type="button"
          class="p-1.5 rounded hover:bg-gray-100 text-text-secondary transition-colors relative"
          aria-label="Notifications"
          @click="notifOpen = !notifOpen"
        >
          <Icon icon="ant-design:bell-outlined" class="text-lg" />
          <span v-if="unreadCount > 0" class="absolute -top-0.5 -right-0.5 w-4 h-4 bg-red-500 text-white text-[10px] font-bold flex items-center justify-center rounded-full">
            {{ unreadCount > 9 ? '9+' : unreadCount }}
          </span>
        </button>

        <div
          v-if="notifOpen"
          class="absolute right-0 mt-1 w-80 bg-white rounded-lg shadow-xl border border-gray-200 z-50 overflow-hidden transition-all duration-200"
          @click.stop
        >
          <div class="p-3 border-b border-gray-100 flex items-center justify-between">
            <div class="text-sm font-semibold text-gray-900">Notifications</div>
            <button class="text-xs text-blue-600 hover:text-blue-800 font-medium" @click="markAllAsRead">
              Mark all as read
            </button>
          </div>
          <div class="max-h-80 overflow-y-auto">
            <div v-if="notifications.length === 0" class="px-4 py-8 text-center text-gray-500">
              <Icon icon="ant-design:bell-outlined" class="mx-auto text-3xl mb-2 opacity-40" />
              <p class="text-sm">No new notifications</p>
            </div>
            <button
              v-for="notif in notifications"
              :key="notif.id"
              class="w-full text-left px-4 py-3 hover:bg-gray-50 border-b border-gray-50 last:border-b-0 transition-colors"
              :class="{ 'bg-blue-50': !notif.read }"
              @click="markAsRead(notif.id)"
            >
              <div class="flex items-start gap-3">
                <div :class="getNotificationIconClass(notif.type)" class="p-2 rounded-full">
                  <Icon :icon="getNotificationIcon(notif.type)" class="text-base" />
                </div>
                <div class="flex-1 min-w-0">
                  <div class="text-sm font-medium text-gray-900 truncate">{{ notif.title }}</div>
                  <div class="text-xs text-gray-500 mt-0.5 line-clamp-2">{{ notif.message }}</div>
                  <div class="text-[10px] text-gray-400 mt-1.5">{{ formatTime(notif.timestamp) }}</div>
                </div>
                <div v-if="!notif.read" class="w-2 h-2 bg-blue-500 rounded-full flex-shrink-0 mt-1.5"></div>
              </div>
            </button>
          </div>
          <div class="p-2 border-t border-gray-100 bg-gray-50">
            <button class="w-full py-2 text-sm text-blue-600 hover:text-blue-800 font-medium text-center">
              View all notifications
            </button>
          </div>
        </div>
      </div>

      <button
        type="button"
        class="p-1.5 rounded hover:bg-gray-100 text-text-secondary transition-colors"
        aria-label="Help"
      >
        <Icon icon="ant-design:question-circle-outlined" class="text-lg" />
      </button>

      <div class="h-6 w-px bg-border mx-1" />

      <div class="relative">
        <button
          type="button"
          class="flex items-center gap-2 px-2 py-1 rounded hover:bg-gray-100 transition-colors"
          :aria-label="user?.email || 'User menu'"
          @click="userMenuOpen = !userMenuOpen"
        >
          <div
            class="w-7 h-7 rounded-full bg-primary text-white flex items-center justify-center text-sm font-medium"
          >
            {{ initials }}
          </div>
          <span class="text-sm text-text-primary max-w-[120px] truncate hidden md:inline">
            {{ user?.email || 'Guest' }}
          </span>
          <Icon
            icon="ant-design:down-outlined"
            class="text-text-disabled text-xs"
          />
        </button>

        <div
          v-if="userMenuOpen"
          class="absolute right-0 mt-1 w-56 bg-white rounded shadow-popover border border-border z-50 py-1"
          @click.stop
        >
          <div class="px-3 py-2 border-b border-border">
            <div class="text-sm font-medium text-text-primary truncate">
              {{ user?.email || 'Guest' }}
            </div>
            <div class="text-xs text-text-secondary mt-0.5">
              {{ tenantLabel }}
            </div>
          </div>
          <button
            v-for="item in userMenuItems"
            :key="item.key"
            class="w-full text-left px-3 py-2 text-sm hover:bg-gray-50 flex items-center gap-2 text-text-primary"
            @click="handleUserMenuClick(item.key)"
          >
            <Icon :icon="item.icon" class="text-base text-text-secondary" />
            {{ item.label }}
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
const { user, logout } = useAuth()
const { sidebarCollapsed, toggleSidebar, darkMode, toggleDarkMode } = useLayoutState()

const userMenuOpen = ref(false)
const notifOpen = ref(false)

// Notification interface and sample data (would be fetched from API in production)
interface Notification {
  id: string
  title: string
  message: string
  type: 'success' | 'warning' | 'error' | 'info'
  read: boolean
  timestamp: Date
}

const notifications = ref<Notification[]>([
  {
    id: '1',
    title: 'New order received',
    message: 'Order #12345 has been placed for Main Branch',
    type: 'info',
    read: false,
    timestamp: new Date(Date.now() - 5 * 60000)
  },
  {
    id: '2',
    title: 'Inventory low alert',
    message: 'Stock level for Product X is below threshold',
    type: 'warning',
    read: false,
    timestamp: new Date(Date.now() - 30 * 60000)
  },
  {
    id: '3',
    title: 'Payment successful',
    message: 'Payment of $1,250.00 has been processed',
    type: 'success',
    read: false,
    timestamp: new Date(Date.now() - 2 * 3600000)
  },
  {
    id: '4',
    title: 'System update completed',
    message: 'All services are running normally after maintenance',
    type: 'success',
    read: true,
    timestamp: new Date(Date.now() - 24 * 3600000)
  }
])

// Computed unread notification count
const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

// Notification icon utilities
function getNotificationIcon(type: string): string {
  const icons: Record<string, string> = {
    success: 'ant-design:check-circle-outlined',
    warning: 'ant-design:warning-outlined',
    error: 'ant-design:close-circle-outlined',
    info: 'ant-design:info-circle-outlined'
  }
  return icons[type] || icons.info
}

function getNotificationIconClass(type: string): string {
  const classes: Record<string, string> = {
    success: 'bg-green-100 text-green-600',
    warning: 'bg-yellow-100 text-yellow-600',
    error: 'bg-red-100 text-red-600',
    info: 'bg-blue-100 text-blue-600'
  }
  return classes[type] || classes.info
}

// Format timestamp to relative time
function formatTime(timestamp: Date): string {
  const seconds = Math.floor((new Date().getTime() - timestamp.getTime()) / 1000)
  if (seconds < 60) return `${seconds}s ago`
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.floor(hours / 24)
  return `${days}d ago`
}

// Mark single notification as read
function markAsRead(id: string) {
  const notif = notifications.value.find(n => n.id === id)
  if (notif) notif.read = true
}

// Mark all notifications as read
function markAllAsRead () {
  notifications.value.forEach(n => { n.read = true })
}

const initials = computed(() => {
  const email = user.value?.email || 'G'
  return email.substring(0, 2).toUpperCase()
})

const tenantLabel = computed(() => {
  if (user.value?.tenantName) { return user.value.tenantName }
  if (user.value?.tenantId) { return `Tenant: ${user.value.tenantId.substring(0, 8)}...` }
  return 'No tenant'
})

const userMenuItems = [
  { key: 'profile', label: 'Profile', icon: 'ant-design:user-outlined' },
  { key: 'settings', label: 'Settings', icon: 'ant-design:setting-outlined' },
  { key: 'logout', label: 'Logout', icon: 'ant-design:logout-outlined' }
]

async function handleUserMenuClick (key: string) {
  userMenuOpen.value = false
  if (key === 'logout') {
    await logout()
    await navigateTo('/login')
  } else if (key === 'profile') {
    await navigateTo('/app/admin/profile')
  } else if (key === 'settings') {
    await navigateTo('/app/admin/settings')
  }
}

if (import.meta.client) {
  const listener = () => {
    userMenuOpen.value = false
    notifOpen.value = false
  }
  onMounted(() => document.addEventListener('click', listener))
  onUnmounted(() => document.removeEventListener('click', listener))
}
</script>
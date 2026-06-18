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
        aria-label="Notifications"
        @click="notifOpen = !notifOpen"
      >
        <Icon icon="ant-design:bell-outlined" class="text-lg" />
      </button>

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
const { sidebarCollapsed, toggleSidebar } = useLayoutState()

const userMenuOpen = ref(false)
const notifOpen = ref(false)

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

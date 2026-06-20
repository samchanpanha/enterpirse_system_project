<template>
  <div class="p-6 max-w-2xl">
    <AdminPageHeader title="Settings" subtitle="Appearance and preferences" />

    <div class="bg-white rounded shadow-card divide-y divide-border">
      <div class="p-6 flex items-center justify-between gap-4">
        <div>
          <h3 class="text-sm font-medium text-text-primary">
            Dark mode
          </h3>
          <p class="text-xs text-text-secondary mt-0.5">
            Reduce eye strain in low-light environments
          </p>
        </div>
        <button
          type="button"
          :class="[
            'relative inline-flex h-6 w-11 flex-shrink-0 rounded-full transition-colors',
            darkMode ? 'bg-primary' : 'bg-gray-200'
          ]"
          role="switch"
          :aria-checked="darkMode"
          @click="toggleDarkMode()"
        >
          <span
            :class="[
              'inline-block h-5 w-5 transform rounded-full bg-white shadow transition-transform mt-0.5',
              darkMode ? 'translate-x-5' : 'translate-x-0.5'
            ]"
          />
        </button>
      </div>

      <div class="p-6 flex items-center justify-between gap-4">
        <div>
          <h3 class="text-sm font-medium text-text-primary">
            Compact sidebar
          </h3>
          <p class="text-xs text-text-secondary mt-0.5">
            Collapse navigation to icons only
          </p>
        </div>
        <button
          type="button"
          :class="[
            'relative inline-flex h-6 w-11 flex-shrink-0 rounded-full transition-colors',
            sidebarCollapsed ? 'bg-primary' : 'bg-gray-200'
          ]"
          role="switch"
          :aria-checked="sidebarCollapsed"
          @click="toggleSidebar()"
        >
          <span
            :class="[
              'inline-block h-5 w-5 transform rounded-full bg-white shadow transition-transform mt-0.5',
              sidebarCollapsed ? 'translate-x-5' : 'translate-x-0.5'
            ]"
          />
        </button>
      </div>

      <div class="p-6">
        <h3 class="text-sm font-medium text-text-primary mb-2">
          Session
        </h3>
        <button
          type="button"
          class="inline-flex items-center gap-2 px-3 py-1.5 text-sm border border-danger text-danger rounded hover:bg-red-50 transition-colors"
          @click="handleLogout"
        >
          <Icon icon="ant-design:logout-outlined" />
          Sign out
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const { logout } = useAuth()
const { darkMode, toggleDarkMode, sidebarCollapsed, toggleSidebar } = useLayoutState()
const toast = useToast()

async function handleLogout () {
  await logout()
  toast.info('Signed out', 'See you next time')
  await navigateTo('/login')
}
</script>

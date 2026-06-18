<template>
  <aside
    :class="[
      'flex flex-col bg-sidebar text-sidebar-text border-r border-sidebar-border transition-all duration-200 ease-in-out h-full',
      sidebarCollapsed ? 'w-sidebarCollapsed' : 'w-sidebar'
    ]"
  >
    <div
      :class="[
        'flex items-center h-header border-b border-sidebar-border px-4 flex-shrink-0',
        sidebarCollapsed ? 'justify-center' : 'gap-3'
      ]"
    >
      <div
        class="w-8 h-8 rounded bg-primary flex items-center justify-center flex-shrink-0"
      >
        <Icon icon="ant-design:line-chart-outlined" class="text-white text-lg" />
      </div>
      <span
        v-if="!sidebarCollapsed"
        class="text-white font-semibold text-lg whitespace-nowrap overflow-hidden"
      >
        Report System
      </span>
    </div>

    <nav class="flex-1 overflow-y-auto py-2">
      <ul>
        <template v-for="item in visibleMenu" :key="item.path">
          <li v-if="!item.children">
            <NuxtLink
              :to="item.path"
              :class="[
                'flex items-center h-10 mx-2 my-0.5 rounded transition-colors',
                sidebarCollapsed ? 'justify-center px-0' : 'gap-3 px-3',
                isActive(item.path)
                  ? 'bg-primary text-white'
                  : 'text-sidebar-text hover:bg-sidebar-hover hover:text-sidebar-textHover'
              ]"
              :title="sidebarCollapsed ? item.title : undefined"
            >
              <Icon
                v-if="item.icon"
                :icon="item.icon"
                class="text-lg flex-shrink-0"
              />
              <span v-if="!sidebarCollapsed" class="truncate">
                {{ item.title }}
              </span>
            </NuxtLink>
          </li>

          <li v-else>
            <button
              type="button"
              :class="[
                'w-full flex items-center h-10 mx-2 my-0.5 rounded transition-colors',
                sidebarCollapsed ? 'justify-center px-0' : 'justify-between px-3',
                isModuleActive(item)
                  ? 'bg-sidebar-hover text-sidebar-textHover'
                  : 'text-sidebar-text hover:bg-sidebar-hover hover:text-sidebar-textHover'
              ]"
              :title="sidebarCollapsed ? item.title : undefined"
              @click="toggleSubmenu(item.path)"
            >
              <span :class="['flex items-center gap-3 min-w-0', sidebarCollapsed ? '' : '']">
                <Icon
                  v-if="item.icon"
                  :icon="item.icon"
                  class="text-lg flex-shrink-0"
                />
                <span v-if="!sidebarCollapsed" class="truncate">
                  {{ item.title }}
                </span>
              </span>
              <Icon
                v-if="!sidebarCollapsed && item.children"
                :icon="isSubmenuOpen(item.path) ? 'ant-design:down-outlined' : 'ant-design:right-outlined'"
                class="text-xs"
              />
            </button>

            <ul
              v-if="item.children && isSubmenuOpen(item.path) && !sidebarCollapsed"
              class="bg-black/20"
            >
              <li v-for="child in item.children" :key="child.path">
                <NuxtLink
                  :to="child.path"
                  :class="[
                    'flex items-center h-9 pl-11 pr-3 mx-2 my-0.5 rounded text-sm transition-colors',
                    isActive(child.path)
                      ? 'bg-primary text-white'
                      : 'text-sidebar-text hover:bg-sidebar-hover hover:text-sidebar-textHover'
                  ]"
                >
                  <span class="truncate">
                    {{ child.title }}
                  </span>
                </NuxtLink>
              </li>
            </ul>
          </li>
        </template>
      </ul>
    </nav>

    <div
      v-if="!sidebarCollapsed"
      class="px-4 py-3 border-t border-sidebar-border text-xs text-sidebar-text/60 flex-shrink-0"
    >
      <div>v0.1.0 · Sprint 13</div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import type { MenuItem } from '~/composables/useLayoutState'

const { sidebarCollapsed } = useLayoutState()
const { menu } = useMenuConfig()
const route = useRoute()
const { has: hasFeature } = useFeature()

const openSubmenus = useState<Set<string>>('layout.sidebar.open-submenus', () => new Set())

const visibleMenu = computed<MenuItem[]>(() => {
  return menu
    .filter(m => !m.feature || hasFeature(m.feature))
    .map(m => ({
      ...m,
      children: m.children?.filter(c => !c.feature || hasFeature(c.feature))
    }))
})

function isActive (path: string): boolean {
  if (path === '/app/dashboard') {
    return route.path === path
  }
  return route.path === path || route.path.startsWith(path + '/')
}

function isModuleActive (item: MenuItem): boolean {
  if (isActive(item.path)) { return true }
  return !!item.children?.some(c => isActive(c.path))
}

function isSubmenuOpen (path: string): boolean {
  // Auto-open if a child is active
  const item = visibleMenu.value.find(m => m.path === path)
  if (item && isModuleActive(item)) { return true }
  return openSubmenus.value.has(path)
}

function toggleSubmenu (path: string) {
  if (openSubmenus.value.has(path)) {
    openSubmenus.value.delete(path)
  } else {
    openSubmenus.value.add(path)
  }
  // Trigger reactivity
  openSubmenus.value = new Set(openSubmenus.value)
}

// Auto-open the submenu containing the current route on mount
onMounted(() => {
  const activeModule = visibleMenu.value.find(m => isModuleActive(m))
  if (activeModule?.children) {
    openSubmenus.value.add(activeModule.path)
  }
})
</script>

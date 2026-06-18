<template>
  <nav class="flex items-center text-sm" aria-label="Breadcrumb">
    <ol class="flex items-center gap-1.5">
      <li
        v-for="(crumb, idx) in crumbs"
        :key="idx"
        class="flex items-center gap-1.5"
      >
        <NuxtLink
          v-if="idx < crumbs.length - 1"
          :to="crumb.path"
          class="text-text-secondary hover:text-primary transition-colors"
        >
          {{ crumb.title }}
        </NuxtLink>
        <span
          v-else
          class="text-text-primary font-medium"
          :title="crumb.title"
        >
          {{ crumb.title }}
        </span>
        <Icon
          v-if="idx < crumbs.length - 1"
          icon="ant-design:caret-right-outlined"
          class="text-text-disabled text-xs"
        />
      </li>
    </ol>
  </nav>
</template>

<script setup lang="ts">
interface Crumb {
  title: string
  path: string
}

const route = useRoute()
const { menu } = useMenuConfig()

const crumbs = computed<Crumb[]>(() => {
  const result: Crumb[] = [{ title: 'Home', path: '/app/dashboard' }]
  const path = route.path

  // Find which top-level module we're in
  const moduleEntry = menu.find(m =>
    path === m.path ||
    path.startsWith(m.path + '/') ||
    m.children?.some(c => path === c.path)
  )

  if (moduleEntry && path !== '/app/dashboard') {
    result.push({ title: moduleEntry.title, path: moduleEntry.path })

    // Sub-page within module
    if (path !== moduleEntry.path) {
      const child = moduleEntry.children?.find(c => path === c.path || path.startsWith(c.path + '/'))
      if (child && path !== child.path) {
        result.push({ title: child.title, path: child.path })
      }
      // Try to extract a useful label from the route path's last segment
      const lastSegment = path.split('/').pop()
      if (lastSegment && !child && !path.includes('dashboard')) {
        const label = lastSegment
          .replace(/-/g, ' ')
          .replace(/\b\w/g, c => c.toUpperCase())
        result.push({ title: label, path })
      } else if (child && path === child.path) {
        result.push({ title: child.title, path: child.path })
      }
    }
  }

  return result
})
</script>

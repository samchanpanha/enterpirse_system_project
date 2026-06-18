<template>
  <div class="flex h-screen bg-body overflow-hidden">
    <AdminSidebar class="flex-shrink-0" />

    <div class="flex-1 flex flex-col min-w-0">
      <AdminHeader class="flex-shrink-0" />

      <main class="flex-1 overflow-y-auto">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
const { initFromStorage } = useLayoutState()

onMounted(() => {
  initFromStorage()
})
</script>

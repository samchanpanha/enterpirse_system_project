<template>
  <div class="relative">
    <button
      type="button"
      class="flex items-center gap-2 px-2.5 py-1.5 text-sm text-text-primary hover:bg-gray-100 rounded transition-colors"
      :disabled="loading || branches.length === 0"
      @click="open = !open"
    >
      <Icon icon="ant-design:bank-outlined" class="text-base text-text-secondary" />
      <span class="font-medium max-w-[140px] truncate">
        {{ currentBranch?.name || 'Select branch' }}
      </span>
      <span
        v-if="currentBranch"
        class="text-xs text-text-disabled hidden lg:inline"
      >
        ({{ currentBranch.code }})
      </span>
      <Icon icon="ant-design:down-outlined" class="text-xs text-text-disabled" />
    </button>

    <div
      v-if="open"
      class="absolute right-0 mt-1 w-72 bg-white rounded shadow-popover border border-border z-50"
      @click.stop
    >
      <div class="p-2">
        <div class="text-xs text-text-disabled px-2 py-1.5 uppercase tracking-wide">
          Switch branch
        </div>
        <button
          v-for="b in branches"
          :key="b.id"
          class="w-full text-left px-2 py-2 rounded hover:bg-primary-light flex items-center justify-between transition-colors"
          :class="{ 'bg-primary-light text-primary': b.id === currentBranchId }"
          @click="select(b.id)"
        >
          <div class="min-w-0 flex-1">
            <div class="font-medium text-sm truncate">
              {{ b.name }}
            </div>
            <div class="text-xs text-text-secondary truncate">
              {{ b.code }} · {{ b.branchType }}{{ b.city ? ' · ' + b.city : '' }}
            </div>
          </div>
          <span
            v-if="b.default"
            class="text-xs text-primary font-semibold ml-2 flex-shrink-0"
          >
            DEFAULT
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const store = useBranchStore()
const { branches, currentBranch, currentBranchId, loading } = storeToRefs(store)
const open = ref(false)

function select (id: string) {
  store.setCurrentBranch(id)
  open.value = false
  reloadNuxtApp({ force: false, path: useRoute().fullPath, ttl: 100 })
}

onMounted(async () => {
  if (branches.value.length === 0) {
    await store.fetchBranches()
  }
})

if (import.meta.client) {
  const listener = (e: MouseEvent) => {
    const target = e.target as HTMLElement
    if (!target.closest('.relative')) { open.value = false }
  }
  onMounted(() => document.addEventListener('click', listener))
  onUnmounted(() => document.removeEventListener('click', listener))
}
</script>

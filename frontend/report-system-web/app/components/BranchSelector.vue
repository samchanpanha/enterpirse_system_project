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
      class="absolute right-0 mt-1 w-80 bg-white rounded-lg shadow-xl border border-gray-200 z-50 overflow-hidden transition-all duration-200"
      @click.stop
    >
      <div class="p-3 border-b border-gray-100">
        <div class="text-xs text-gray-500 px-1 py-1 uppercase tracking-wide font-semibold">
          Switch branch
        </div>
        <!-- Search input for branches -->
        <div class="mt-2 relative">
          <Icon icon="ant-design:search-outlined" class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search branches by name, code, or city..."
            class="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-md focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            @click.stop
          />
        </div>
      </div>
      <div class="max-h-64 overflow-y-auto p-2">
        <div v-if="filteredBranches.length === 0" class="px-3 py-4 text-center text-sm text-gray-500">
          <Icon icon="ant-design:search-outlined" class="mx-auto text-2xl mb-2 opacity-50" />
          No branches match your search
        </div>
        <button
          v-for="b in filteredBranches"
          :key="b.id"
          class="w-full text-left px-3 py-2.5 rounded-md hover:bg-blue-50 flex items-center justify-between transition-colors mb-1"
          :class="{ 'bg-blue-100 text-blue-700 ring-1 ring-blue-200': b.id === currentBranchId }"
          @click="select(b.id)"
        >
          <div class="min-w-0 flex-1">
            <div class="font-medium text-sm truncate flex items-center gap-2">
              {{ b.name }}
              <span v-if="b.id === currentBranchId" class="text-xs bg-blue-500 text-white px-1.5 py-0.5 rounded-full">Active</span>
            </div>
            <div class="text-xs text-gray-500 truncate mt-1">
              {{ b.code }} · {{ b.branchType }}{{ b.city ? ' · ' + b.city : '' }}
            </div>
          </div>
          <span
            v-if="b.default"
            class="text-xs text-green-600 font-semibold ml-2 flex-shrink-0 bg-green-50 px-2 py-1 rounded-full"
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
const searchQuery = ref('')

// Filter branches based on search query
const filteredBranches = computed(() => {
  if (!searchQuery.value) return branches.value
  const query = searchQuery.value.toLowerCase().trim()
  return branches.value.filter(b => 
    b.name.toLowerCase().includes(query) ||
    b.code.toLowerCase().includes(query) ||
    (b.city && b.city.toLowerCase().includes(query)) ||
    b.branchType.toLowerCase().includes(query)
  )
})

function select (id: string) {
  store.setCurrentBranch(id)
  open.value = false
  searchQuery.value = '' // Reset search when branch is selected
  reloadNuxtApp({ force: false, path: useRoute().fullPath, ttl: 100 })
}

// Reset search when dropdown is closed
watch(open, (newVal) => {
  if (!newVal) {
    searchQuery.value = ''
  }
})

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
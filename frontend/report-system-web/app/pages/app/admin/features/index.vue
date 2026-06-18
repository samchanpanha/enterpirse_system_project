<template>
  <div>
    <AdminPageHeader
      title="Features"
      :subtitle="`${featureCount} features in catalog · ${enabledCount} enabled for your tenant`"
    >
      <template #actions>
        <button
          class="inline-flex items-center gap-1.5 px-3 py-1.5 border border-border text-text-primary text-sm rounded hover:bg-gray-50 transition-colors"
          @click="load"
        >
          <Icon icon="ant-design:reload-outlined" :class="list.loading.value ? 'animate-spin' : ''" />
          Refresh
        </button>
      </template>
    </AdminPageHeader>

    <div v-if="list.loading.value" class="p-6 text-center text-text-secondary">
      <Icon icon="ant-design:loading-outlined" class="animate-spin text-2xl" />
    </div>

    <div v-else class="p-6 space-y-4">
      <div
        v-for="moduleNode in featureStore.tree"
        :key="moduleNode.module"
        class="bg-white rounded shadow-card p-6"
      >
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-2">
            <Icon :icon="moduleIcon(moduleNode.module)" class="text-xl text-primary" />
            <h2 class="text-base font-semibold text-text-primary capitalize">
              {{ moduleNode.module }}
            </h2>
            <span class="text-xs text-text-secondary">
              ({{ enabledInModule(moduleNode.features) }} / {{ moduleNode.features.length }})
            </span>
          </div>
          <button
            class="text-sm text-text-secondary hover:text-primary transition-colors"
            @click="toggleModuleExpansion(moduleNode.module)"
          >
            <Icon :icon="isModuleExpanded(moduleNode.module) ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
          </button>
        </div>

        <div v-if="isModuleExpanded(moduleNode.module)" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
          <div
            v-for="f in moduleNode.features"
            :key="f.code"
            :class="[
              'border rounded p-3 transition-colors',
              f.enabled ? 'border-primary bg-primary-light/30' : 'border-border bg-gray-50'
            ]"
          >
            <div class="flex items-start justify-between mb-1">
              <p class="text-sm font-medium text-text-primary">
                {{ f.name }}
              </p>
              <Icon
                :icon="f.enabled ? 'ant-design:check-circle-filled' : 'ant-design:minus-circle-outlined'"
                :class="f.enabled ? 'text-success' : 'text-text-disabled'"
              />
            </div>
            <p v-if="f.description" class="text-xs text-text-secondary mb-2 line-clamp-2">
              {{ f.description }}
            </p>
            <div class="flex items-center justify-between">
              <span :class="['text-xs px-1.5 py-0.5 rounded', tierClass(f.tierRequired)]">
                {{ f.tierRequired }}
              </span>
              <span class="text-xs font-mono text-text-disabled">
                {{ f.code }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useFeatureStore } from '~/stores/feature'

definePageMeta({ middleware: 'auth' })

const featureStore = useFeatureStore()
const { user } = useAuth()
const list = useListPage({ pageSize: 100 })
const expandedModules = ref<Set<string>>(new Set())

const featureCount = computed(() => featureStore.tree.reduce((s, m) => s + m.features.length, 0))
const enabledCount = computed(() => featureStore.tree.reduce((s, m) => s + m.features.filter((f: any) => f.enabled).length, 0))

function isModuleExpanded (mod: string): boolean {
  return expandedModules.value.has(mod)
}

function toggleModuleExpansion (mod: string) {
  if (expandedModules.value.has(mod)) {
    expandedModules.value.delete(mod)
  } else {
    expandedModules.value.add(mod)
  }
  expandedModules.value = new Set(expandedModules.value)
}

function enabledInModule (features: any[]): number {
  return features.filter(f => f.enabled).length
}

function moduleIcon (mod: string): string {
  switch (mod) {
    case 'inventory': return 'ant-design:database-outlined'
    case 'restaurant': return 'ant-design:shop-outlined'
    case 'property': return 'ant-design:home-outlined'
    case 'finance': return 'ant-design:account-book-outlined'
    case 'payment': return 'ant-design:credit-card-outlined'
    case 'reporting': return 'ant-design:bar-chart-outlined'
    case 'platform': return 'ant-design:setting-outlined'
    default: return 'ant-design:appstore-outlined'
  }
}

function tierClass (tier: string): string {
  switch (tier) {
    case 'BASIC': return 'bg-info-light text-info'
    case 'PRO': return 'bg-primary-light text-primary'
    case 'ENTERPRISE': return 'bg-warning-light text-warning'
    default: return 'bg-gray-100 text-text-secondary'
  }
}

async function load () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    await featureStore.fetchFeatures()
    // Fetch the tree
    const config = useRuntimeConfig()
    const headers: Record<string, string> = { 'X-Tenant-Id': user.value.tenantId }
    const tree: any = await $fetch(`${config.public.apiBaseUrl}/api/features/tree`, { headers })
    if (Array.isArray(tree)) {
      featureStore.setTree(tree)
      // Expand all modules by default
      expandedModules.value = new Set(tree.map((m: any) => m.module))
    } else {
      // Fallback: build a tree from the enabled feature list
      const codes = Array.from(featureStore.enabled)
      featureStore.setTree([
        { module: 'system', features: codes.map(code => ({ code, name: code, tierRequired: 'BASIC', enabled: true })) }
      ])
    }
  } catch (e) {
    // Fallback for dev: show a sample tree
    featureStore.setTree([
      { module: 'system', features: Array.from(featureStore.enabled).map(code => ({ code, name: code, tierRequired: 'BASIC', enabled: true })) }
    ])
    expandedModules.value = new Set(['system'])
  } finally {
    list.loading.value = false
  }
}

onMounted(load)
</script>

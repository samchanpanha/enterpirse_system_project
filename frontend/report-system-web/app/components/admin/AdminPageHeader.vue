<template>
  <div class="bg-white border-b border-border px-6 py-4 flex items-center justify-between gap-4 flex-wrap">
    <div class="min-w-0">
      <h1 class="text-xl font-semibold text-text-primary m-0 truncate">
        {{ title }}
      </h1>
      <p v-if="subtitle" class="text-sm text-text-secondary mt-1 mb-0">
        {{ subtitle }}
      </p>
    </div>
    <div class="flex items-center gap-2 flex-shrink-0">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string
  subtitle?: string
}

defineProps<Props>()
</script>

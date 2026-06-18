<template>
  <div class="text-center py-12 px-6">
    <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-primary-light mb-3">
      <Icon :icon="icon" class="text-2xl text-primary" />
    </div>
    <h3 class="text-base font-medium text-text-primary mb-1">
      {{ title }}
    </h3>
    <p v-if="description" class="text-sm text-text-secondary max-w-md mx-auto mb-4">
      {{ description }}
    </p>
    <slot />
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  icon?: string
  title: string
  description?: string
}>(), {
  icon: 'ant-design:inbox-outlined'
})
</script>

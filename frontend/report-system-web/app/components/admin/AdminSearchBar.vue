<template>
  <div class="bg-white border-b border-border px-6 py-3">
    <div class="flex items-center gap-2 flex-wrap">
      <div class="relative flex-1 min-w-[200px] max-w-md">
        <Icon
          icon="ant-design:search-outlined"
          class="absolute left-3 top-1/2 -translate-y-1/2 text-text-disabled"
        />
        <input
          :value="modelValue"
          type="text"
          :placeholder="placeholder"
          class="w-full pl-9 pr-3 py-1.5 text-sm border border-border rounded text-text-primary placeholder:text-text-disabled focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/30 transition-colors"
          @input="onInput"
        >
      </div>

      <slot name="filters" />

      <button
        v-if="hasActiveFilters"
        type="button"
        class="text-sm text-text-secondary hover:text-primary flex items-center gap-1 px-2 py-1.5 transition-colors"
        @click="emit('reset')"
      >
        <Icon icon="ant-design:close-circle-outlined" />
        Reset
      </button>

      <div class="flex-1" />

      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  modelValue: string
  placeholder?: string
  hasActiveFilters?: boolean
}

withDefaults(defineProps<Props>(), {
  placeholder: 'Search…',
  hasActiveFilters: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  reset: []
}>()

function onInput (e: Event) {
  emit('update:modelValue', (e.target as HTMLInputElement).value)
}
</script>

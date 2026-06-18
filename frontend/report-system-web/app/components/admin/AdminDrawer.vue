<template>
  <Teleport to="body">
    <Transition name="drawer">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-50 flex justify-end"
        @keydown.esc="onEsc"
      >
        <div
          class="absolute inset-0 bg-black/40"
          @click="emit('update:modelValue', false)"
        />
        <div
          :class="[
            'relative bg-white h-full flex flex-col shadow-drawer',
            widthClass
          ]"
        >
          <div class="px-5 py-4 border-b border-border flex items-center justify-between flex-shrink-0">
            <div class="min-w-0">
              <h2 class="text-base font-semibold text-text-primary m-0 truncate">
                {{ title }}
              </h2>
              <p v-if="subtitle" class="text-xs text-text-secondary mt-0.5 mb-0 truncate">
                {{ subtitle }}
              </p>
            </div>
            <button
              type="button"
              class="p-1 rounded hover:bg-gray-100 text-text-secondary transition-colors flex-shrink-0 ml-2"
              aria-label="Close"
              @click="emit('update:modelValue', false)"
            >
              <Icon icon="ant-design:close-outlined" class="text-lg" />
            </button>
          </div>

          <div class="flex-1 overflow-y-auto px-5 py-4">
            <slot />
          </div>

          <div
            v-if="$slots.footer"
            class="px-5 py-3 border-t border-border bg-gray-50 flex items-center justify-end gap-2 flex-shrink-0"
          >
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
interface Props {
  modelValue: boolean
  title: string
  subtitle?: string
  width?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | 'full'
  closeOnEsc?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  width: 'md',
  closeOnEsc: true
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const widthClass = computed(() => {
  switch (props.width) {
    case 'sm': return 'w-[360px]'
    case 'md': return 'w-[480px]'
    case 'lg': return 'w-[640px]'
    case 'xl': return 'w-[800px]'
    case '2xl': return 'w-[1024px]'
    case 'full': return 'w-full'
    default: return 'w-[480px]'
  }
})

function onEsc () {
  if (props.closeOnEsc) {
    emit('update:modelValue', false)
  }
}
</script>

<style scoped>
.drawer-enter-active,
.drawer-leave-active {
  transition: opacity 0.2s ease;
}
.drawer-enter-active > .relative,
.drawer-leave-active > .relative {
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
}
.drawer-enter-from > .relative,
.drawer-leave-to > .relative {
  transform: translateX(100%);
}
</style>

<template>
  <div
    class="fixed top-4 right-4 z-[100] flex flex-col gap-2 w-full max-w-sm pointer-events-none"
    aria-live="polite"
    aria-atomic="true"
  >
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="[
          'pointer-events-auto flex items-start gap-3 p-4 rounded-lg shadow-lg border',
          typeClasses[toast.type]
        ]"
        role="alert"
      >
        <Icon :icon="typeIcons[toast.type]" class="text-lg flex-shrink-0 mt-0.5" />
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium">
            {{ toast.title }}
          </p>
          <p v-if="toast.message" class="text-xs mt-0.5 opacity-90">
            {{ toast.message }}
          </p>
        </div>
        <button
          type="button"
          class="p-0.5 rounded hover:bg-black/5 flex-shrink-0"
          aria-label="Dismiss"
          @click="dismiss(toast.id)"
        >
          <Icon icon="ant-design:close-outlined" class="text-sm" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup lang="ts">
import type { ToastType } from '~/composables/useToast'

const { toasts, dismiss } = useToast()

const typeIcons: Record<ToastType, string> = {
  success: 'ant-design:check-circle-outlined',
  error: 'ant-design:close-circle-outlined',
  warning: 'ant-design:warning-outlined',
  info: 'ant-design:info-circle-outlined'
}

const typeClasses: Record<ToastType, string> = {
  success: 'bg-green-50 border-green-200 text-green-900',
  error: 'bg-red-50 border-red-200 text-red-900',
  warning: 'bg-amber-50 border-amber-200 text-amber-900',
  info: 'bg-blue-50 border-blue-200 text-blue-900'
}
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.25s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(1rem);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(1rem);
}
</style>

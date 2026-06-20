export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface Toast {
  id: string
  type: ToastType
  title: string
  message?: string
  duration: number
}

export const useToast = () => {
  const toasts = useState<Toast[]>('app.toasts', () => [])

  function push (type: ToastType, title: string, message?: string, duration = 4000) {
    const id = `${Date.now()}-${Math.random().toString(36).slice(2, 7)}`
    toasts.value = [...toasts.value, { id, type, title, message, duration }]
    if (duration > 0 && import.meta.client) {
      setTimeout(() => dismiss(id), duration)
    }
    return id
  }

  function dismiss (id: string) {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }

  function clear () {
    toasts.value = []
  }

  return {
    toasts: readonly(toasts),
    success: (title: string, message?: string) => push('success', title, message),
    error: (title: string, message?: string) => push('error', title, message, 6000),
    warning: (title: string, message?: string) => push('warning', title, message),
    info: (title: string, message?: string) => push('info', title, message),
    dismiss,
    clear
  }
}

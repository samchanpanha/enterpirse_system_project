import { ref } from 'vue'

/**
 * useDrawer — shared state for a right-side drawer (create/edit modal).
 *
 * Each drawer usage creates an independent state. The drawer is opened
 * with `openFor(item?)` and closed with `close()`.
 */
export function useDrawer<T = any> () {
  const open = ref(false)
  const editing = ref<T | null>(null)
  const mode = ref<'create' | 'edit' | 'view'>('create')
  const error = ref<string | null>(null)
  const saving = ref(false)
  const formData = ref<Record<string, any>>({})

  function openFor (item?: T | null) {
    if (item) {
      editing.value = item
      mode.value = 'edit'
      formData.value = { ...(item as any) }
    } else {
      editing.value = null
      mode.value = 'create'
      formData.value = {}
    }
    error.value = null
    open.value = true
  }

  function openView (item: T) {
    editing.value = item
    mode.value = 'view'
    formData.value = { ...(item as any) }
    error.value = null
    open.value = true
  }

  function close () {
    open.value = false
    setTimeout(() => {
      editing.value = null
      mode.value = 'create'
      formData.value = {}
      error.value = null
      saving.value = false
    }, 200) // wait for slide-out animation
  }

  const title = () => {
    if (mode.value === 'view') { return 'View' }
    if (mode.value === 'edit') { return 'Edit' }
    return 'Create'
  }

  const isEdit = () => mode.value === 'edit'
  const isCreate = () => mode.value === 'create'
  const isView = () => mode.value === 'view'

  return {
    // state
    open,
    editing,
    mode,
    error,
    saving,
    formData,
    // methods
    openFor,
    openView,
    close,
    title,
    isEdit,
    isCreate,
    isView
  }
}

type Api = {
  <T = any>(url: string, options?: any): Promise<T>
  raw: typeof $fetch.raw
  create: typeof $fetch.create
}

export default defineNuxtPlugin(() => {
  const { token } = useAuth()

  const api: Api = $fetch.create({
    baseURL: '/api',
    onRequest ({ options }) {
      if (token.value) {
        options.headers = new Headers(options.headers)
        options.headers.set('Authorization', `Bearer ${token.value}`)
      }
    },
    onResponseError ({ response, options }) {
      if (response.status === 401) {
        const { logout } = useAuth()
        logout()
        navigateTo('/login')
        return
      }
      const method = (options.method || 'GET').toUpperCase()
      const showToast = method !== 'GET' && method !== 'HEAD'
      if (import.meta.client && showToast && response.status >= 400) {
        const toast = useToast()
        const data = response._data as { message?: string; error?: string } | undefined
        const detail = data?.message || data?.error || `Request failed (${response.status})`
        toast.error('Action failed', detail)
      }
    }
  }) as Api

  return {
    provide: {
      api
    }
  }
})

declare module '#app' {
  interface NuxtApp {
    $api: Api
  }
}

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $api: Api
  }
}

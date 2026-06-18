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
    onResponseError ({ response }) {
      if (response.status === 401) {
        const { logout } = useAuth()
        logout()
        navigateTo('/login')
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

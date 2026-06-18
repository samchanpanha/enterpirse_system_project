<template>
  <div class="min-h-full flex items-center justify-center p-6 bg-body">
    <div class="w-full max-w-md bg-white rounded shadow-card p-8">
      <div class="text-center mb-6">
        <div class="inline-flex items-center justify-center w-12 h-12 bg-primary-light rounded-full mb-3">
          <Icon icon="ant-design:line-chart-outlined" class="text-2xl text-primary" />
        </div>
        <h1 class="text-2xl font-semibold text-text-primary m-0">
          Sign in
        </h1>
        <p class="text-text-secondary text-sm mt-1">
          Report System admin console
        </p>
      </div>

      <button
        type="button"
        :disabled="loading"
        class="w-full flex items-center justify-center gap-2 py-2.5 bg-indigo-600 text-white rounded hover:bg-indigo-700 disabled:opacity-50 transition-colors"
        @click="handleKeycloakLogin"
      >
        <Icon icon="ant-design:safety-outlined" class="text-base" />
        Sign in with Keycloak (SSO)
      </button>

      <div class="flex items-center my-5">
        <div class="flex-1 border-t border-border" />
        <span class="px-3 text-xs text-text-secondary">OR</span>
        <div class="flex-1 border-t border-border" />
      </div>

      <form class="space-y-4" @submit.prevent="handleLogin">
        <div>
          <label class="block text-sm font-medium text-text-primary mb-1">Email</label>
          <input
            v-model="email"
            type="email"
            required
            class="w-full px-3 py-2 border border-border rounded text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
            placeholder="you@example.com"
          >
        </div>
        <div>
          <label class="block text-sm font-medium text-text-primary mb-1">Password</label>
          <input
            v-model="password"
            type="password"
            required
            class="w-full px-3 py-2 border border-border rounded text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
            placeholder="••••••••"
          >
        </div>
        <p v-if="error" class="text-sm text-danger flex items-center gap-1">
          <Icon icon="ant-design:exclamation-circle-outlined" />
          {{ error }}
        </p>
        <button
          type="submit"
          :disabled="loading"
          class="w-full py-2.5 bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50 transition-colors"
        >
          {{ loading ? 'Signing in...' : 'Sign in (legacy)' }}
        </button>
        <p class="text-xs text-text-disabled text-center">
          Legacy login is deprecated. Prefer Keycloak SSO.
        </p>
      </form>

      <p class="mt-5 text-center text-sm text-text-secondary">
        Don't have an account?
        <NuxtLink to="/register" class="text-primary hover:text-primary-hover">
          Register
        </NuxtLink>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
const { login, loginWithKeycloak } = useAuth()
const router = useRouter()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handleLogin = async () => {
  error.value = ''
  loading.value = true
  try {
    await login(email.value, password.value)
    router.push('/app/dashboard')
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Login failed'
  } finally {
    loading.value = false
  }
}

const handleKeycloakLogin = async () => {
  error.value = ''
  loading.value = true
  try {
    await loginWithKeycloak()
  } catch (e: any) {
    error.value = e?.message || 'SSO login failed'
    loading.value = false
  }
}
</script>

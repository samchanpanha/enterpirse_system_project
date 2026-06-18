export const useAuth = () => {
  const token = useState<string | null>('auth.token', () => null)
  const user = useState<any>('auth.user', () => null)
  const { $keycloak } = useNuxtApp() as any

  const login = async (email: string, password: string) => {
    if ($keycloak && $keycloak.authenticated) {
      // Already authenticated via Keycloak silent SSO; just bridge
      await bridgeFromKeycloak()
      return
    }
    // Fall back to legacy login (will be deprecated)
    const data: any = await $fetch('/api/auth/login', {
      method: 'POST',
      body: { email, password }
    })
    setSession(data.accessToken, data.user)
  }

  /**
   * Login via Keycloak (PKCE redirect flow).
   * Returns a promise that resolves after the redirect-back.
   */
  const loginWithKeycloak = async (): Promise<void> => {
    if (!$keycloak) {
      throw new Error('Keycloak not initialised (SSR or plugin failed)')
    }
    await $keycloak.login({
      redirectUri: window.location.origin + '/auth/callback'
    })
  }

  /**
   * Handle Keycloak redirect-back: exchange the Keycloak token for our
   * legacy JWT, then navigate to the dashboard.
   */
  const handleKeycloakCallback = async (): Promise<void> => {
    if (!$keycloak) { return }
    // The keycloak.init({onLoad: 'check-sso'}) on the callback page will
    // parse the URL fragment and authenticate. For PKCE we need init
    // with onLoad: 'login-required' or process manually:
    if (!$keycloak.authenticated) {
      // Process the redirect if not yet done
      await $keycloak.init({ onLoad: 'check-sso', pkceMethod: 'S256' })
    }
    if ($keycloak.token) {
      await bridgeFromKeycloak()
    }
  }

  const bridgeFromKeycloak = async () => {
    if (!$keycloak?.token) { return }
    const config = useRuntimeConfig()
    const data: any = await $fetch(`${config.public.apiBaseUrl}/auth/sso-login`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${$keycloak.token}` }
    })
    setSession(data.accessToken, data.user)
  }

  const register = async (firstName: string, lastName: string, email: string, password: string, tenantSlug: string) => {
    const data: any = await $fetch('/api/auth/register', {
      method: 'POST',
      body: { firstName, lastName, email, password, tenantSlug }
    })
    setSession(data.accessToken, data.user)
  }

  const logout = async () => {
    token.value = null
    user.value = null
    localStorage.removeItem('auth.token')
    localStorage.removeItem('auth.user')
    if ($keycloak?.authenticated) {
      try {
        await $keycloak.logout({ redirectUri: window.location.origin + '/login' })
      } catch (e) {
        console.warn('Keycloak logout failed', e)
      }
    }
    // Clear permission + feature caches
    if (import.meta.client) {
      try {
        const { usePermissionStore } = await import('~/stores/permission')
        const { useFeatureStore } = await import('~/stores/feature')
        usePermissionStore().clear()
        useFeatureStore().clear()
      } catch (e) { /* not registered yet */ }
    }
  }

  const setSession = (newToken: string, newUser: any) => {
    token.value = newToken
    user.value = newUser
    if (import.meta.client) {
      localStorage.setItem('auth.token', newToken)
      localStorage.setItem('auth.user', JSON.stringify(newUser))
    }
  }

  const init = () => {
    const savedToken = localStorage.getItem('auth.token')
    const savedUser = localStorage.getItem('auth.user')
    if (savedToken) { token.value = savedToken }
    if (savedUser) { user.value = JSON.parse(savedUser) }
  }

  if (import.meta.client) {
    init()
  }

  /**
   * Hook for after a successful login: fetch the current user's
   * permission codes + tenant feature set.
   */
  const afterLogin = async () => {
    if (import.meta.client) {
      try {
        const { usePermissionStore } = await import('~/stores/permission')
        const { useFeatureStore } = await import('~/stores/feature')
        await Promise.all([
          usePermissionStore().fetchPermissions(),
          useFeatureStore().fetchFeatures()
        ])
      } catch (e) { /* not yet wired */ }
    }
  }

  return {
    token,
    user,
    login,
    loginWithKeycloak,
    handleKeycloakCallback,
    register,
    logout,
    init,
    afterLogin
  }
}

// Keycloak client plugin (browser only)
// Initialises the Keycloak instance on the client side and provides
// it via useNuxtApp().$keycloak.
import Keycloak from 'keycloak-js'

export default defineNuxtPlugin(async (nuxtApp) => {
  const config = useRuntimeConfig()
  const keycloak = new Keycloak({
    url: config.public.keycloakUrl,
    realm: config.public.keycloakRealm,
    clientId: config.public.keycloakClientId
  })

  // Try silent SSO check on app load (uses hidden iframe if supported)
  try {
    const authenticated = await keycloak.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
      pkceMethod: 'S256',
      checkLoginIframe: false
    })
    if (authenticated) {
      // Bridge: exchange Keycloak token for legacy JWT
      await bridgeToBackend(keycloak.token)
    }
  } catch (e) {
    console.warn('Keycloak init failed', e)
  }

  nuxtApp.hook('app:mounted', () => {
    // expose globally for debugging
    ;(window as any).keycloak = keycloak
  })

  return {
    provide: {
      keycloak
    }
  }
})

async function bridgeToBackend (kcToken: string | undefined) {
  if (!kcToken) { return }
  const config = useRuntimeConfig()
  try {
    const resp = await $fetch(`${config.public.apiBaseUrl}/auth/sso-login`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${kcToken}` }
    })
    if (resp && (resp as any).accessToken) {
      localStorage.setItem('auth.token', (resp as any).accessToken)
      if ((resp as any).user) {
        localStorage.setItem('auth.user', JSON.stringify((resp as any).user))
      }
    }
  } catch (e) {
    console.warn('SSO bridge failed', e)
  }
}

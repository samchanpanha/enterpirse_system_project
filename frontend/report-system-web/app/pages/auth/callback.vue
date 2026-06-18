<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="text-center">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4" />
      <p class="text-gray-600">
        Completing sign-in via Keycloak...
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ layout: false })
const { handleKeycloakCallback } = useAuth()
const router = useRouter()
const route = useRoute()

onMounted(async () => {
  try {
    await handleKeycloakCallback()
    await router.push((route.query.redirect as string) || '/app/dashboard')
  } catch (e: any) {
    console.error('Keycloak callback failed', e)
    await router.push('/login?error=sso_failed')
  }
})
</script>

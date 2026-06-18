export default defineNuxtRouteMiddleware((_to, _from) => {
  const { user } = useAuth()

  if (!user.value) {
    return navigateTo('/login')
  }
})

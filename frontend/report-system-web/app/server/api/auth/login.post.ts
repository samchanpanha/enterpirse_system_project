export default defineEventHandler(async (event): Promise<any> => {
  const body = await readBody(event)
  const config = useRuntimeConfig()
  return await $fetch(`${config.public.apiBaseUrl}/api/auth/login`, {
    method: 'POST',
    body
  })
})

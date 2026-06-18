export default defineEventHandler(async (event): Promise<any> => {
  const config = useRuntimeConfig()
  const method = event.method
  const id = getRouterParam(event, 'id')
  const headers: Record<string, string> = {}
  for (const [k, v] of Object.entries(event.node.req.headers)) {
    if (typeof v === 'string') { headers[k] = v }
  }
  return await $fetch.raw(`${config.public.apiBaseUrl}/api/branches/${id}`, {
    method: method as any,
    headers,
    body: method !== 'GET' ? await readBody(event) : undefined
  }).then(r => r._data)
})

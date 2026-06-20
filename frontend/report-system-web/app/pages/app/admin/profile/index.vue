<template>
  <div class="p-6 max-w-2xl">
    <AdminPageHeader title="Profile" subtitle="Your account information" />

    <div class="bg-white rounded shadow-card p-6 space-y-6">
      <div class="flex items-center gap-4">
        <div class="w-16 h-16 rounded-full bg-primary text-white flex items-center justify-center text-xl font-semibold">
          {{ initials }}
        </div>
        <div>
          <h2 class="text-lg font-medium text-text-primary m-0">
            {{ fullName }}
          </h2>
          <p class="text-text-secondary text-sm mt-0.5">
            {{ user?.email }}
          </p>
        </div>
      </div>

      <dl class="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
        <div>
          <dt class="text-text-secondary mb-1">
            Tenant
          </dt>
          <dd class="text-text-primary font-medium">
            {{ user?.tenantName || user?.tenantId || '—' }}
          </dd>
        </div>
        <div>
          <dt class="text-text-secondary mb-1">
            Current branch
          </dt>
          <dd class="text-text-primary font-medium">
            {{ currentBranch?.name || 'All branches (admin)' }}
          </dd>
        </div>
        <div>
          <dt class="text-text-secondary mb-1">
            Permissions loaded
          </dt>
          <dd class="text-text-primary font-medium">
            {{ permissionCount }} codes
          </dd>
        </div>
      </dl>

      <div class="pt-4 border-t border-border">
        <p class="text-xs text-text-disabled">
          Profile editing will be available in a future release. Contact your tenant admin to update account details.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const { user } = useAuth()
const { currentBranch } = storeToRefs(useBranchStore())
const { codes } = usePermission()

const fullName = computed(() => {
  if (!user.value) { return 'Guest' }
  return [user.value.firstName, user.value.lastName].filter(Boolean).join(' ') || user.value.email
})

const initials = computed(() => {
  const name = fullName.value
  const parts = name.split(/\s+/).filter(Boolean)
  if (parts.length >= 2) {
    return (parts[0][0] + parts[1][0]).toUpperCase()
  }
  return (name[0] || 'G').toUpperCase()
})

const permissionCount = computed(() => codes.value.length)
</script>

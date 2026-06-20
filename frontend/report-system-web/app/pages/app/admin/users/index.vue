<template>
  <div>
    <AdminPageHeader title="Users" :subtitle="`Manage users for ${tenantLabel}`">
      <template #actions>
        <button
          v-if="can('users.write')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="drawer.openFor(null)"
        >
          <Icon icon="ant-design:plus-outlined" />
          New User
        </button>
      </template>
    </AdminPageHeader>

    <AdminSearchBar
      v-model="list.search.value"
      placeholder="Search by name or email…"
      :has-active-filters="!!list.filters.value.active"
      @reset="list.resetFilters()"
    >
      <template #filters>
        <select
          v-model="list.filters.value.active"
          class="text-sm border border-border rounded px-2 py-1.5 text-text-primary focus:outline-none focus:border-primary"
        >
          <option value="">
            All statuses
          </option>
          <option value="true">
            Active
          </option>
          <option value="false">
            Inactive
          </option>
        </select>
      </template>
    </AdminSearchBar>

    <AdminTable
      :items="filteredItems"
      :columns="columns"
      :loading="list.loading.value"
      :pagination="list.pagination"
      @page-change="list.setPage"
      @page-size-change="list.setPageSize"
    >
      <template #cell-name="{ row }">
        <div>
          <p class="font-medium text-text-primary">
            {{ row.firstName }} {{ row.lastName }}
          </p>
          <p class="text-xs text-text-secondary">
            {{ row.email }}
          </p>
        </div>
      </template>
      <template #cell-active="{ value }">
        <span :class="['inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded', value ? 'bg-success-light text-success' : 'bg-gray-100 text-text-secondary']">
          <span :class="['w-1.5 h-1.5 rounded-full', value ? 'bg-success' : 'bg-text-disabled']" />
          {{ value ? 'Active' : 'Inactive' }}
        </span>
      </template>
      <template #cell-roles="{ row }">
        <div class="flex flex-wrap gap-1">
          <span
            v-for="role in row.roleNames"
            :key="role"
            class="text-xs bg-primary-light text-primary px-1.5 py-0.5 rounded"
          >
            {{ role }}
          </span>
          <span v-if="!row.roleNames?.length" class="text-xs text-text-disabled">—</span>
        </div>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('users.write')"
          class="text-sm text-primary hover:text-primary-hover px-2 py-0.5"
          @click="roleDrawerOpenFor(row)"
        >
          Roles
        </button>
        <button
          v-if="can('users.write')"
          class="text-sm text-text-secondary hover:text-primary px-2 py-0.5"
          @click="resetPassword(row)"
        >
          Reset Password
        </button>
        <button
          v-if="can('users.write') && row.active"
          class="text-sm text-danger hover:opacity-80 px-2 py-0.5"
          @click="deactivate(row)"
        >
          Deactivate
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      title="New User"
      subtitle="Invite a user to your tenant"
      width="md"
    >
      <AdminForm
        v-if="drawer.open.value"
        v-model="formData"
        :groups="formGroups"
      />
      <p v-if="drawer.error.value" class="mt-3 text-sm text-danger bg-danger-light p-2 rounded">
        {{ drawer.error.value }}
      </p>
      <template #footer>
        <button class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50" @click="drawer.close()">
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50 inline-flex items-center gap-1.5"
          :disabled="drawer.saving.value"
          @click="submitForm"
        >
          <Icon v-if="drawer.saving.value" icon="ant-design:loading-outlined" class="animate-spin" />
          {{ drawer.saving.value ? 'Saving…' : 'Create' }}
        </button>
      </template>
    </AdminDrawer>

    <AdminDrawer
      v-model="roleDrawer.open.value"
      :title="`Roles for ${roleDrawer.editing.value?.firstName || ''} ${roleDrawer.editing.value?.lastName || ''}`"
      width="md"
    >
      <div class="space-y-2">
        <label
          v-for="role in roles"
          :key="role.id"
          class="flex items-center gap-3 p-3 border rounded hover:bg-gray-50 cursor-pointer"
        >
          <input
            v-model="selectedRoleIds"
            type="checkbox"
            :value="role.id"
            class="w-4 h-4 text-primary focus:ring-primary border-border rounded"
          >
          <div>
            <p class="text-sm font-medium text-text-primary">{{ role.name }}</p>
            <p v-if="role.description" class="text-xs text-text-secondary">{{ role.description }}</p>
          </div>
        </label>
      </div>
      <template #footer>
        <button class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50" @click="roleDrawer.close()">
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50"
          :disabled="roleDrawer.saving.value"
          @click="saveRoles"
        >
          {{ roleDrawer.saving.value ? 'Saving…' : 'Save Roles' }}
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import type { FormGroup } from '~/shared/types/form'
import type { Role, User } from '~/shared/types/auth'

definePageMeta({ middleware: 'auth' })

const { user } = useAuth()
const { can } = usePermission()
const config = useRuntimeConfig()
const list = useListPage<User & { roleNames?: string[] }>({ pageSize: 20, initialFilters: { active: '' } })
const drawer = useDrawer<User>()
const roleDrawer = useDrawer<User>()
const roles = ref<Role[]>([])
const selectedRoleIds = ref<string[]>([])

const tenantLabel = computed(() => user.value?.tenantName || user.value?.tenantId?.slice(0, 8) + '…' || 'your tenant')

const columns: ColumnDef[] = [
  { key: 'name', title: 'User', sortable: true },
  { key: 'phone', title: 'Phone', width: '160px' },
  { key: 'roles', title: 'Roles' },
  { key: 'active', title: 'Status', width: '120px' },
  { key: 'createdAt', title: 'Created', width: '180px' }
]

const filteredItems = computed(() => {
  let items = list.items.value
  if (list.search.value) {
    const q = list.search.value.toLowerCase()
    items = items.filter(u =>
      u.email.toLowerCase().includes(q) ||
      `${u.firstName} ${u.lastName}`.toLowerCase().includes(q)
    )
  }
  if (list.filters.value.active === 'true') { items = items.filter(u => u.active) }
  if (list.filters.value.active === 'false') { items = items.filter(u => !u.active) }
  return items
})

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  {
    title: 'Profile',
    fields: [
      { key: 'firstName', label: 'First name', type: 'text', required: true, span: 6 },
      { key: 'lastName', label: 'Last name', type: 'text', required: true, span: 6 },
      { key: 'email', label: 'Email', type: 'email', required: true, span: 12 },
      { key: 'phone', label: 'Phone', type: 'tel', span: 6 },
      { key: 'password', label: 'Initial password', type: 'password', required: true, span: 6 }
    ]
  }
])

async function fetchUsers () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    const res: any = await $fetch(`${config.public.apiBaseUrl}/api/users/by-tenant/${user.value.tenantId}`, {
      headers: { Authorization: `Bearer ${useAuth().token.value}` }
    })
    const users: User[] = Array.isArray(res) ? res : []
    // Enrich with role names
    const enriched = await Promise.all(users.map(async (u) => {
      try {
        const roleRes: any = await $fetch(`${config.public.apiBaseUrl}/api/users/${u.id}/roles`, {
          headers: { Authorization: `Bearer ${useAuth().token.value}` }
        })
        return { ...u, roleNames: (Array.isArray(roleRes) ? roleRes : []).map((r: Role) => r.name) }
      } catch (e) {
        return { ...u, roleNames: [] }
      }
    }))
    list.setItems(enriched, enriched.length)
  } catch (e: any) {
    list.error.value = e?.data?.message || 'Failed to load users'
  } finally {
    list.loading.value = false
  }
}

async function fetchRoles () {
  if (!user.value?.tenantId) { return }
  try {
    const res: any = await $fetch(`${config.public.apiBaseUrl}/api/roles/by-tenant/${user.value.tenantId}`, {
      headers: { Authorization: `Bearer ${useAuth().token.value}` }
    })
    roles.value = Array.isArray(res) ? res : []
  } catch (e) {
    roles.value = []
  }
}

async function submitForm () {
  const f = drawer.formData.value
  if (!f.firstName || !f.lastName || !f.email || !f.password) {
    drawer.error.value = 'All fields are required'
    return
  }
  drawer.saving.value = true
  drawer.error.value = null
  try {
    await $fetch(`${config.public.apiBaseUrl}/api/users`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${useAuth().token.value}`, 'Content-Type': 'application/json' },
      body: {
        tenantId: user.value?.tenantId,
        email: f.email,
        password: f.password,
        firstName: f.firstName,
        lastName: f.lastName,
        phone: f.phone || null
      }
    })
    await fetchUsers()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.data?.message || 'Failed to create user'
  } finally {
    drawer.saving.value = false
  }
}

async function roleDrawerOpenFor (u: User) {
  roleDrawer.openFor(u)
  const res: any = await $fetch(`${config.public.apiBaseUrl}/api/users/${u.id}/roles`, {
    headers: { Authorization: `Bearer ${useAuth().token.value}` }
  }).catch(() => [])
  selectedRoleIds.value = (Array.isArray(res) ? res : []).map((r: Role) => r.id)
}

async function saveRoles () {
  if (!roleDrawer.editing.value) { return }
  roleDrawer.saving.value = true
  try {
    await $fetch(`${config.public.apiBaseUrl}/api/users/${roleDrawer.editing.value.id}/roles`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${useAuth().token.value}`, 'Content-Type': 'application/json' },
      body: { roleIds: selectedRoleIds.value }
    })
    await fetchUsers()
    roleDrawer.close()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to assign roles')
  } finally {
    roleDrawer.saving.value = false
  }
}

async function resetPassword (u: User) {
  const password = prompt(`Enter new password for ${u.email}`)
  if (!password) { return }
  try {
    await $fetch(`${config.public.apiBaseUrl}/api/users/${u.id}/password`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${useAuth().token.value}`, 'Content-Type': 'application/json' },
      body: { password }
    })
    alert('Password updated')
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to reset password')
  }
}

async function deactivate (u: User) {
  if (!confirm(`Deactivate ${u.email}?`)) { return }
  try {
    await $fetch(`${config.public.apiBaseUrl}/api/users/${u.id}/deactivate`, {
      method: 'PATCH',
      headers: { Authorization: `Bearer ${useAuth().token.value}` }
    })
    await fetchUsers()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to deactivate user')
  }
}

onMounted(async () => {
  await fetchRoles()
  await fetchUsers()
})
</script>

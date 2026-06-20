<template>
  <div>
    <AdminPageHeader title="Roles" :subtitle="`Manage roles and permissions for ${tenantLabel}`">
      <template #actions>
        <button
          v-if="can('roles.write')"
          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-primary text-white text-sm rounded hover:bg-primary-hover transition-colors"
          @click="drawer.openFor(null)"
        >
          <Icon icon="ant-design:plus-outlined" />
          New Role
        </button>
      </template>
    </AdminPageHeader>

    <AdminTable
      :items="list.items.value"
      :columns="columns"
      :loading="list.loading.value"
      :pagination="list.pagination"
      @page-change="list.setPage"
      @page-size-change="list.setPageSize"
    >
      <template #cell-name="{ row }">
        <div class="flex items-center gap-2">
          <span class="font-medium text-text-primary">{{ row.name }}</span>
          <span v-if="row.system" class="text-xs bg-gray-100 text-text-secondary px-1.5 py-0.5 rounded">SYSTEM</span>
        </div>
      </template>
      <template #actions="{ row }">
        <button
          v-if="can('roles.write') && !row.system"
          class="text-sm text-primary hover:text-primary-hover px-2 py-0.5"
          @click="permDrawerOpenFor(row)"
        >
          Permissions
        </button>
        <button
          v-if="can('roles.write') && !row.system"
          class="text-sm text-danger hover:opacity-80 px-2 py-0.5"
          @click="del(row)"
        >
          Delete
        </button>
      </template>
    </AdminTable>

    <AdminDrawer
      v-model="drawer.open.value"
      title="New Role"
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
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50"
          :disabled="drawer.saving.value"
          @click="submitForm"
        >
          {{ drawer.saving.value ? 'Saving…' : 'Create' }}
        </button>
      </template>
    </AdminDrawer>

    <AdminDrawer
      v-model="permDrawer.open.value"
      :title="`Permissions for ${permDrawer.editing.value?.name}`"
      width="lg"
    >
      <div class="space-y-4 max-h-[60vh] overflow-y-auto pr-2">
        <div v-for="mod in permissionModules" :key="mod">
          <h4 class="text-sm font-semibold text-text-primary mb-2 capitalize">
            {{ mod }}
          </h4>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-2">
            <label
              v-for="perm in permissionsByModule(mod)"
              :key="perm.id"
              class="flex items-center gap-2 p-2 border rounded hover:bg-gray-50 cursor-pointer"
            >
              <input
                v-model="selectedPermissionIds"
                type="checkbox"
                :value="perm.id"
                class="w-4 h-4 text-primary focus:ring-primary border-border rounded"
              >
              <span class="text-sm text-text-primary">{{ perm.name || perm.code }}</span>
            </label>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="px-3 py-1.5 text-sm border border-border rounded text-text-primary hover:bg-gray-50" @click="permDrawer.close()">
          Cancel
        </button>
        <button
          class="px-3 py-1.5 text-sm bg-primary text-white rounded hover:bg-primary-hover disabled:opacity-50"
          :disabled="permDrawer.saving.value"
          @click="savePermissions"
        >
          {{ permDrawer.saving.value ? 'Saving…' : 'Save Permissions' }}
        </button>
      </template>
    </AdminDrawer>
  </div>
</template>

<script setup lang="ts">
import type { ColumnDef } from '~/components/admin/AdminTable.vue'
import type { FormGroup } from '~/shared/types/form'
import type { Permission, Role } from '~/shared/types/auth'

definePageMeta({ middleware: 'auth' })

const { user } = useAuth()
const { can } = usePermission()
const config = useRuntimeConfig()
const list = useListPage<Role>({ pageSize: 20 })
const drawer = useDrawer<Role>()
const permDrawer = useDrawer<Role>()
const permissions = ref<Permission[]>([])
const selectedPermissionIds = ref<string[]>([])

const tenantLabel = computed(() => user.value?.tenantName || user.value?.tenantId?.slice(0, 8) + '…' || 'your tenant')

const columns: ColumnDef[] = [
  { key: 'name', title: 'Role', sortable: true },
  { key: 'description', title: 'Description' },
  { key: 'createdAt', title: 'Created', width: '180px' }
]

const formData = computed({
  get: () => drawer.formData.value,
  set: (val) => { drawer.formData.value = val }
})

const formGroups = computed<FormGroup[]>(() => [
  {
    title: 'Role',
    fields: [
      { key: 'name', label: 'Name', type: 'text', required: true, span: 12 },
      { key: 'description', label: 'Description', type: 'textarea', span: 12 }
    ]
  }
])

const permissionModules = computed(() => [...new Set(permissions.value.map(p => p.module || 'general'))].sort())

function permissionsByModule (mod: string) {
  return permissions.value.filter(p => (p.module || 'general') === mod)
}

async function fetchRoles () {
  if (!user.value?.tenantId) { return }
  list.loading.value = true
  try {
    const res: any = await $fetch(`${config.public.apiBaseUrl}/api/roles/by-tenant/${user.value.tenantId}`, {
      headers: { Authorization: `Bearer ${useAuth().token.value}` }
    })
    list.setItems(Array.isArray(res) ? res : [], (Array.isArray(res) ? res : []).length)
  } catch (e: any) {
    list.error.value = e?.data?.message || 'Failed to load roles'
  } finally {
    list.loading.value = false
  }
}

async function fetchPermissions () {
  try {
    const res: any = await $fetch(`${config.public.apiBaseUrl}/api/permissions`, {
      headers: { Authorization: `Bearer ${useAuth().token.value}` }
    })
    permissions.value = Array.isArray(res) ? res : []
  } catch (e) {
    permissions.value = []
  }
}

async function submitForm () {
  const f = drawer.formData.value
  if (!f.name) {
    drawer.error.value = 'Name is required'
    return
  }
  drawer.saving.value = true
  drawer.error.value = null
  try {
    await $fetch(`${config.public.apiBaseUrl}/api/roles`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${useAuth().token.value}`, 'Content-Type': 'application/json' },
      body: {
        tenantId: user.value?.tenantId,
        name: f.name,
        description: f.description || null,
        system: false
      }
    })
    await fetchRoles()
    drawer.close()
  } catch (e: any) {
    drawer.error.value = e?.data?.message || 'Failed to create role'
  } finally {
    drawer.saving.value = false
  }
}

async function permDrawerOpenFor (role: Role) {
  permDrawer.openFor(role)
  const res: any = await $fetch(`${config.public.apiBaseUrl}/api/roles/${role.id}/permissions`, {
    headers: { Authorization: `Bearer ${useAuth().token.value}` }
  }).catch(() => [])
  selectedPermissionIds.value = (Array.isArray(res) ? res : []).map((p: Permission) => p.id)
}

async function savePermissions () {
  if (!permDrawer.editing.value) { return }
  permDrawer.saving.value = true
  try {
    await $fetch(`${config.public.apiBaseUrl}/api/roles/${permDrawer.editing.value.id}/permissions`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${useAuth().token.value}`, 'Content-Type': 'application/json' },
      body: { permissionIds: selectedPermissionIds.value }
    })
    permDrawer.close()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to save permissions')
  } finally {
    permDrawer.saving.value = false
  }
}

async function del (role: Role) {
  if (!confirm(`Delete role "${role.name}"?`)) { return }
  try {
    await $fetch(`${config.public.apiBaseUrl}/api/roles/${role.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${useAuth().token.value}` }
    })
    await fetchRoles()
  } catch (e: any) {
    alert(e?.data?.message || 'Failed to delete role')
  }
}

onMounted(async () => {
  await fetchPermissions()
  await fetchRoles()
})
</script>

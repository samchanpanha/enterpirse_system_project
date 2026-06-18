<template>
  <div>
    <AdminPageHeader
      title="Administration"
      subtitle="Manage tenants, branches, users, and feature enablement"
    />

    <div class="p-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <NuxtLink
        to="/app/admin/branches"
        class="bg-white rounded shadow-card p-6 hover:shadow-popover transition-shadow"
      >
        <div class="w-12 h-12 rounded bg-primary flex items-center justify-center mb-3">
          <Icon icon="ant-design:bank-outlined" class="text-white text-2xl" />
        </div>
        <h3 class="text-base font-semibold text-text-primary">
          Branches
        </h3>
        <p class="text-sm text-text-secondary mt-1">
          Manage physical branch locations and their settings.
        </p>
      </NuxtLink>

      <NuxtLink
        to="/app/admin/clients"
        class="bg-white rounded shadow-card p-6 hover:shadow-popover transition-shadow"
      >
        <div class="w-12 h-12 rounded bg-success flex items-center justify-center mb-3">
          <Icon icon="ant-design:team-outlined" class="text-white text-2xl" />
        </div>
        <h3 class="text-base font-semibold text-text-primary">
          Clients
        </h3>
        <p class="text-sm text-text-secondary mt-1">
          Manage SaaS tenants and their subscription tier.
        </p>
      </NuxtLink>

      <NuxtLink
        to="/app/admin/features"
        class="bg-white rounded shadow-card p-6 hover:shadow-popover transition-shadow"
      >
        <div class="w-12 h-12 rounded bg-warning flex items-center justify-center mb-3">
          <Icon icon="ant-design:appstore-outlined" class="text-white text-2xl" />
        </div>
        <h3 class="text-base font-semibold text-text-primary">
          Features
        </h3>
        <p class="text-sm text-text-secondary mt-1">
          Enable or disable features per client.
        </p>
      </NuxtLink>
    </div>
  </div>
</template>

<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
</script>

<template>
  <div class="bg-white">
    <div class="overflow-x-auto">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-border">
          <tr>
            <th v-if="selectable" class="w-10 px-3 py-2.5">
              <input
                type="checkbox"
                :checked="allSelected"
                class="rounded border-border text-primary focus:ring-primary/30"
                @change="emit('toggleAll')"
              >
            </th>
            <th
              v-for="col in columns"
              :key="col.key"
              :class="[
                'px-3 py-2.5 text-left font-medium text-text-secondary whitespace-nowrap',
                col.sortable ? 'cursor-pointer hover:text-primary select-none' : '',
                col.align === 'right' ? 'text-right' : '',
                col.align === 'center' ? 'text-center' : '',
                col.width ? `w-[${col.width}]` : ''
              ]"
              @click="col.sortable && emit('sort', col.key)"
            >
              <span class="inline-flex items-center gap-1">
                {{ col.title }}
                <Icon
                  v-if="col.sortable"
                  :icon="sortIcon(col.key)"
                  :class="['text-xs', sortField === col.key ? 'text-primary' : 'text-text-disabled']"
                />
              </span>
            </th>
            <th v-if="$slots.actions" class="px-3 py-2.5 text-right font-medium text-text-secondary w-32">
              Actions
            </th>
          </tr>
        </thead>
        <tbody>
          <template v-if="loading">
            <tr v-for="n in 5" :key="`s-${n}`" class="border-b border-border last:border-0">
              <td v-if="selectable" class="px-3 py-3">
                <div class="w-4 h-4 bg-gray-200 rounded animate-pulse" />
              </td>
              <td
                v-for="col in columns"
                :key="`s-${n}-${col.key}`"
                class="px-3 py-3"
              >
                <div class="h-4 bg-gray-200 rounded animate-pulse" :style="{ width: '70%' }" />
              </td>
              <td v-if="$slots.actions" class="px-3 py-3 text-right">
                <div class="h-4 bg-gray-200 rounded animate-pulse w-16 ml-auto" />
              </td>
            </tr>
          </template>
          <template v-else-if="items.length === 0">
            <tr>
              <td :colspan="totalCols" class="p-0">
                <slot name="empty">
                  <AdminEmpty
                    icon="ant-design:table-outlined"
                    title="No data"
                    description="There are no records to display."
                  />
                </slot>
              </td>
            </tr>
          </template>
          <template v-else>
            <tr
              v-for="(row, idx) in items"
              :key="getRowKey(row, idx)"
              class="border-b border-border last:border-0 hover:bg-primary-light/40 transition-colors"
            >
              <td v-if="selectable" class="px-3 py-2.5">
                <input
                  type="checkbox"
                  :checked="isSelected(row)"
                  class="rounded border-border text-primary focus:ring-primary/30"
                  @change="emit('toggleSelect', row)"
                >
              </td>
              <td
                v-for="col in columns"
                :key="`${idx}-${col.key}`"
                :class="[
                  'px-3 py-2.5 text-text-primary',
                  col.align === 'right' ? 'text-right' : '',
                  col.align === 'center' ? 'text-center' : '',
                  col.className || ''
                ]"
              >
                <slot :name="`cell-${col.key}`" :row="row" :value="getValue(row, col.key)" :col="col">
                  {{ formatValue(getValue(row, col.key), col) }}
                </slot>
              </td>
              <td v-if="$slots.actions" class="px-3 py-2.5 text-right">
                <div class="inline-flex items-center gap-1">
                  <slot name="actions" :row="row" />
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>

    <div
      v-if="pagination && (pagination.total > 0 || items.length > 0)"
      class="flex items-center justify-between px-4 py-3 border-t border-border bg-gray-50 text-sm"
    >
      <div class="text-text-secondary">
        <slot name="pagination-info" :pagination="pagination">
          Showing
          <span class="font-medium text-text-primary">{{ rangeStart }}</span>
          –
          <span class="font-medium text-text-primary">{{ rangeEnd }}</span>
          of
          <span class="font-medium text-text-primary">{{ pagination.total }}</span>
        </slot>
      </div>
      <div class="flex items-center gap-2">
        <select
          v-if="showSizeChanger"
          :value="pagination.pageSize"
          class="text-sm border border-border rounded px-2 py-1 text-text-primary focus:outline-none focus:border-primary"
          @change="onPageSizeChange($event)"
        >
          <option v-for="s in pageSizes" :key="s" :value="s">
            {{ s }} / page
          </option>
        </select>
        <button
          type="button"
          :disabled="pagination.page <= 1"
          class="p-1 rounded hover:bg-gray-200 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          @click="emit('pageChange', pagination.page - 1)"
        >
          <Icon icon="ant-design:left-outlined" />
        </button>
        <span class="text-text-primary px-2">
          {{ pagination.page }} / {{ pagination.totalPages || 1 }}
        </span>
        <button
          type="button"
          :disabled="pagination.page >= (pagination.totalPages || 1)"
          class="p-1 rounded hover:bg-gray-200 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          @click="emit('pageChange', pagination.page + 1)"
        >
          <Icon icon="ant-design:right-outlined" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" generic="T extends Record<string, any>">
import type { PaginationInfo } from '~/composables/useListPage'

export interface ColumnDef {
  key: string
  title: string
  sortable?: boolean
  align?: 'left' | 'right' | 'center'
  width?: string
  className?: string
  /** If true, render as a status tag (uses statusColor helper) */
  status?: boolean
  /** Format the value for display */
  formatter?: (val: any, row: Record<string, any>) => string
}

interface Props {
  items: T[]
  columns: ColumnDef[]
  loading?: boolean
  pagination?: PaginationInfo
  sortField?: string | null
  sortOrder?: 'asc' | 'desc' | null
  selectable?: boolean
  selectedIds?: Set<string>
  rowKey?: keyof T | ((row: T) => string)
  pageSizes?: number[]
  showSizeChanger?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  selectable: false,
  pageSizes: () => [10, 20, 50, 100],
  showSizeChanger: true
})

const emit = defineEmits<{
  sort: [field: string]
  pageChange: [page: number]
  pageSizeChange: [size: number]
  toggleSelect: [row: T]
  toggleAll: []
}>()

const slots = useSlots()

const totalCols = computed(() => {
  return props.columns.length + (props.selectable ? 1 : 0) + (slots.actions ? 1 : 0)
})

const allSelected = computed(() => {
  if (!props.items.length) { return false }
  if (!props.selectedIds) { return false }
  return props.items.every(it => props.selectedIds!.has(getRowKey(it, 0)))
})

function getRowKey (row: T, idx: number): string {
  if (props.rowKey) {
    if (typeof props.rowKey === 'function') { return (props.rowKey as any)(row) }
    return String(row[props.rowKey])
  }
  return String(row.id ?? idx)
}

function getValue (row: T, key: string): any {
  return key.split('.').reduce((acc: any, k) => acc?.[k], row)
}

function isSelected (row: T): boolean {
  return props.selectedIds?.has(getRowKey(row, 0)) ?? false
}

function formatValue (val: any, col: ColumnDef): string {
  if (val === null || val === undefined) { return '—' }
  if (col.formatter) { return col.formatter(val, {} as Record<string, any>) }
  if (typeof val === 'boolean') { return val ? 'Yes' : 'No' }
  return String(val)
}

function sortIcon (key: string): string {
  if (props.sortField !== key) { return 'ant-design:sort-ascending-outlined' }
  return props.sortOrder === 'desc' ? 'ant-design:sort-descending-outlined' : 'ant-design:sort-ascending-outlined'
}

function onPageSizeChange (e: Event) {
  emit('pageSizeChange', Number((e.target as HTMLSelectElement).value))
}

const rangeStart = computed(() => {
  if (!props.pagination || !props.items.length) { return 0 }
  return (props.pagination.page - 1) * props.pagination.pageSize + 1
})

const rangeEnd = computed(() => {
  if (!props.pagination || !props.items.length) { return 0 }
  return rangeStart.value + props.items.length - 1
})
</script>

import type { ColumnDef } from '~/components/admin/AdminTable.vue'

export function exportToCsv<T extends Record<string, any>> (
  data: T[],
  columns: ColumnDef[],
  filename: string
) {
  if (!data.length) { return }

  const headers = columns.map(c => c.title)
  const rows = data.map(row =>
    columns.map((col) => {
      const val = col.key.split('.').reduce((acc: any, k) => acc?.[k], row)
      if (val === null || val === undefined) { return '""' }
      const str = String(val).replace(/"/g, '""')
      return `"${str}"`
    })
  )

  const bom = '\uFEFF'
  const csv = bom + headers.join(',') + '\n' + rows.map(r => r.join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${filename}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

export function useExport () {
  return { exportToCsv }
}

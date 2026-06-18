export interface Branch {
  id: string
  tenantId: string
  code: string
  name: string
  nameKh?: string
  branchType: string
  parentId?: string
  address?: string
  city?: string
  district?: string
  province?: string
  phone?: string
  email?: string
  timezone?: string
  locale?: string
  currency?: string
  taxRate?: number
  logoUrl?: string
  settings?: string
  active: boolean
  default: boolean
  openedAt?: string
  closedAt?: string
  createdAt?: string
  updatedAt?: string
}

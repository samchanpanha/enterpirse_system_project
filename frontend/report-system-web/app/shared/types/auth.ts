export interface UserInfo {
  id: string
  email: string
  firstName: string
  lastName: string
  tenantId: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  password: string
  tenantSlug: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: UserInfo
}

export interface User {
  id: string
  tenantId: string
  email: string
  firstName: string
  lastName: string
  phone?: string
  locale?: string
  active: boolean
  lastLoginAt?: string
  createdAt: string
  updatedAt?: string
}

export interface Role {
  id: string
  tenantId: string
  name: string
  description?: string
  system: boolean
  createdAt: string
}

export interface Permission {
  id: string
  code: string
  name?: string
  module?: string
  createdAt: string
}

export interface Tenant {
  id: string
  name: string
  slug: string
  domain?: string
  logoUrl?: string
  active: boolean
  subscription?: string
  settings?: string
  createdAt: string
  updatedAt?: string
}

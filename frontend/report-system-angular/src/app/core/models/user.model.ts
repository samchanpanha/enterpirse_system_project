export interface User {
  id: string;
  email: string;
  name: string;
  tenantId: string;
  branchIds: string[];
  roles: string[];
  permissions: string[];
}

export interface LoginRequest {
  email: string;
  password: string;
  tenantId?: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  user: User;
}

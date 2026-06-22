import { request } from '@/utils/http'

export interface LoginPayload {
  account: string
  password: string
  remember?: boolean
}

export interface LoginResult {
  token: string
  account_id: string
  account_type: string
  login_scope: string
}

export interface PortalProfile {
  account_id: string
  nickname?: string | null
  avatar_url?: string | null
  bio?: string | null
  level?: string | null
  created_at?: string | null
  updated_at?: string | null
}

export interface PortalMe {
  account_id: string
  account_type: string
  login_scope: string
  profile?: PortalProfile | null
}

export function login(data: LoginPayload) {
  return request.post<LoginResult>('/api/v1/portal/auth/login', data)
}

export function logout() {
  return request.post<{ success: boolean }>('/api/v1/portal/auth/logout')
}

export function getMe() {
  return request.get<PortalMe>('/api/v1/portal/profile/me')
}

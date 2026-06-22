import type { ResponseResult } from '@hei/shared'

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

async function send<T>(method: Promise<ResponseResult<T | null>> | Promise<T>) {
  const result = await method
  if (isResponseResult<T>(result)) {
    if (!result.isSuccess || result.data === null) {
      throw new Error(result.message)
    }
    return result.data
  }
  return result
}

function isResponseResult<T>(result: ResponseResult<T | null> | T): result is ResponseResult<T | null> {
  return Boolean(result && typeof result === 'object' && 'isSuccess' in result)
}

export function login(data: LoginPayload) {
  return send(request.post<ResponseResult<LoginResult | null>>('/api/v1/portal/auth/login', data))
}

export function logout() {
  return send(request.post<ResponseResult<{ success: boolean } | null>>('/api/v1/portal/auth/logout'))
}

export function getMe() {
  return send(request.get<ResponseResult<PortalMe | null>>('/api/v1/portal/profile/me'))
}

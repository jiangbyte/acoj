import type { AdminMe, LoginPayload, LoginResult } from '@/apis/auth'
import { delay } from '@mock/utils'
import { mockMe } from '@mock/data'

export async function login(payload: LoginPayload): Promise<LoginResult> {
  if (!payload.account || !payload.password) {
    throw new Error('请输入账号和密码')
  }

  return delay({
    token: 'mock-admin-token',
    account_id: mockMe.account_id,
    account_type: 'admin',
    login_scope: 'admin',
  })
}

export async function logout(): Promise<{ success: boolean }> {
  return delay({ success: true })
}

export async function getMe(): Promise<AdminMe> {
  return delay(mockMe)
}

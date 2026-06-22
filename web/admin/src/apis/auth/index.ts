import { getMe as getMockMe, login as mockLogin, logout as mockLogout } from '@mock/modules/auth'

interface MockResponse<T> {
  isSuccess: boolean
  errorType: null
  code: number
  message: string
  data: T
}

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

export interface AdminProfile {
  account_id: string
  real_name?: string | null
  avatar_url?: string | null
  title?: string | null
  employee_no?: string | null
  created_at?: string | null
  updated_at?: string | null
}

export interface AdminMe {
  account_id: string
  account_type: string
  login_scope: string
  permissions?: string[]
  profile?: AdminProfile | null
}

function mockSuccess<T>(data: T): MockResponse<T> {
  return {
    isSuccess: true,
    errorType: null,
    code: 200,
    message: 'success',
    data,
  }
}

export function login(data: LoginPayload) {
  return mockLogin(data).then((result) => mockSuccess<LoginResult | null>(result))
}

export function logout() {
  return mockLogout().then((result) => mockSuccess<{ success: boolean } | null>(result))
}

export function getMe() {
  return getMockMe().then((result) => mockSuccess<AdminMe | null>(result))
}

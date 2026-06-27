import { http } from '@/utils'

const authPrefix = '/api/v1/admin/auth'

export function login(data: any) {
  return http.post<any>(`${authPrefix}/login`, data, {
    addToken: false,
  })
}

export function logout() {
  return http.post<any>(`${authPrefix}/logout`)
}

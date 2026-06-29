import { http } from '@/utils'

const authPrefix = '/api/v1/admin'

export function login(data: any) {
  return http.post<any>(`${authPrefix}/login`, data, {
    addToken: false,
  })
}

export function register(data: any) {
  return http.post<any>(`${authPrefix}/register`, data, {
    addToken: false,
  })
}

export function logout() {
  return http.post<any>(`${authPrefix}/logout`)
}

export function cancel(data: any) {
  return http.post<any>(`${authPrefix}/cancel`, data)
}

export function me() {
  return http.get<any>(`${authPrefix}/me`)
}

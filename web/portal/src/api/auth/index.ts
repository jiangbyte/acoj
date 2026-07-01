import { http } from '@/utils'

const authPrefix = '/api/v1/portal'

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

export function updateUserCenterProfile(data: any) {
  return http.post<any>(`${authPrefix}/user-center/profile/update`, data)
}

export function updateUserCenterPassword(data: any) {
  return http.post<any>(`${authPrefix}/user-center/password/update`, data)
}

export function updateUserCenterPhone(data: any) {
  return http.post<any>(`${authPrefix}/user-center/phone/update`, data)
}

export function updateUserCenterEmail(data: any) {
  return http.post<any>(`${authPrefix}/user-center/email/update`, data)
}

import { http } from '@/utils/request'

const prefix = '/api/v1/admin'

export function captcha(params?: any) {
  return http.get<any>(`${prefix}/captcha`, params, { addToken: false })
}

export function passwordKey() {
  return http.get<any>(`${prefix}/password-key`, undefined, { addToken: false })
}

export function login(data: any) {
  return http.post<any>(`${prefix}/login`, data, { addToken: false })
}

export function logout() {
  return http.post<any>(`${prefix}/logout`)
}

export function me() {
  return http.get<any>(`${prefix}/me`)
}

export function forgotPassword(data: any) {
  return http.post<any>(`${prefix}/forgot-password`, data, { addToken: false })
}

export function resetPassword(data: any) {
  return http.post<any>(`${prefix}/reset-password`, data, { addToken: false })
}

export function updateProfile(data: any) {
  return http.post<any>(`${prefix}/user-center/profile/update`, data)
}

export function updatePassword(data: any) {
  return http.post<any>(`${prefix}/user-center/password/update`, data)
}

export function updatePhone(data: any) {
  return http.post<any>(`${prefix}/user-center/phone/update`, data)
}

export function updateEmail(data: any) {
  return http.post<any>(`${prefix}/user-center/email/update`, data)
}

export function orgInfo() {
  return http.get<any>(`${prefix}/user-center/org-info`)
}

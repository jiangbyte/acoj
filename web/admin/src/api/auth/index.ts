import { http } from '@/utils'

const authPrefix = '/api/v1/admin'

export function login(data: any) {
  return http.post<any>(`${authPrefix}/login`, data, {
    addToken: false,
  })
}

export function captcha() {
  return http.get<any>(`${authPrefix}/captcha`, {
    addToken: false,
  })
}

export function passwordKey() {
  return http.get<any>(`${authPrefix}/password-key`, {
    addToken: false,
  })
}

export function forgotPassword(data: any) {
  return http.post<any>(`${authPrefix}/forgot-password`, data, {
    addToken: false,
  })
}

export function resetPassword(data: any) {
  return http.post<any>(`${authPrefix}/reset-password`, data, {
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

export function uploadUserCenterAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<any>(`${authPrefix}/user-center/avatar/upload`, formData)
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

export function userCenterOrgInfo() {
  return http.get<any>(`${authPrefix}/user-center/org-info`)
}

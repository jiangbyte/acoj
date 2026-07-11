import { http } from '@/utils/request'

const prefix = '/api/v1/portal'

export function captcha(params?: any) {
  return http.get<any>(`${prefix}/captcha`, params, { addToken: false })
}

export function passwordKey() {
  return http.get<any>(`${prefix}/password-key`, undefined, { addToken: false })
}

export function login(data: any) {
  return http.post<any>(`${prefix}/login`, data, { addToken: false })
}

export function register(data: any) {
  return http.post<any>(`${prefix}/register`, data, { addToken: false })
}

export function logout() {
  return http.post<any>(`${prefix}/logout`)
}

export function cancel(data: any) {
  return http.post<any>(`${prefix}/cancel`, data)
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

export function updateUserCenterProfile(data: any) {
  return http.post<any>(`${prefix}/user-center/profile/update`, data)
}

export function uploadUserCenterAvatar(filePath: string) {
  return uploadFile(`${prefix}/user-center/avatar/upload`, filePath)
}

export function updateUserCenterPassword(data: any) {
  return http.post<any>(`${prefix}/user-center/password/update`, data)
}

export function updateUserCenterPhone(data: any) {
  return http.post<any>(`${prefix}/user-center/phone/update`, data)
}

export function updateUserCenterEmail(data: any) {
  return http.post<any>(`${prefix}/user-center/email/update`, data)
}

function uploadFile(url: string, filePath: string) {
  return new Promise<any>((resolve, reject) => {
    const baseURL = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '')
    const token = uni.getStorageSync('portal_token')
    uni.uploadFile({
      url: `${baseURL}${url}`,
      filePath,
      name: 'file',
      header: token ? { Authorization: String(token) } : {},
      success(response) {
        try {
          const body = JSON.parse(response.data)
          if (body?.code !== 0 && body?.code !== 200) {
            uni.showToast({ title: body?.message || '上传失败', icon: 'none' })
            reject(body)
            return
          }
          resolve(body.data)
        } catch (error) {
          reject(error)
        }
      },
      fail: reject,
    })
  })
}

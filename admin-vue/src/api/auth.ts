import { request } from '@/utils'

export function fetchCaptcha() {
  return request.Get<Service.ResponseResult>('/api/v1/public/b/captcha')
}

export function fetchLogin(data: {
  username: string
  password: string
  captcha_code?: string
  captcha_id?: string
  device_id?: string
}) {
  return request.Post<Service.ResponseResult<{ token: string }>>('/api/v1/public/b/login', data)
}

export function fetchRegister(data: any) {
  return request.Post<Service.ResponseResult>('/api/v1/public/b/register', data)
}

export function fetchSm2PublicKey() {
  return request.Get<Service.ResponseResult<string>>('/api/v1/public/b/sm2/public-key')
}

export function fetchLogout() {
  return request.Post<Service.ResponseResult>('/api/v1/b/logout')
}

export function fetchCurrentUser() {
  return request.Get<Service.ResponseResult>('/api/v1/sys/user/current')
}

export function fetchUserMenus() {
  return request.Get<Service.ResponseResult>('/api/v1/sys/user/menus')
}

export function fetchUserPermissions() {
  return request.Get<Service.ResponseResult<string[]>>('/api/v1/sys/user/permissions')
}

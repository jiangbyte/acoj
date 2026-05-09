import { request } from '../http'

export function fetchCaptcha() {
  return request.Get('/api/v1/public/b/captcha')
}

export function fetchLogin(data: any) {
  return request.Post('/api/v1/public/b/login', data)
}

export function fetchRegister(data: any) {
  return request.Post('/api/v1/public/b/register', data)
}

export function fetchLogout() {
  return request.Post('/api/v1/b/logout')
}

export function fetchCurrentUser() {
  return request.Get('/api/v1/sys/user/current')
}

export function fetchUserMenus() {
  return request.Get('/api/v1/sys/user/menus')
}

export function fetchUserPermissions() {
  return request.Get('/api/v1/sys/user/permissions')
}

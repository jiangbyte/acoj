import { request } from '../http'

export function fetchCaptcha() {
  return request.Get('/public/b/captcha')
}

export function fetchLogin(data: any) {
  return request.Post('/public/b/login', data)
}

export function fetchRegister(data: any) {
  return request.Post('/public/b/register', data)
}

export function fetchLogout() {
  return request.Post('/b/logout')
}

export function fetchCurrentUser() {
  return request.Get('/sys/user/current')
}

export function fetchUserMenus() {
  return request.Get('/sys/user/menus')
}

export function fetchUserPermissions() {
  return request.Get('/sys/user/permissions')
}

import { removeStorage } from './storage'

export const tokenKey = 'admin_token'
export const userInfoKey = 'admin_user_info'

const clearListeners = new Set<() => void>()

export function getToken() {
  const token = uni.getStorageSync(tokenKey)
  return token ? String(token) : ''
}

export function setToken(token: string) {
  uni.setStorageSync(tokenKey, token)
}

export function clearSessionStorage() {
  removeStorage(tokenKey)
  removeStorage(userInfoKey)
  clearListeners.forEach((listener) => listener())
}

export function onSessionCleared(listener: () => void) {
  clearListeners.add(listener)
  return () => clearListeners.delete(listener)
}

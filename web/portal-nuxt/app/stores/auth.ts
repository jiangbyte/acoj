import { defineStore } from 'pinia'
import * as authApi from '~/api/auth'
import { encryptPassword } from '~/utils/crypto'

interface AuthUserInfo {
  accountId: string
  account: string
  accountType: string
  name?: string | null
  nickname?: string | null
  avatar?: string | null
  email?: string | null
  signature?: string | null
}

export const useAuthStore = defineStore('auth', () => {
  const tokenCookie = useCookie<string | null>('token', { default: () => null })
  const userInfo = ref<AuthUserInfo | null>(null)

  // 从 localStorage 恢复用户信息
  if (import.meta.client) {
    try {
      const stored = localStorage.getItem('hei-user-info')
      if (stored) userInfo.value = JSON.parse(stored)
    } catch {
      localStorage.removeItem('hei-user-info')
    }
  }

  const isLogin = computed(() => Boolean(tokenCookie.value))
  const token = computed(() => tokenCookie.value ?? '')

  function persistUserInfo() {
    if (userInfo.value) {
      localStorage.setItem('hei-user-info', JSON.stringify(userInfo.value))
    } else {
      localStorage.removeItem('hei-user-info')
    }
  }

  async function login(account: string, password: string, captchaId: string, captchaValue: string) {
    const { data: key } = await authApi.passwordKey()
    const encrypted = await encryptPassword(password, {
      key_id: key.key_id,
      public_key: key.public_key,
    })

    const { data } = await authApi.login({
      account,
      password: encrypted.encrypted,
      password_key_id: encrypted.password_key_id,
      captcha_id: captchaId,
      captcha_value: captchaValue,
    })

    tokenCookie.value = data.token

    // 登录成功后获取用户信息
    await refreshUserInfo()

    return data
  }

  async function register(
    account: string,
    email: string,
    password: string,
    nickname: string,
    captchaId: string,
    captchaValue: string,
  ) {
    const { data: key } = await authApi.passwordKey()
    const encrypted = await encryptPassword(password, {
      key_id: key.key_id,
      public_key: key.public_key,
    })

    await authApi.register({
      account,
      email,
      password: encrypted.encrypted,
      password_key_id: encrypted.password_key_id,
      captcha_id: captchaId,
      captcha_value: captchaValue,
      nickname,
    })
  }

  async function refreshUserInfo() {
    const { data } = await authApi.me()
    userInfo.value = {
      accountId: data.account_id,
      account: data.account,
      accountType: data.account_type,
      name: data.name ?? null,
      nickname: data.nickname ?? null,
      avatar: data.avatar ?? null,
      email: data.profile?.email ?? null,
      signature: data.profile?.signature ?? null,
    }
    persistUserInfo()
  }

  function clearAuthStorage() {
    tokenCookie.value = null
    userInfo.value = null
    persistUserInfo()
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // 后端登出失败不阻塞本地清理
    } finally {
      clearAuthStorage()
    }
  }

  async function update(data: Partial<AuthUserInfo>) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...data }
      persistUserInfo()
    }
  }

  return {
    token,
    userInfo,
    isLogin,
    login,
    register,
    refreshUserInfo,
    clearAuthStorage,
    logout,
    update,
  }
})

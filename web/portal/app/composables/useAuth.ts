import { useAuthStore } from '~/stores/auth'

export interface User {
  id: string
  username: string
  email: string
  nickname?: string
}

export function useAuth() {
  const store = useAuthStore()

  const user = computed<User | null>(() => {
    if (!store.userInfo) return null
    return {
      id: store.userInfo.accountId,
      username: store.userInfo.account,
      email: store.userInfo.email ?? '',
      nickname: store.userInfo.nickname ?? undefined,
    }
  })

  const isLoggedIn = computed(() => store.isLogin)

  function login(
    account: string,
    password: string,
    captchaId: string,
    captchaValue: string,
  ): Promise<void> {
    return store.login(account, password, captchaId, captchaValue) as unknown as Promise<void>
  }

  function register(
    account: string,
    email: string,
    password: string,
    nickname: string,
    captchaId: string,
    captchaValue: string,
  ): Promise<{ account_id: string; account: string }> {
    return store.register(
      account,
      email,
      password,
      nickname,
      captchaId,
      captchaValue,
    ) as unknown as Promise<{ account_id: string; account: string }>
  }

  function logout() {
    return store.logout()
  }

  return {
    user,
    isLoggedIn,
    login,
    register,
    logout,
  }
}

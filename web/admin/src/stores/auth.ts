import { defineStore } from 'pinia'
import { router } from '@/router'
import { useRouteStore } from './route'
import { useTabStore } from './tab'

interface AuthUserInfo {
  account: string
  nickname: string
  loginAt: number
}

interface AuthState {
  token: string
  userInfo: AuthUserInfo | null
}

const tokenKey = 'token'
const userInfoKey = 'userInfo'
const loginPath = '/auth/login'

function getStoredUserInfo() {
  const raw = localStorage.getItem(userInfoKey)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw) as AuthUserInfo
  } catch {
    localStorage.removeItem(userInfoKey)
    return null
  }
}

function getSafeRedirect(redirect?: string) {
  if (!redirect || redirect.startsWith('/auth')) {
    return import.meta.env.VITE_HOME_PATH
  }
  return redirect
}

export const useAuthStore = defineStore('auth-store', {
  state: (): AuthState => ({
    token: localStorage.getItem(tokenKey) ?? '',
    userInfo: getStoredUserInfo(),
  }),
  getters: {
    isLogin: (state) => Boolean(state.token),
  },
  actions: {
    async mockLogin(account: string, _password: string, redirect?: string) {
      const now = Date.now()
      const userInfo: AuthUserInfo = {
        account,
        nickname: account,
        loginAt: now,
      }
      const token = `mock-token-${now}`

      localStorage.setItem(tokenKey, token)
      localStorage.setItem(userInfoKey, JSON.stringify(userInfo))
      this.token = token
      this.userInfo = userInfo

      const routeStore = useRouteStore()
      await routeStore.initAuthRoute()
      await router.push(getSafeRedirect(redirect))
    },

    clearAuthStorage() {
      localStorage.removeItem(tokenKey)
      localStorage.removeItem(userInfoKey)
      this.token = ''
      this.userInfo = null
    },

    resetSession() {
      this.clearAuthStorage()

      const routeStore = useRouteStore()
      routeStore.resetRouteStore()

      const tabStore = useTabStore()
      tabStore.clearAllTabs()
    },

    async logout(redirect?: string) {
      const currentRoute = router.currentRoute.value
      const finalRedirect = redirect ?? currentRoute.fullPath

      this.resetSession()

      await router.push({
        path: loginPath,
        query: finalRedirect.startsWith('/auth') ? undefined : { redirect: finalRedirect },
      })
    },
  },
})

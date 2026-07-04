import { defineStore } from 'pinia'
import { router } from '@/router'
import { authApi } from '@/api'
import { useDictStore } from './dict'
import { useRouteStore } from './route'
import { useTabStore } from './tab'

interface AuthUserInfo {
  accountId: string
  account: string
  accountType: string
  name?: string | null
  nickname?: string | null
  avatar?: string | null
  roleIds: string[]
  deptIds: string[]
  groupIds: string[]
  roleIdNames?: { id: string; name: string }[]
  deptIdNames?: { id: string; name: string }[]
  groupIdNames?: { id: string; name: string }[]
  permissionKeys: string[]
  buttonCodes: string[]
  profile?: Record<string, unknown> | null
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
    async login(
      account: string,
      password: string,
      redirect?: string,
      identityType = 'ACCOUNT',
      security?: { password_key_id: string; captcha_id: string; captcha_value: string },
    ) {
      const response = await authApi.login({
        account,
        password,
        identity_type: identityType,
        password_key_id: security?.password_key_id,
        captcha_id: security?.captcha_id,
        captcha_value: security?.captcha_value,
      })
      const token = response.data.token
      localStorage.setItem(tokenKey, token)
      this.token = token

      const meResponse = await authApi.me()
      const now = Date.now()
      const userInfo: AuthUserInfo = {
        accountId: meResponse.data.account_id,
        account: meResponse.data.account,
        accountType: meResponse.data.account_type,
        name: meResponse.data.name,
        nickname: meResponse.data.nickname,
        avatar: meResponse.data.avatar,
        roleIds: meResponse.data.role_ids ?? [],
        deptIds: meResponse.data.dept_ids ?? [],
        groupIds: meResponse.data.group_ids ?? [],
        roleIdNames: meResponse.data.role_id_names ?? [],
        deptIdNames: meResponse.data.dept_id_names ?? [],
        groupIdNames: meResponse.data.group_id_names ?? [],
        permissionKeys: meResponse.data.permission_keys ?? [],
        buttonCodes: meResponse.data.button_codes ?? [],
        profile: meResponse.data.profile ?? null,
        loginAt: now,
      }

      localStorage.setItem(userInfoKey, JSON.stringify(userInfo))
      this.userInfo = userInfo

      const routeStore = useRouteStore()
      await routeStore.initAuthRoute()
      const dictStore = useDictStore()
      dictStore.syncDictTree()
      await dictStore.refreshDict()
      await router.push(getSafeRedirect(redirect))
    },

    async refreshUserInfo() {
      const meResponse = await authApi.me()
      const userInfo: AuthUserInfo = {
        ...(this.userInfo ?? { loginAt: Date.now() }),
        accountId: meResponse.data.account_id,
        account: meResponse.data.account,
        accountType: meResponse.data.account_type,
        name: meResponse.data.name,
        nickname: meResponse.data.nickname,
        avatar: meResponse.data.avatar,
        roleIds: meResponse.data.role_ids ?? [],
        deptIds: meResponse.data.dept_ids ?? [],
        groupIds: meResponse.data.group_ids ?? [],
        roleIdNames: meResponse.data.role_id_names ?? [],
        deptIdNames: meResponse.data.dept_id_names ?? [],
        groupIdNames: meResponse.data.group_id_names ?? [],
        permissionKeys: meResponse.data.permission_keys ?? [],
        buttonCodes: meResponse.data.button_codes ?? [],
        profile: meResponse.data.profile ?? null,
      }

      localStorage.setItem(userInfoKey, JSON.stringify(userInfo))
      this.userInfo = userInfo
      return meResponse.data
    },

    hasPermission(permissionKey: string) {
      const keys = this.userInfo?.permissionKeys ?? []
      const buttonCodes = this.userInfo?.buttonCodes ?? []
      return (
        keys.includes('*:*:*') ||
        keys.includes(permissionKey) ||
        buttonCodes.includes(permissionKey)
      )
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

      const dictStore = useDictStore()
      dictStore.clearDict()
    },

    async logout(redirect?: string) {
      const currentRoute = router.currentRoute.value
      const finalRedirect = redirect ?? currentRoute.fullPath

      try {
        await authApi.logout()
      } catch {
        // 后端登出失败不阻塞本地会话清理。
      } finally {
        this.resetSession()
      }

      await router.push({
        path: loginPath,
        query: finalRedirect.startsWith('/auth') ? undefined : { redirect: finalRedirect },
      })
    },
  },
})

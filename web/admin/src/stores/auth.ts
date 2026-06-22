import { defineStore } from 'pinia'
import { message } from 'ant-design-vue'

import { getMe, login, logout } from '@/apis/auth'
import type { LoginPayload } from '@/apis/auth'
import { useAppStore } from '@/stores/app'
import { useRouteStore } from '@/stores/route'
import { useUserStore } from '@/stores/user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    rememberAccount: '',
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    async login(payload: LoginPayload) {
      const loginResult = await login(payload)
      const loginData = loginResult?.data
      if (!loginResult?.isSuccess || !loginData) {
        throw new Error(loginResult?.message || '登录失败')
      }

      this.token = loginData.token
      this.rememberAccount = payload.remember ? payload.account : ''
      const meResult = await getMe()
      const me = meResult?.data
      if (!meResult?.isSuccess || !me) {
        this.clearSession()
        throw new Error(meResult?.message || '获取用户信息失败')
      }

      const user = useUserStore()
      user.setMe({
        ...me,
        permissions: me.permissions ?? [],
      })
    },
    async logout() {
      const result = await logout()
      if (!result?.isSuccess) {
        message.warning(result?.message || '退出登录请求失败，已清理本地登录状态')
      }
      this.clearSession()
      useUserStore().clear()
      useRouteStore().reset_route_store()
      useAppStore().clearAllTabs()
    },
    clearSession() {
      this.token = ''
    },
  },
  persist: {
    key: 'auth',
    pick: ['token', 'rememberAccount'],
  },
})

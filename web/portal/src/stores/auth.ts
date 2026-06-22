import { defineStore } from 'pinia'

import { getMe, login, logout, type LoginPayload } from '@/apis/auth'
import { useUserStore } from '@/stores/user'

export const useAuthStore = defineStore('portal-auth', {
  state: () => ({
    token: '',
    rememberAccount: '',
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
  },
  actions: {
    async login(payload: LoginPayload) {
      const loginData = await login(payload)
      this.token = loginData.token
      this.rememberAccount = payload.remember ? payload.account : ''

      const me = await getMe()
      useUserStore().setMe(me)
    },
    async logout() {
      try {
        await logout()
      } catch {
        // Local state should be cleared even when the remote session is already invalid.
      } finally {
        this.clearSession()
        useUserStore().clear()
      }
    },
    clearSession() {
      this.token = ''
    },
  },
  persist: {
    key: 'hei-portal-auth',
    pick: ['token', 'rememberAccount'],
  },
})

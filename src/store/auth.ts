import { defineStore } from 'pinia'
import {
  fetchLogin,
  fetchCurrentUser,
  fetchUserMenus,
  fetchUserPermissions,
  fetchLogout,
  fetchSm2PublicKey,
} from '@/api/auth'
import { sm2 } from 'sm-crypto'
import { useRouteStore } from './route'

let _permPollTimer: ReturnType<typeof setInterval> | null = null

interface AuthState {
  token: string
  userInfo: any
  permissions: string[]
  sm2PublicKey: string
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: '',
    userInfo: null,
    permissions: [],
    sm2PublicKey: '',
  }),
  getters: {
    isLogin: state => !!state.token,
    hasPermission: state => (code: string) => state.permissions.includes(code),
  },
  actions: {
    encryptPassword(password: string) {
      if (!this.sm2PublicKey) return password
      const publicKey = this.sm2PublicKey
      return sm2.doEncrypt(password, publicKey, 1)
    },
    async fetchSm2PublicKey() {
      const { data } = await fetchSm2PublicKey()
      if (data) this.sm2PublicKey = data
    },
    async login(username: string, password: string, captchaCode?: string, captchaId?: string) {
      const encryptedPwd = this.encryptPassword(password)
      const { success, data } = await fetchLogin({
        username,
        password: encryptedPwd,
        captcha_code: captchaCode,
        captcha_id: captchaId,
      })
      if (!success) return false
      this.token = data.token
      await this.loadUserInfo()
      this.startPermissionPolling()
      return true
    },
    async loadUserInfo() {
      await Promise.all([this.fetchUserInfo(), this.loadMenusAndPermissions()])
    },
    async fetchUserInfo() {
      const { data } = await fetchCurrentUser()
      if (data) this.userInfo = data
    },
    async loadMenusAndPermissions() {
      const routeStore = useRouteStore()
      const [menusRes, permsRes] = await Promise.all([fetchUserMenus(), fetchUserPermissions()])
      if (menusRes.data) {
        routeStore.setMenus(menusRes.data)
        await routeStore.initAuthRoute()
      }
      if (permsRes.data) this.permissions = permsRes.data
    },
    async refreshPermissions() {
      const { data } = await fetchUserPermissions()
      if (data) this.permissions = data
    },
    startPermissionPolling(intervalMs = 5 * 60 * 1000) {
      this.stopPermissionPolling()
      _permPollTimer = setInterval(() => this.refreshPermissions(), intervalMs)
    },
    stopPermissionPolling() {
      if (_permPollTimer !== null) {
        clearInterval(_permPollTimer)
        _permPollTimer = null
      }
    },
    async logout() {
      await fetchLogout().catch(() => {})
      this.token = ''
      this.userInfo = null
      this.permissions = []
      this.stopPermissionPolling()
      const routeStore = useRouteStore()
      routeStore.reset()
    },
  },
  persist: true,
})

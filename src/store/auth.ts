import { defineStore } from 'pinia'
import { fetchLogin, fetchCurrentUser, fetchUserMenus, fetchUserPermissions, fetchLogout } from '@/service/api/auth'
import { sm2 } from 'sm-crypto'
import { useRouteStore } from './route'

interface AuthState {
  token: string
  userInfo: any
  permissions: string[]
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: '',
    userInfo: null,
    permissions: [],
  }),
  getters: {
    isLogin: (state) => !!state.token,
    hasPermission: (state) => (code: string) => state.permissions.includes(code),
  },
  actions: {
    encryptPassword(password: string) {
      const publicKey = import.meta.env.VITE_SM2_PUBLIC_KEY as string
      if (!publicKey) return password
      return sm2.doEncrypt(password, publicKey, 1)
    },
    async login(username: string, password: string, captchaCode?: string, captchaId?: string) {
      const encryptedPwd = this.encryptPassword(password)
      const { isSuccess, data } = await fetchLogin({
        username,
        password: encryptedPwd,
        captcha_code: captchaCode,
        captcha_id: captchaId,
      })
      if (!isSuccess) return false
      this.token = data.token
      await this.loadUserInfo()
      return true
    },
    async loadUserInfo() {
      await Promise.all([
        this.fetchUserInfo(),
        this.loadMenusAndPermissions(),
      ])
    },
    async fetchUserInfo() {
      const { data } = await fetchCurrentUser()
      if (data) this.userInfo = data
    },
    async loadMenusAndPermissions() {
      const routeStore = useRouteStore()
      const [menusRes, permsRes] = await Promise.all([fetchUserMenus(), fetchUserPermissions()])
      if (menusRes.data) routeStore.setMenus(menusRes.data)
      if (permsRes.data) this.permissions = permsRes.data
    },
    async logout() {
      await fetchLogout().catch(() => {})
      this.token = ''
      this.userInfo = null
      this.permissions = []
      const routeStore = useRouteStore()
      routeStore.reset()
    },
  },
  persist: true,
})

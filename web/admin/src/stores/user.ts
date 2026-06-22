import { defineStore } from 'pinia'

import { getMe } from '@/apis/auth'
import type { AdminMe } from '@/apis/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    me: null as AdminMe | null,
  }),
  getters: {
    profile: (state) => state.me?.profile,
    permissions: (state) => state.me?.permissions ?? [],
  },
  actions: {
    setMe(me: AdminMe) {
      this.me = me
    },
    clear() {
      this.me = null
    },
    async ensureMe() {
      if (!this.me) {
        const result = await getMe()
        const me = result?.data
        if (!result?.isSuccess || !me) {
          throw new Error(result?.message || '获取用户信息失败')
        }
        this.me = {
          ...me,
          permissions: me.permissions ?? [],
        }
      }
      return this.me
    },
  },
})

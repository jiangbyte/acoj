import { defineStore } from 'pinia'

import { getMe, type PortalMe } from '@/apis/auth'

export const useUserStore = defineStore('portal-user', {
  state: () => ({
    me: null as PortalMe | null,
  }),
  getters: {
    profile: (state) => state.me?.profile,
  },
  actions: {
    setMe(me: PortalMe) {
      this.me = me
    },
    clear() {
      this.me = null
    },
    async ensureMe() {
      if (!this.me) {
        this.me = await getMe()
      }
      return this.me
    },
  },
})

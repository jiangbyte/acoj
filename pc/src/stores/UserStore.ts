export const useUserStore = defineStore('userStore', {
  state: () => {
    return {
      id: null as string | null,
    }
  },
  getters: {
    getUserId(state): string | null {
      return state.id
    },
  },
  actions: {
    async setUserId(id: string) {
      this.id = id
    },

    async resetToken() {
      this.$reset()
    },
  },
  persist: {
    storage: localStorage,
  },
})

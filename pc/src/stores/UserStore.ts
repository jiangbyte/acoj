export const useUserStore = defineStore('userStore', {
  state: () => {
    return {
      id: '',
    }
  },
  getters: {
    getUserId(state): string {
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

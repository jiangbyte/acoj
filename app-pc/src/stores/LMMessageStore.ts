export const useLMMessageStore = defineStore('LMMessageStore', {
  state: () => {
    return {
      messages: [],
    }
  },
  getters: {
    getMessages(state): any[] {
      return state.messages
    },
  },
  actions: {
    async setMessages(messages: any) {
      this.messages = messages
    },
  },
  persist: {
    storage: localStorage,
  },
})

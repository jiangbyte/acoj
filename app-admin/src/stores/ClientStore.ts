interface ClientStoreState {
  clientId: string | null
  createdAt: number | null // 时间戳
  expiresAt: number | null // 过期时间戳
}

// 客户端ID的有效期，这里设置为1小时(3600000毫秒)
const CLIENT_ID_EXPIRY = 3600000

function generateClientId(): string {
  // 生成UUID v4格式的唯一ID
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

export const useClientStore = defineStore('clientStore', {
  state: (): ClientStoreState => {
    return {
      clientId: null,
      createdAt: null,
      expiresAt: null,
    }
  },
  getters: {},
  actions: {
    initializeClientId() {
      // 检查是否存在有效的客户端ID
      if (!this.clientId || !this.expiresAt || Date.now() > this.expiresAt) {
        // 生成新的客户端ID
        this.clientId = generateClientId()
        this.createdAt = Date.now()
        this.expiresAt = Date.now() + CLIENT_ID_EXPIRY

        // 设置定时器，在过期时自动刷新
        const timeToExpiry = this.expiresAt - Date.now()
        setTimeout(() => {
          this.refreshClientId()
        }, timeToExpiry)
      }
      else {
        // 已存在有效ID，设置剩余时间的定时器
        const timeToExpiry = this.expiresAt - Date.now()
        setTimeout(() => {
          this.refreshClientId()
        }, timeToExpiry)
      }
    },

    refreshClientId() {
      // 刷新客户端ID
      this.clientId = generateClientId()
      this.createdAt = Date.now()
      this.expiresAt = Date.now() + CLIENT_ID_EXPIRY

      // 重新设置定时器
      setTimeout(() => {
        this.refreshClientId()
      }, CLIENT_ID_EXPIRY)
    },
  },
  persist: {
    storage: localStorage, // 使用localStorage而不是sessionStorage，确保页面关闭后仍能保留
  },
})

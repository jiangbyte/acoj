interface ClientStoreState {
  clientId: string | null
  createdAt: number | null // Timestamp
  expiresAt: number | null // Expiration timestamp
}

// Client ID validity period, set to one hour (3,600,000 ms)
const CLIENT_ID_EXPIRY = 3600000

function generateClientId(): string {
  // Generate a unique UUID v4
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
      // Check for a valid client ID
      if (!this.clientId || !this.expiresAt || Date.now() > this.expiresAt) {
        // Generate a new client ID
        this.clientId = generateClientId()
        this.createdAt = Date.now()
        this.expiresAt = Date.now() + CLIENT_ID_EXPIRY

        // Schedule an automatic refresh at expiration
        const timeToExpiry = this.expiresAt - Date.now()
        setTimeout(() => {
          this.refreshClientId()
        }, timeToExpiry)
      }
      else {
        // A valid ID exists; schedule a timer for the remaining duration
        const timeToExpiry = this.expiresAt - Date.now()
        setTimeout(() => {
          this.refreshClientId()
        }, timeToExpiry)
      }
    },

    refreshClientId() {
      // Refresh the client ID
      this.clientId = generateClientId()
      this.createdAt = Date.now()
      this.expiresAt = Date.now() + CLIENT_ID_EXPIRY

      // Reset the timer
      setTimeout(() => {
        this.refreshClientId()
      }, CLIENT_ID_EXPIRY)
    },
  },
  persist: {
    storage: localStorage, // Use localStorage instead of sessionStorage so the value persists after closing the page
  },
})

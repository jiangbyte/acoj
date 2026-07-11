import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface TokenState {
  accessToken: string | null
  refreshToken: string | null
}

export const useTokenStore = defineStore('token', () => {
  const accessToken = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)

  function setTokens(payload: Partial<TokenState>) {
    if (payload.accessToken !== undefined) {
      accessToken.value = payload.accessToken
    }
    if (payload.refreshToken !== undefined) {
      refreshToken.value = payload.refreshToken
    }
  }

  function clearTokens() {
    accessToken.value = null
    refreshToken.value = null
  }

  function getAccessToken(): string | null {
    return accessToken.value
  }

  function getRefreshToken(): string | null {
    return refreshToken.value
  }

  function isAuthenticated(): boolean {
    return !!accessToken.value
  }

  return {
    accessToken,
    refreshToken,
    setTokens,
    clearTokens,
    getAccessToken,
    getRefreshToken,
    isAuthenticated,
  }
})

import { createAxiosInstance } from '@hei/shared'

export const request = createAxiosInstance({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  hooks: {
    getToken: () => {
      const raw = localStorage.getItem('hei-portal-auth')
      if (!raw) {
        return ''
      }
      try {
        const data = JSON.parse(raw) as { token?: string }
        return data.token || ''
      } catch {
        return ''
      }
    },
    onAuthExpired: () => {
      localStorage.removeItem('hei-portal-auth')
      window.dispatchEvent(new CustomEvent('portal-auth-expired'))
    },
  },
})

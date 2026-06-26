import { createHttp } from './axios'

export const http = createHttp({
  baseURL: import.meta.env.VITE_API_URL,
})

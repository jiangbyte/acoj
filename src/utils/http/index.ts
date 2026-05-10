import { createAlovaInstance } from './alova'

const baseURL = (import.meta.env.VITE_API_BASE_URL as string) || ''

export const request = createAlovaInstance(baseURL)

/** 无baseURL实例，适用于外部链接或blob下载 */
export const blankInstance = createAlovaInstance('')

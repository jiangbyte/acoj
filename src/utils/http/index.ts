import { createDefaultAlovaInstance, createUploadAlovaInstance } from './alova'

const baseURL = (import.meta.env.VITE_API_BASE_URL as string) || ''

export const request = createDefaultAlovaInstance(baseURL)

/** 上传专用实例，使用 XHR  adapter 以支持上传进度 */
export const uploadRequest = createUploadAlovaInstance(baseURL)

/** 无baseURL实例，适用于外部链接或blob下载 */
export const blankInstance = createDefaultAlovaInstance('')

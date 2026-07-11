import { http } from '@/utils/request'

export function uploadFile(url: string, filePath: string, name = 'file') {
  return new Promise<any>((resolve, reject) => {
    const token = uni.getStorageSync('portal_token')
    uni.uploadFile({
      url: `${(import.meta.env.VITE_API_URL || '').replace(/\/$/, '')}${url}`,
      filePath,
      name,
      header: token ? { Authorization: String(token) } : {},
      success(response) {
        try {
          const body = JSON.parse(response.data)
          if (body.code !== 200 && body.code !== 0) {
            reject(new Error(body.message || '上传失败'))
            return
          }
          resolve(body.data)
        } catch (error) {
          reject(error)
        }
      },
      fail: reject,
    })
  })
}

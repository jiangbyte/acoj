import { http } from '@/utils/request'

const adminPrefix = '/api/v1/admin'
const prefix = `${adminPrefix}/sys/file`

export function page(params?: any) {
  return http.get<any>(`${prefix}/page`, params)
}

export function detail(params: any) {
  return http.get<any>(`${prefix}/detail`, params)
}

export function update(data: any) {
  return http.post<any>(`${prefix}/update`, data)
}

export function remove(data: any) {
  return http.post<any>(`${prefix}/delete`, data)
}

export function upload(filePath: string) {
  return uploadFile('/sys/file/upload', filePath)
}

export function uploadFile(url: string, filePath: string, name = 'file') {
  return new Promise<any>((resolve, reject) => {
    const token = uni.getStorageSync('admin_token')
    uni.uploadFile({
      url: `${(import.meta.env.VITE_API_URL || '').replace(/\/$/, '')}${adminPrefix}${url}`,
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

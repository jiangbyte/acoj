import { http } from '@/utils'

const bannerPrefix = '/api/v1/portal/sys/banners'

export function list(params: any) {
  return http.get<any>(`${bannerPrefix}/list`, { params })
}

export function interaction(data: any) {
  return http.post<any>(`${bannerPrefix}/interaction`, data)
}

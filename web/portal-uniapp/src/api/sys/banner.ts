import { http } from '@/utils/request'

const prefix = '/api/v1/portal/sys/banners'

export function list(params: any) {
  return http.get<any[]>(`${prefix}/list`, params, { addToken: false })
}

export function interaction(data: any) {
  return http.post<any>(`${prefix}/interaction`, data, { addToken: false })
}

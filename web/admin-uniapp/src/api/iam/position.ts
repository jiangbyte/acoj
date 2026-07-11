import { http } from '@/utils/request'

const prefix = '/api/v1/admin/sys/positions'

export function page(params?: any) {
  return http.get<any>(`${prefix}/page`, params)
}

export function detail(params: any) {
  return http.get<any>(`${prefix}/detail`, params)
}

export function create(data: any) {
  return http.post<any>(`${prefix}/create`, data)
}

export function update(data: any) {
  return http.post<any>(`${prefix}/update`, data)
}

export function remove(data: any) {
  return http.post<any>(`${prefix}/delete`, data)
}

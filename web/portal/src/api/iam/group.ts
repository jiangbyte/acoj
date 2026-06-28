import { http } from '@/utils'

const groupPrefix = '/api/v1/admin/sys/groups'

export function page(params: any) {
  return http.get<any>(`${groupPrefix}/page`, { params })
}

export function detail(params: any) {
  return http.get<any>(`${groupPrefix}/detail`, { params })
}

export function create(data: any) {
  return http.post<any>(`${groupPrefix}/create`, data)
}

export function update(data: any) {
  return http.post<any>(`${groupPrefix}/update`, data)
}

export function remove(data: any) {
  return http.post<any>(`${groupPrefix}/delete`, data)
}

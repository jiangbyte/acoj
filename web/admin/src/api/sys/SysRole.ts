import { http } from '@/utils'

const rolePrefix = '/api/v1/admin/sys/roles'

export function page(params: any) {
  return http.get<any>(`${rolePrefix}/page`, { params })
}

export function detail(params: any) {
  return http.get<any>(`${rolePrefix}/detail`, { params })
}

export function create(data: any) {
  return http.post<any>(`${rolePrefix}/create`, data)
}

export function update(data: any) {
  return http.post<any>(`${rolePrefix}/update`, data)
}

export function remove(data: any) {
  return http.post<any>(`${rolePrefix}/delete`, data)
}

import { http } from '@/utils'

const prefix = '/api/v1/admin/biz/clazz'

export function page(params: any) {
  return http.get<any>(`${prefix}/page`, { params })
}

export function detail(params: { id: string }) {
  return http.get<any>(`${prefix}/detail`, { params })
}

export function create(data: any) {
  return http.post<any>(`${prefix}/create`, data)
}

export function update(data: any) {
  return http.post<any>(`${prefix}/update`, data)
}

export function remove(data: { ids: string[] }) {
  return http.post<any>(`${prefix}/delete`, data)
}

export function members(params: { class_id: string }) {
  return http.get<any>(`${prefix}/members`, { params })
}

export function memberAdd(data: { class_id: string, account_ids: string[], role?: string }) {
  return http.post<any>(`${prefix}/member/add`, data)
}

export function memberRemove(data: { class_id: string, account_id: string }) {
  return http.post<any>(`${prefix}/member/remove`, data)
}

export function refreshInvite(data: { id: string }) {
  return http.post<any>(`${prefix}/invite/refresh`, data)
}

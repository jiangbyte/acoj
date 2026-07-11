import { http } from '@/utils/request'

const prefix = '/api/v1/admin/sys/groups'

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

export function ownUser(id: string) {
  return http.get<any>(`${prefix}/own-user`, { id })
}

export function grantUser(data: any) {
  return http.post<any>(`${prefix}/grant-user`, data)
}

export function ownRole(id: string) {
  return http.get<any>(`${prefix}/own-role`, { id })
}

export function grantRole(data: any) {
  return http.post<any>(`${prefix}/grant-role`, data)
}

export function ownResource(id: string) {
  return http.get<any>(`${prefix}/own-resource`, { id })
}

export function grantResource(data: any) {
  return http.post<any>(`${prefix}/grant-resource`, data)
}

export function ownPermission(id: string) {
  return http.get<any>(`${prefix}/own-permission-detail`, { id })
}

export function grantPermission(data: any) {
  return http.post<any>(`${prefix}/grant-permission`, data)
}

import { http } from '@/utils/request'

const prefix = '/api/v1/admin/sys/accounts'

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

export function ownRole(id: string) {
  return http.get<any>(`${prefix}/own-role`, { id })
}

export function grantRole(data: any) {
  return http.post<any>(`${prefix}/grant-role`, data)
}

export function ownGroup(id: string) {
  return http.get<any>(`${prefix}/own-group`, { id })
}

export function grantGroup(data: any) {
  return http.post<any>(`${prefix}/grant-group`, data)
}

export function ownDept(id: string) {
  return http.get<any>(`${prefix}/own-dept`, { id })
}

export function grantDept(data: any) {
  return http.post<any>(`${prefix}/grant-dept`, data)
}

export function ownResource(id: string) {
  return http.get<any>(`${prefix}/own-resource`, { id })
}

export function grantResource(data: any) {
  return http.post<any>(`${prefix}/grant-resource`, data)
}

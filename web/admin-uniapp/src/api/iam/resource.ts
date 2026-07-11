import { http } from '@/utils/request'

const prefix = '/api/v1/admin/sys/resources'

export function current() {
  return http.get<any[]>(`${prefix}/current`)
}

export function page(params?: any) {
  return http.get<any>(`${prefix}/page`, params)
}

export function tree(params?: any) {
  return http.get<any[]>(`${prefix}/tree`, params)
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

export function permissionRegistry() {
  return http.get<any[]>('/api/v1/admin/permission-registry')
}

export function modules(params?: any) {
  return http.get<any[]>('/api/v1/admin/sys/resource-modules/selector', params)
}

export function bindPermission(data: any) {
  return http.post<any>('/api/v1/admin/resource-permissions', data)
}

export function buttonPage(params?: any) {
  return http.get<any>('/api/v1/admin/sys/resource-buttons/page', params)
}

export function buttonCreate(data: any) {
  return http.post<any>('/api/v1/admin/sys/resource-buttons/create', data)
}

export function buttonUpdate(data: any) {
  return http.post<any>('/api/v1/admin/sys/resource-buttons/update', data)
}

export function buttonRemove(data: any) {
  return http.post<any>('/api/v1/admin/sys/resource-buttons/delete', data)
}

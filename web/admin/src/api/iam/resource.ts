import { http } from '@/utils'

const resourcePrefix = '/api/v1/admin/sys/resources'

export function page(params: any) {
  return http.get<any>(`${resourcePrefix}/page`, { params })
}

export function tree(params?: any) {
  return http.get<any>(`${resourcePrefix}/tree`, { params })
}

export function detail(params: any) {
  return http.get<any>(`${resourcePrefix}/detail`, { params })
}

export function create(data: any) {
  return http.post<any>(`${resourcePrefix}/create`, data)
}

export function update(data: any) {
  return http.post<any>(`${resourcePrefix}/update`, data)
}

export function remove(data: any) {
  return http.post<any>(`${resourcePrefix}/delete`, data)
}

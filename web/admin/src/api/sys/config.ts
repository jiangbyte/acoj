import { http } from '@/utils'

const configPrefix = '/api/v1/admin/sys/config'

export function page(params: any) {
  return http.get<any>(`${configPrefix}/page`, {
    params,
  })
}

export function detail(params: any) {
  return http.get<any>(`${configPrefix}/detail`, {
    params,
  })
}

export function create(data: any) {
  return http.post<any>(`${configPrefix}/create`, data)
}

export function update(data: any) {
  return http.post<any>(`${configPrefix}/update`, data)
}

export function remove(data: any) {
  return http.post<any>(`${configPrefix}/delete`, data)
}

import { http } from '@/utils'

const accountPrefix = '/api/v1/admin/sys/accounts'

export function page(params: any) {
  return http.get<any>(`${accountPrefix}/page`, {
    params,
  })
}

export function detail(params: any) {
  return http.get<any>(`${accountPrefix}/detail`, {
    params,
  })
}

export function create(data: any) {
  return http.post<any>(`${accountPrefix}/create`, data)
}

export function update(data: any) {
  return http.post<any>(`${accountPrefix}/update`, data)
}

export function remove(data: any) {
  return http.post<any>(`${accountPrefix}/delete`, data)
}

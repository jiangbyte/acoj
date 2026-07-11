import { http } from '@/utils/request'

const prefix = '/api/v1/admin/auth/sessions'

export function analysis() {
  return http.get<any>(`${prefix}/analysis`)
}

export function page(params?: any) {
  return http.get<any>(`${prefix}/page`, params)
}

export function detail(params: any) {
  return http.get<any[]>(`${prefix}/tokens`, params)
}

export function remove(data: any) {
  return http.post<any>(
    `${prefix}/exit`,
    data.targets ? data : { targets: data.ids ?? [] }
  )
}

export function tokenExit(data: any) {
  return http.post<any>(
    `${prefix}/token/exit`,
    data.tokens ? data : { tokens: data.ids ?? [] }
  )
}

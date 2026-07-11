import { http } from '@/utils/request'

const prefix = '/api/v1/portal/sys/dicts'

export function tree(params?: any) {
  return http.get<any[]>(`${prefix}/tree`, params, { addToken: false })
}

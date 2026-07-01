import { http } from '@/utils'

const dictPrefix = '/api/v1/portal/sys/dicts'

export function tree(params?: any) {
  return http.get<any>(`${dictPrefix}/tree`, { params })
}

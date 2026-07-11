import { http } from '@/utils/request'

const prefix = '/api/v1/admin/dashboard'

export function overview() {
  return http.get<any>(`${prefix}/overview`)
}

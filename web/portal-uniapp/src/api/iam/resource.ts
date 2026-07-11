import { http } from '@/utils/request'

const prefix = '/api/v1/portal/sys/resources'

export function current() {
  return http.get<any[]>(`${prefix}/current`, undefined, { addToken: false })
}

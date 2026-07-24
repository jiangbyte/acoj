import { http } from '@/utils'

const resourcePrefix = '/api/v1/portal/sys/resources'

export function current() {
  return http.get<any>(`${resourcePrefix}/current`)
}

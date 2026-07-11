import { http } from '@/utils/request'

const prefix = '/api/v1/portal/spaces'

export function detail(accountId: string) {
  return http.get<any>(`${prefix}/${accountId}`, undefined, { addToken: false })
}

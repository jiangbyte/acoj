import { http } from '@/utils'

const spacePrefix = '/api/v1/portal/spaces'

export function detail(accountId: string) {
  return http.get<any>(`${spacePrefix}/${accountId}`, {
    addToken: false,
  })
}

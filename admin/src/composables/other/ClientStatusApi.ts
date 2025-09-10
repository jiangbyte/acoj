import { request as $alova } from '@/utils'

const pathPrefix = '/core/api/v1/'
const table = 'activities'

export function useClientStatusFetch() {
  return {
    serverConnect(clientId: string) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/connect/${clientId}`)
    },
    serverHeartbeat(clientId: string) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/heartbeat/${clientId}`)
    },
  }
}

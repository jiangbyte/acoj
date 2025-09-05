import { request as $alova } from '@/utils'

const pathPrefix = '/core/api/v1/'
const table = 'sys/monitor'

export function useMonitorFetch() {
  return {
    healthStatus() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/health/status`)
    },
  }
}

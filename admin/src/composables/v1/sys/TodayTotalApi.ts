import { request as $alova } from '@/utils'

export function useTodayTotalFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'analyse/today'
  return {
    getTodayTotal() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/total`)
    },
  }
}

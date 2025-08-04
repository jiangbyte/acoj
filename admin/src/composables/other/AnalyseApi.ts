import { request as $alova } from '@/utils'

const pathPrefix = '/core/api/v1/'
const table = 'analyse'

export function useAnalyseFetch() {
  return {
    getTotalUserAnalyse() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/user/total`)
    },
    getTotalProblemAnalyse() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem/total`)
    },
    getTotalProblemSubmitAnalyse() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem/submit/total`)
    },
    getLanguageDistribution() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem/submit/language`)
    },
    getProblemRateDistribution() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem/pass/rate`)
    },
    getWeeklySubmitTrend() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem/submit/trend`)
    },
    getTodayHourlySubmitTrend() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem/submit/today/trend`)
    },
  }
}

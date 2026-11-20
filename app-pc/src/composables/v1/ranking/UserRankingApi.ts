import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useUserRankingFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/user/rank'

  return {
    /*
      * Implementation details.
     */
    useUserRankingPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    useUserRankingTop() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/top`)
    },
    /*
      * Implementation details.
     */
    useUserActiveTop() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/active/top`)
    },
    /*
      * Implementation details.
     */
    useUserRankingDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

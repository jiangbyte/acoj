import { request as $alova } from '@/utils'

/**
 * API请求
 */
export function useUserRankingFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/user/rank'

  return {
    /*
     * 用户排行榜 分页接口
     */
    useUserRankingPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 用户排行榜 详情接口
     */
    useUserRankingTop() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/top`)
    },
    /*
     * 用户排行榜 详情接口
     */
    useUserActiveTop() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/active/top`)
    },
    /*
     * 用户排行榜 详情接口
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

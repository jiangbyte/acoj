import { request as $alova } from '@/utils'

/**
 * 题目排行榜 API请求
 */
export function useSetRankingFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/set'

  return {
    setRankingPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/ranking/page`, {
        params: {
          ...data,
        },
      })
    },
    /**
     * 获取Top排行榜
     */
    setRankingTop() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/set/ranking/top`)
    },
  }
}

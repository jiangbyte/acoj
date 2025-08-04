import { request as $alova } from '@/utils'

/**
 * 题目排行榜 API请求
 */
export function useProblemRankingFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/problem'

  return {
    problemRankingPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/ranking/page`, {
        params: {
          ...data,
        },
      })
    },
  }
}

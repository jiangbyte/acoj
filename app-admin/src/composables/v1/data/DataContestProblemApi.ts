import { request as $alova } from '@/utils'

/**
 * 竞赛题目 API请求
 */
export function useDataContestProblemFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/contest/problem'

  /*
     * 竞赛题目 默认数据
     */
  const dataContestProblemDefaultData = {
    id: '',
    contestId: '',
    problemId: '',
    problemCode: '',
    displayId: '',
    score: 0,
    sort: 0,
    submitCount: 0,
    acceptCount: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 竞赛题目 默认数据
    */
    dataContestProblemDefaultData,

    /*
     * 竞赛题目 分页接口
     */
    dataContestProblemPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 竞赛题目 新增接口
     */
    dataContestProblemAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 竞赛题目 修改接口
     */
    dataContestProblemEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 竞赛题目 删除接口
     */
    dataContestProblemDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },
    dataContestProblemList(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/lists`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 竞赛题目 详情接口
     */
    dataContestProblemDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

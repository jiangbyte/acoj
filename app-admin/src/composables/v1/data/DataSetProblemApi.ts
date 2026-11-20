import { request as $alova } from '@/utils'

/**
 * 题集题目 API请求
 */
export function useDataSetProblemFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/set/problem'

  /*
     * 题集题目 默认数据
     */
  const dataSetProblemDefaultData = {
    id: '',
    setId: '',
    problemId: '',
    sort: 0,
  }
  return {
    /*
    * 题集题目 默认数据
    */
    dataSetProblemDefaultData,

    /*
     * 题集题目 分页接口
     */
    dataSetProblemPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },
    dataSetProblemManageList(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/manageLists`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 题集题目 新增接口
     */
    dataSetProblemAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 题集题目 修改接口
     */
    dataSetProblemEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 题集题目 删除接口
     */
    dataSetProblemDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 题集题目 详情接口
     */
    dataSetProblemDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

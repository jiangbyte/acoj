import { request as $alova } from '@/utils'

/**
 * 题集 API请求
 */
export function useDataSetFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/set'

  /*
     * 题集 默认数据
     */
  const dataSetDefaultData = {
    id: '',
    setType: 0,
    title: '',
    cover: '',
    description: '',
    categoryId: '',
    difficulty: 0,
    startTime: '',
    endTime: '',
    exJson: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 题集 默认数据
    */
    dataSetDefaultData,

    /*
     * 题集 分页接口
     */
    dataSetPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 题集 新增接口
     */
    dataSetAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 题集 修改接口
     */
    dataSetEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 题集 删除接口
     */
    dataSetDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 题集 详情接口
     */
    dataSetDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    dataSetProblem(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem`, {
        params: {
          ...data,
        },
      })
    },
    dataSetLatest() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/latest`)
    },
    dataSetHot() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/hot`)
    },
    dataSetProblemDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

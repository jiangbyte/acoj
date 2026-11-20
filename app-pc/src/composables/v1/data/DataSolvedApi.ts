import { request as $alova } from '@/utils'

/**
 * 用户解决 API请求
 */
export function useDataSolvedFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/solved'

  /*
     * 用户解决 默认数据
     */
  const dataSolvedDefaultData = {
    id: '',
    setId: '',
    isSet: false,
    userId: '',
    problemId: '',
    submitId: '',
    solved: false,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 用户解决 默认数据
    */
    dataSolvedDefaultData,

    /*
     * 用户解决 分页接口
     */
    dataSolvedPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 用户解决 新增接口
     */
    dataSolvedAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 用户解决 修改接口
     */
    dataSolvedEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 用户解决 删除接口
     */
    dataSolvedDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 用户解决 详情接口
     */
    dataSolvedDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

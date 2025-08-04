import { request as $alova } from '@/utils'

/**
 * 用户解决 API请求
 */
export function useProSolvedFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/solved'

  /*
     * 用户解决 默认数据
     */
  const proSolvedDefaultData = {
    id: '',
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
    proSolvedDefaultData,

    /*
     * 用户解决 分页接口
     */
    proSolvedPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 用户解决 新增接口
     */
    proSolvedAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 用户解决 修改接口
     */
    proSolvedEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 用户解决 删除接口
     */
    proSolvedDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 用户解决 详情接口
     */
    proSolvedDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

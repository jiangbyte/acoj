import { request as $alova } from '@/utils'

/**
 * 题集 API请求
 */
export function useProSetFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/set'

  /*
     * 题集 默认数据
     */
  const proSetDefaultData = {
    id: '',
    setType: 0,
    title: '',
    cover: '',
    description: '',
    categoryId: '',
    difficulty: 0,
    startTime: '',
    endTime: '',
    config: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 题集 默认数据
    */
    proSetDefaultData,

    /*
     * 题集 分页接口
     */
    proSetPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 题集 新增接口
     */
    proSetAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 题集 修改接口
     */
    proSetEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 题集 删除接口
     */
    proSetDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 题集 详情接口
     */
    proSetDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    proSetLatest10() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/latest`)
    },
    proSetRecentSolvedPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/user/recent/solved`, {
        params: {
          ...data,
        },
      })
    },
    proSetProblemPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problems/page`, {
        params: {
          ...data,
        },
      })
    },
    proSetProblemList(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problems/list`, {
        params: {
          ...data,
        },
      })
    },
    proSetProblemDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problems/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

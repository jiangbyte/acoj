import { request as $alova } from '@/utils'

/**
 * 题单提交 API请求
 */
export function useProSetSubmitFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/set/submit'

  /*
     * 题单提交 默认数据
     */
  const proSetSubmitDefaultData = {
    id: '',
    userId: '',
    problemId: '',
    setId: '',
    language: '',
    code: '',
    submitType: false,
    maxTime: 0,
    maxMemory: 0,
    message: '',
    testCases: '',
    status: '',
    similarity: 0.0,
    taskId: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 题单提交 默认数据
    */
    proSetSubmitDefaultData,

    /*
     * 题单提交 分页接口
     */
    proSetSubmitPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 题单提交 新增接口
     */
    proSetSubmitAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 题单提交 修改接口
     */
    proSetSubmitEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 题单提交 删除接口
     */
    proSetSubmitDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 题单提交 详情接口
     */
    proSetSubmitDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

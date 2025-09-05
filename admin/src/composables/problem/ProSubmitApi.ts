import { request as $alova } from '@/utils'

/**
 * 提交 API请求
 */
export function useProSubmitFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/submit'

  /*
     * 提交 默认数据
     */
  const proSubmitDefaultData = {
    id: '',
    userId: '',
    problemId: '',
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
    * 提交 默认数据
    */
    proSubmitDefaultData,

    /*
     * 提交 分页接口
     */
    proSubmitPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 提交 新增接口
     */
    proSubmitAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 提交 修改接口
     */
    proSubmitEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 提交 删除接口
     */
    proSubmitDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 提交 详情接口
     */
    proSubmitDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

import { request as $alova } from '@/utils'

/**
 * 提交 API请求
 */
export function useDataSubmitFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/submit'

  /*
     * 提交 默认数据
     */
  const dataSubmitDefaultData = {
    id: '',
    userId: '',
    setId: '',
    isSet: false,
    problemId: '',
    language: '',
    code: '',
    codeLength: 0,
    submitType: false,
    maxTime: 0,
    maxMemory: 0,
    message: '',
    testCase: '',
    status: '',
    isFinish: false,
    similarity: 0.0,
    taskId: '',
    reportId: '',
    similarityCategory: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 提交 默认数据
    */
    dataSubmitDefaultData,

    /*
     * 提交 分页接口
     */
    dataSubmitPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },
    dataSubmitProblemPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem/page`, {
        params: {
          ...data,
        },
      })
    },
    dataSubmitSetPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/set/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 提交 新增接口
     */
    dataSubmitAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 提交 修改接口
     */
    dataSubmitEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 提交 删除接口
     */
    dataSubmitDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 提交 详情接口
     */
    dataSubmitDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    dataSubmitSetExecute(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/set/execute`, data)
    },
    dataSubmitExecute(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/execute`, data)
    },
    dataSubmitProblemStatusCount() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/status/count`)
    },
  }
}

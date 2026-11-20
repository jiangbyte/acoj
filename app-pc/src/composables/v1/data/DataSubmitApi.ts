import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useDataSubmitFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/submit'

  /*
      * Implementation details.
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
     * Implementation details.
    */
    dataSubmitDefaultData,

    /*
      * Implementation details.
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
    dataModuleSubmitPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/module/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    dataSubmitAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    dataSubmitEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    dataSubmitDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    dataSubmitDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail/client`, {
        params: {
          ...data,
        },
      })
    },
    dataSubmitSetExecute(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/set/execute`, data)
    },
    dataSubmitContestExecute(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/contest/execute`, data)
    },
    dataSubmitExecute(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/execute`, data)
    },
    dataSubmitProblemStatusCount() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/status/count`)
    },
  }
}

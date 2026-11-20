import { request as $alova } from '@/utils'

/**
 * Submission API requests
 */
export function useDataSubmitFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/submit'

  /*
     * Submission default data
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
    * Submission default data
    */
    dataSubmitDefaultData,

    /*
     * Submission pagination endpoint
     */
    dataSubmitPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Submission create endpoint
     */
    dataSubmitAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Submission update endpoint
     */
    dataSubmitEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Submission delete endpoint
     */
    dataSubmitDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Submission details endpoint
     */
    dataSubmitDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

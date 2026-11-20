import { request as $alova } from '@/utils'

/**
 * Problem Test Case API requests
 */
export function useDataTestCaseFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/test/case'

  /*
     * Problem Test Case default data
     */
  const dataTestCaseDefaultData = {
    id: '',
    problemId: '',
    caseSign: '',
    inputData: '',
    expectedOutput: '',
    inputFilePath: '',
    inputFileSize: 0,
    outputFilePath: '',
    outputFileSize: 0,
    isSample: false,
    score: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * Problem Test Case default data
    */
    dataTestCaseDefaultData,

    /*
     * Problem Test Case pagination endpoint
     */
    dataTestCasePage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Problem Test Case create endpoint
     */
    dataTestCaseAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Problem Test Case update endpoint
     */
    dataTestCaseEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Problem Test Case delete endpoint
     */
    dataTestCaseDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Problem Test Case details endpoint
     */
    dataTestCaseDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

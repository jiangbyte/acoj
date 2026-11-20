import { request as $alova } from '@/utils'

/**
 * Judge Result Case API requests
 */
export function useDataJudgeCaseFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/judge/case'

  /*
     * Judge Result Case default data
     */
  const dataJudgeCaseDefaultData = {
    id: '',
    submitId: '',
    inputData: '',
    outputData: '',
    expectedOutput: '',
    inputFilePath: '',
    inputFileSize: 0,
    outputFilePath: '',
    outputFileSize: 0,
    maxTime: 0,
    maxMemory: 0,
    isSample: false,
    score: 0.0,
    status: '',
    exitCode: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * Judge Result Case default data
    */
    dataJudgeCaseDefaultData,

    /*
     * Judge Result Case pagination endpoint
     */
    dataJudgeCasePage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Judge Result Case create endpoint
     */
    dataJudgeCaseAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Judge Result Case update endpoint
     */
    dataJudgeCaseEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Judge Result Case delete endpoint
     */
    dataJudgeCaseDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Judge Result Case details endpoint
     */
    dataJudgeCaseDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

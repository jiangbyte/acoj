import { request as $alova } from '@/utils'

/**
 * 判题结果用例 API请求
 */
export function useDataJudgeCaseFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/judge/case'

  /*
     * 判题结果用例 默认数据
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
    * 判题结果用例 默认数据
    */
    dataJudgeCaseDefaultData,

    /*
     * 判题结果用例 分页接口
     */
    dataJudgeCasePage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 判题结果用例 新增接口
     */
    dataJudgeCaseAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 判题结果用例 修改接口
     */
    dataJudgeCaseEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 判题结果用例 删除接口
     */
    dataJudgeCaseDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 判题结果用例 详情接口
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

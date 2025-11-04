import { request as $alova } from '@/utils'

/**
 * 题目测试用例 API请求
 */
export function useDataTestCaseFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/test/case'

  /*
     * 题目测试用例 默认数据
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
    * 题目测试用例 默认数据
    */
    dataTestCaseDefaultData,

    /*
     * 题目测试用例 分页接口
     */
    dataTestCasePage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 题目测试用例 新增接口
     */
    dataTestCaseAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 题目测试用例 修改接口
     */
    dataTestCaseEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 题目测试用例 删除接口
     */
    dataTestCaseDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 题目测试用例 详情接口
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

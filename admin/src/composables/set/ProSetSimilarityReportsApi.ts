import { request as $alova } from '@/utils'

/**
 * 题库题目报告库 API请求
 */
export function useProSetSimilarityReportsFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/set/similarity/reports'

  /*
     * 题库题目报告库 默认数据
     */
  const proSetSimilarityReportsDefaultData = {
    id: '',
    reportType: 0,
    taskId: '',
    setId: '',
    problemId: '',
    sampleCount: 0,
    similarityGroupCount: 0,
    avgSimilarity: 0.0,
    maxSimilarity: 0.0,
    threshold: 0.0,
    similarityDistribution: '',
    degreeStatistics: '',
    checkMode: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 题库题目报告库 默认数据
    */
    proSetSimilarityReportsDefaultData,

    /*
     * 题库题目报告库 分页接口
     */
    proSetSimilarityReportsPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 题库题目报告库 新增接口
     */
    proSetSimilarityReportsAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 题库题目报告库 修改接口
     */
    proSetSimilarityReportsEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 题库题目报告库 删除接口
     */
    proSetSimilarityReportsDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 题库题目报告库 详情接口
     */
    proSetSimilarityReportsDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

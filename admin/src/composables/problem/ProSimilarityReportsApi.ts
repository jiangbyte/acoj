import { request as $alova } from '@/utils'

/**
 * 题目报告库 API请求
 */
export function useProSimilarityReportsFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/similarity/reports'

  /*
     * 题目报告库 默认数据
     */
  const proSimilarityReportsDefaultData = {
    id: '',
    reportType: 0,
    taskId: '',
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
    * 题目报告库 默认数据
    */
    proSimilarityReportsDefaultData,

    /*
     * 题目报告库 分页接口
     */
    proSimilarityReportsPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 题目报告库 新增接口
     */
    proSimilarityReportsAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 题目报告库 修改接口
     */
    proSimilarityReportsEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 题目报告库 删除接口
     */
    proSimilarityReportsDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 题目报告库 详情接口
     */
    proSimilarityReportsDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 题目报告库 删除接口
     */
    proSimilarityReportsGenerate(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/generate`, data)
    },
  }
}

import { request as $alova } from '@/utils'

/**
 * 报告库 API请求
 */
export function useTaskReportsFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'task/reports'

  /*
     * 报告库 默认数据
     */
  const taskReportsDefaultData = {
    id: '',
    reportType: 0,
    taskId: '',
    setId: '',
    isSet: false,
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
    * 报告库 默认数据
    */
    taskReportsDefaultData,

    /*
     * 报告库 分页接口
     */
    taskReportsPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 报告库 新增接口
     */
    taskReportsAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 报告库 修改接口
     */
    taskReportsEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 报告库 删除接口
     */
    taskReportsDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 报告库 详情接口
     */
    taskReportsDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

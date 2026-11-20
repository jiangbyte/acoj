import { request as $alova } from '@/utils'

/**
 * Report Library API requests
 */
export function useTaskReportsFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'task/reports'

  /*
     * Report Library default data
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
    * Report Library default data
    */
    taskReportsDefaultData,

    /*
     * Report Library pagination endpoint
     */
    taskReportsPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Report Library create endpoint
     */
    taskReportsAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Report Library update endpoint
     */
    taskReportsEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Report Library delete endpoint
     */
    taskReportsDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Report Library details endpoint
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

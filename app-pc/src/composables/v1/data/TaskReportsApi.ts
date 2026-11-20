import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useTaskReportsFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'task/reports'

  /*
      * Implementation details.
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
     * Implementation details.
    */
    taskReportsDefaultData,

    /*
      * Implementation details.
     */
    taskReportsPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    taskReportsAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    taskReportsEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    taskReportsDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
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

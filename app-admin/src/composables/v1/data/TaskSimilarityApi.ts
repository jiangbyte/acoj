import { request as $alova } from '@/utils'

/**
 * Check Result Task Library API requests
 */
export function useTaskSimilarityFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'task/similarity'

  /*
     * Check Result Task Library default data
     */
  const taskSimilarityDefaultData = {
    id: '',
    taskId: '',
    taskType: false,
    problemId: '',
    setId: '',
    isSet: false,
    language: '',
    similarity: 0.0,
    submitUser: '',
    submitCode: '',
    submitCodeLength: 0,
    submitId: '',
    submitTime: Date.now(),
    submitTokenName: '',
    submitTokenTexts: '',
    originUser: '',
    originCode: '',
    originCodeLength: 0,
    originId: '',
    originTime: Date.now(),
    originTokenName: '',
    originTokenTexts: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * Check Result Task Library default data
    */
    taskSimilarityDefaultData,

    /*
     * Check Result Task Library pagination endpoint
     */
    taskSimilarityPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Check Result Task Library create endpoint
     */
    taskSimilarityAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Check Result Task Library update endpoint
     */
    taskSimilarityEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Check Result Task Library delete endpoint
     */
    taskSimilarityDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Check Result Task Library details endpoint
     */
    taskSimilarityDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Check Result Task Library batch endpoint
     */
    taskSimilarityBatch(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/batch`, data)
    },
    taskSimilarityProgress(taskId: string) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/progress/${taskId}`)
    },
    taskSimilarityBatchProgress(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/progress/batch`, data)
    },
  }
}

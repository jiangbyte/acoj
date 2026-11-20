import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useTaskSimilarityFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'task/similarity'

  /*
      * Implementation details.
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
    submitTime: '',
    submitTokenName: '',
    submitTokenTexts: '',
    originUser: '',
    originCode: '',
    originCodeLength: 0,
    originId: '',
    originTime: '',
    originTokenName: '',
    originTokenTexts: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
     * Implementation details.
    */
    taskSimilarityDefaultData,

    /*
      * Implementation details.
     */
    taskSimilarityPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    taskSimilarityAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    taskSimilarityEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    taskSimilarityDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    taskSimilarityDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

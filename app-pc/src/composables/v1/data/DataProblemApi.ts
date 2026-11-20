import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useDataProblemFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/problem'

  /*
      * Implementation details.
     */
  const dataProblemDefaultData = {
    id: '',
    displayId: '',
    categoryId: '',
    title: '',
    source: '',
    url: '',
    maxTime: 0,
    maxMemory: 0,
    description: '',
    testCase: '',
    allowedLanguages: '',
    difficulty: 0,
    threshold: 0.0,
    useTemplate: false,
    codeTemplate: '',
    isPublic: false,
    isVisible: false,
    useAi: false,
    solved: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
     * Implementation details.
    */
    dataProblemDefaultData,

    /*
      * Implementation details.
     */
    dataProblemPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page/client`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    dataProblemAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    dataProblemEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    dataProblemDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    dataProblemDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    /*
      * Implementation details.
     */
    dataProblemClientDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/client/detail`, {
        params: {
          ...data,
        },
      })
    },
    dataProblemCount() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/count`)
    },
    dataProblemHot() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/hot`)
    },
    dataProblemLatest() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/latest`)
    },
    dataProblemDifficultyDistribution() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/difficulty/distribution`)
    },
  }
}

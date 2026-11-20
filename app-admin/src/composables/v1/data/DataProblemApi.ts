import { request as $alova } from '@/utils'

/**
 * Problem API requests
 */
export function useDataProblemFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/problem'

  /*
     * Problem default data
     */
  const dataProblemDefaultData = {
    id: '',
    displayId: '',
    categoryId: null,
    title: '',
    source: '',
    url: '',
    maxTime: 0,
    maxMemory: 0,
    description: '',
    testCase: [],
    allowedLanguages: '',
    difficulty: null,
    threshold: 0.0,
    useTemplate: false,
    codeTemplate: [],
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
    * Problem default data
    */
    dataProblemDefaultData,

    /*
     * Problem pagination endpoint
     */
    dataProblemPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },
    dataProblemSetPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/setpage`, {
        params: {
          ...data,
        },
      })
    },
    dataProblemListIds(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/listids`, data)
    },

    /*
     * Problem create endpoint
     */
    dataProblemAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Problem update endpoint
     */
    dataProblemEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Problem delete endpoint
     */
    dataProblemDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Problem details endpoint
     */
    dataProblemDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    dataProblemImport(formData: FormData | undefined) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/import`, formData)
    },
  }
}

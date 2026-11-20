import { request as $alova } from '@/utils'

/**
 * Problem Set API requests
 */
export function useDataSetFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/set'

  /*
     * Problem Set default data
     */
  const dataSetDefaultData = {
    id: '',
    setType: null,
    isVisible: false,
    useAi: false,
    title: '',
    cover: '',
    description: '',
    categoryId: null,
    difficulty: null,
    startTime: Date.now(),
    endTime: Date.now(),
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
    problemIds: [],
  }
  return {
    /*
    * Problem Set default data
    */
    dataSetDefaultData,

    /*
     * Problem Set pagination endpoint
     */
    dataSetPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Problem Set create endpoint
     */
    dataSetAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Problem Set update endpoint
     */
    dataSetEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Problem Set delete endpoint
     */
    dataSetDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Problem Set details endpoint
     */
    dataSetDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    dataProblemGetSetProblemLanguages(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem/commonLanguages`, {
        params: {
          ...data,
        },
      })
    },
    dataSetProblemWithSearch(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/problem/options`, {
        params: {
          ...data,
        },
      })
    },
  }
}

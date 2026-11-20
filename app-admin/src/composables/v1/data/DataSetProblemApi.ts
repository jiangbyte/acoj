import { request as $alova } from '@/utils'

/**
 * Set Problem API requests
 */
export function useDataSetProblemFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/set/problem'

  /*
     * Set Problem default data
     */
  const dataSetProblemDefaultData = {
    id: '',
    setId: '',
    problemId: '',
    sort: 0,
  }
  return {
    /*
    * Set Problem default data
    */
    dataSetProblemDefaultData,

    /*
     * Set Problem pagination endpoint
     */
    dataSetProblemPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },
    dataSetProblemManageList(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/manageLists`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Set Problem create endpoint
     */
    dataSetProblemAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Set Problem update endpoint
     */
    dataSetProblemEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Set Problem delete endpoint
     */
    dataSetProblemDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Set Problem details endpoint
     */
    dataSetProblemDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

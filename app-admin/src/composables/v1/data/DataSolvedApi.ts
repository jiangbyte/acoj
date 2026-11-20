import { request as $alova } from '@/utils'

/**
 * User Solution API requests
 */
export function useDataSolvedFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/solved'

  /*
     * User Solution default data
     */
  const dataSolvedDefaultData = {
    id: '',
    setId: '',
    isSet: false,
    userId: '',
    problemId: '',
    submitId: '',
    solved: false,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * User Solution default data
    */
    dataSolvedDefaultData,

    /*
     * User Solution pagination endpoint
     */
    dataSolvedPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * User Solution create endpoint
     */
    dataSolvedAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * User Solution update endpoint
     */
    dataSolvedEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * User Solution delete endpoint
     */
    dataSolvedDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * User Solution details endpoint
     */
    dataSolvedDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

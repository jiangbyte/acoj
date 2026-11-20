import { request as $alova } from '@/utils'

/**
 * Contest Problem API requests
 */
export function useDataContestProblemFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/contest/problem'

  /*
     * Contest Problem default data
     */
  const dataContestProblemDefaultData = {
    id: '',
    contestId: '',
    problemId: '',
    problemCode: '',
    displayId: '',
    score: 0,
    sort: 0,
    submitCount: 0,
    acceptCount: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * Contest Problem default data
    */
    dataContestProblemDefaultData,

    /*
     * Contest Problem pagination endpoint
     */
    dataContestProblemPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Contest Problem create endpoint
     */
    dataContestProblemAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Contest Problem update endpoint
     */
    dataContestProblemEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Contest Problem delete endpoint
     */
    dataContestProblemDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },
    dataContestProblemList(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/lists`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Contest Problem details endpoint
     */
    dataContestProblemDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

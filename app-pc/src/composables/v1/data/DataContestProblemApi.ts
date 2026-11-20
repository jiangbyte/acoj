import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useDataContestProblemFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/contest/problem'

  /*
      * Implementation details.
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
     * Implementation details.
    */
    dataContestProblemDefaultData,

    /*
      * Implementation details.
     */
    dataContestProblemPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    dataContestProblemAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    dataContestProblemEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    dataContestProblemDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    dataContestProblemDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    dataContestProblemList(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/lists`, {
        params: {
          ...data,
        },
      })
    },
    dataContestProblemDetailClient(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail/client`, {
        params: {
          ...data,
        },
      })
    },
  }
}

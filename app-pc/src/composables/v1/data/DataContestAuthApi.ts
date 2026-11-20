import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useDataContestAuthFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/contest/auth'

  /*
      * Implementation details.
     */
  const dataContestAuthDefaultData = {
    id: '',
    contestId: '',
    userId: '',
    isAuth: false,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
     * Implementation details.
    */
    dataContestAuthDefaultData,

    /*
      * Implementation details.
     */
    dataContestAuthPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    dataContestAuthAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },
    dataContestAuth(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/user`, data)
    },

    /*
      * Implementation details.
     */
    dataContestAuthEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    dataContestAuthDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    dataContestAuthDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

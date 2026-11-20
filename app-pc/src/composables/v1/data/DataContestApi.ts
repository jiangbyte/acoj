import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useDataContestFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/contest'

  /*
      * Implementation details.
     */
  const dataContestDefaultData = {
    id: '',
    title: '',
    description: '',
    contestType: 'OI',
    ruleType: '',
    categoryId: '',
    cover: '',
    maxTeamMembers: 0,
    isTeamContest: false,
    isPublic: false,
    password: '',
    registerStartTime: '',
    registerEndTime: '',
    contestStartTime: '',
    contestEndTime: '',
    frozenTime: 0,
    penaltyTime: 0,
    allowedLanguages: '',
    status: '',
    sort: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
     * Implementation details.
    */
    dataContestDefaultData,

    /*
      * Implementation details.
     */
    dataContestPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page/client`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    dataContestAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    dataContestEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    dataContestDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },
    dataContestSignUp(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/signUp`, data)
    },
    dataContestCancelSignUp(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/cancelSignUp`, data)
    },

    dataContestHot() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/hot`)
    },
    /*
      * Implementation details.
     */
    dataContestDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail/client`, {
        params: {
          ...data,
        },
      })
    },
  }
}

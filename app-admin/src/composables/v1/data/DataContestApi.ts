import { request as $alova } from '@/utils'

/**
 * Contest API requests
 */
export function useDataContestFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/contest'

  /*
     * Contest default data
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
    registerStartTime: Date.now(),
    registerEndTime: Date.now(),
    contestStartTime: Date.now(),
    contestEndTime: Date.now(),
    frozenTime: 0,
    penaltyTime: 0,
    allowedLanguages: [],
    status: '',
    sort: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
    isVisible: false,
    problemIds: [],
  }
  return {
    /*
    * Contest default data
    */
    dataContestDefaultData,

    /*
     * Contest pagination endpoint
     */
    dataContestPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Contest create endpoint
     */
    dataContestAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Contest update endpoint
     */
    dataContestEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Contest delete endpoint
     */
    dataContestDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Contest details endpoint
     */
    dataContestDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

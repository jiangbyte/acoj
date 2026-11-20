import { request as $alova } from '@/utils'

/**
 * Contest Participant API requests
 */
export function useDataContestParticipantFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/contest/participant'

  /*
     * Contest Participant default data
     */
  const dataContestParticipantDefaultData = {
    id: '',
    contestId: '',
    userId: '',
    teamId: '',
    teamName: '',
    isTeamLeader: false,
    registerTime: '',
    status: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * Contest Participant default data
    */
    dataContestParticipantDefaultData,

    /*
     * Contest Participant pagination endpoint
     */
    dataContestParticipantPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Contest Participant create endpoint
     */
    dataContestParticipantAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Contest Participant update endpoint
     */
    dataContestParticipantEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Contest Participant delete endpoint
     */
    dataContestParticipantDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Contest Participant details endpoint
     */
    dataContestParticipantDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

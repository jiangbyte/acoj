import { request as $alova } from '@/utils'

/**
 * 竞赛参与 API请求
 */
export function useDataContestParticipantFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/contest/participant'

  /*
     * 竞赛参与 默认数据
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
    * 竞赛参与 默认数据
    */
    dataContestParticipantDefaultData,

    /*
     * 竞赛参与 分页接口
     */
    dataContestParticipantPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 竞赛参与 新增接口
     */
    dataContestParticipantAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 竞赛参与 修改接口
     */
    dataContestParticipantEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 竞赛参与 删除接口
     */
    dataContestParticipantDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 竞赛参与 详情接口
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

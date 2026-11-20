import { request as $alova } from '@/utils'

/**
 * 竞赛 API请求
 */
export function useDataContestFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/contest'

  /*
     * 竞赛 默认数据
     */
  const dataContestDefaultData = {
    id: '',
    title: '',
    description: '',
    contestType: '',
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
    * 竞赛 默认数据
    */
    dataContestDefaultData,

    /*
     * 竞赛 分页接口
     */
    dataContestPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page/client`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 竞赛 新增接口
     */
    dataContestAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 竞赛 修改接口
     */
    dataContestEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 竞赛 删除接口
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
     * 竞赛 详情接口
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

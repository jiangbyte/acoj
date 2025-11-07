import { request as $alova } from '@/utils'

/**
 * 竞赛认证 API请求
 */
export function useDataContestAuthFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/contest/auth'

  /*
     * 竞赛认证 默认数据
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
    * 竞赛认证 默认数据
    */
    dataContestAuthDefaultData,

    /*
     * 竞赛认证 分页接口
     */
    dataContestAuthPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 竞赛认证 新增接口
     */
    dataContestAuthAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },
    dataContestAuth(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/user`, data)
    },

    /*
     * 竞赛认证 修改接口
     */
    dataContestAuthEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 竞赛认证 删除接口
     */
    dataContestAuthDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 竞赛认证 详情接口
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

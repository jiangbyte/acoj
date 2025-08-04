/**
* 用户题集解决记录 API请求
*/
export function useProSetSolvedFetch() {
  const { $alova } = useNuxtApp()
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/set/solved'

    /*
     * 用户题集解决记录 默认数据
     */
  const proSetSolvedDefaultData = {
    id: '',
      userId: '',
      problemId: '',
      problemSetId: '',
      submitId: '',
      solved: false,
      createTime: '',
      createUser: '',
      updateTime: '',
      updateUser: '',
    }
  return {
    /*
     * 用户题集解决记录 默认数据
     */
    proSetSolvedDefaultData,

    /*
     * 用户题集解决记录 分页接口
     */
    proSetSolvedPage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 用户题集解决记录 新增接口
     */
    proSetSolvedAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 用户题集解决记录 修改接口
     */
    proSetSolvedEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 用户题集解决记录 删除接口
     */
    proSetSolvedDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 用户题集解决记录 详情接口
     */
    proSetSolvedDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}
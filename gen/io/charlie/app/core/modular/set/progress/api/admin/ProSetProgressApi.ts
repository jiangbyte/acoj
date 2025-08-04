import { request as $alova } from '@/utils'

/**
* 题集进度 API请求
*/
export function useProSetProgressFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/set/progress'

    /*
     * 题集进度 默认数据
     */
    const proSetProgressDefaultData = {
    id: '',
        userId: '',
        problemSetId: '',
        progress: '',
        completed: false,
        completedAt: '',
        createTime: '',
        createUser: '',
        updateTime: '',
        updateUser: '',
    }
 return {
    /*
    * 题集进度 默认数据
    */
    proSetProgressDefaultData,

    /*
     * 题集进度 分页接口
     */
    proSetProgressPage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 题集进度 新增接口
     */
    proSetProgressAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 题集进度 修改接口
     */
    proSetProgressEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 题集进度 删除接口
     */
    proSetProgressDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 题集进度 详情接口
     */
    proSetProgressDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}
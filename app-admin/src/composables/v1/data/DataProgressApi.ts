import { request as $alova } from '@/utils'

/**
 * 题集进度 API请求
 */
export function useDataProgressFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'data/progress'

  /*
     * 题集进度 默认数据
     */
  const dataProgressDefaultData = {
    id: '',
    userId: '',
    setId: '',
    problemId: '',
    status: '',
    extraJson: '',
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
    dataProgressDefaultData,

    /*
     * 题集进度 分页接口
     */
    dataProgressPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 题集进度 新增接口
     */
    dataProgressAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 题集进度 修改接口
     */
    dataProgressEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 题集进度 删除接口
     */
    dataProgressDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 题集进度 详情接口
     */
    dataProgressDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

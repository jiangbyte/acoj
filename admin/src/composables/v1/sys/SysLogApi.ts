import { request as $alova } from '@/utils'

/**
 * 系统活动/日志记录 API请求
 */
export function useSysLogFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/log'

  /*
     * 系统活动/日志记录 默认数据
     */
  const sysLogDefaultData = {
    id: '',
    userId: '',
    operation: '',
    method: '',
    params: '',
    ip: '',
    operationTime: Date.now(),
    category: '',
    module: '',
    status: '',
    message: '',
    description: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 系统活动/日志记录 默认数据
    */
    sysLogDefaultData,

    /*
     * 系统活动/日志记录 分页接口
     */
    sysLogPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 系统活动/日志记录 新增接口
     */
    sysLogAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 系统活动/日志记录 修改接口
     */
    sysLogEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 系统活动/日志记录 删除接口
     */
    sysLogDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 系统活动/日志记录 详情接口
     */
    sysLogDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    sysLogRecent() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/recent`)
    },
  }
}

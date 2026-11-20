import { request as $alova } from '@/utils'

/**
 * System Activity and Log API requests
 */
export function useSysLogFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/log'

  /*
     * System Activity and Log default data
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
    * System Activity and Log default data
    */
    sysLogDefaultData,

    /*
     * System Activity and Log pagination endpoint
     */
    sysLogPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * System Activity and Log create endpoint
     */
    sysLogAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * System Activity and Log update endpoint
     */
    sysLogEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * System Activity and Log delete endpoint
     */
    sysLogDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * System Activity and Log details endpoint
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

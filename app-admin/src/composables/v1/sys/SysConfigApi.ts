import { request as $alova } from '@/utils'

/**
 * System Configuration API requests
 */
export function useSysConfigFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/config'

  /*
     * System Configuration default data
     */
  const sysConfigDefaultData = {
    id: '',
    name: '',
    code: '',
    value: '',
    componentType: null,
    sort: 0,
    description: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * System Configuration default data
    */
    sysConfigDefaultData,

    /*
     * System Configuration pagination endpoint
     */
    sysConfigPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },
    sysConfigList(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/list`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * System Configuration create endpoint
     */
    sysConfigAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * System Configuration update endpoint
     */
    sysConfigEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * System Configuration delete endpoint
     */
    sysConfigDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * System Configuration details endpoint
     */
    sysConfigDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    /*
     * System Configuration get-value endpoint
     */
    getValueByCode(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/code`, {
        params: {
          ...data,
        },
      })
    },
  }
}

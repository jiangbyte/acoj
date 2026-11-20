import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useSysConfigFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/config'

  /*
      * Implementation details.
     */
  const sysConfigDefaultData = {
    id: '',
    name: '',
    code: '',
    value: '',
    componentType: '',
    description: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
     * Implementation details.
    */
    sysConfigDefaultData,

    /*
      * Implementation details.
     */
    sysConfigPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    sysConfigAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    sysConfigEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    sysConfigDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    sysConfigDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    /*
      * Implementation details.
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

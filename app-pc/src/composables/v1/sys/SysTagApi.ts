import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useSysTagFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/tag'

  /*
      * Implementation details.
     */
  const sysTagDefaultData = {
    id: '',
    name: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
     * Implementation details.
    */
    sysTagDefaultData,

    /*
      * Implementation details.
     */
    sysTagPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    sysTagAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    sysTagEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    sysTagDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    sysTagDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    /*
      * Implementation details.
     */
    sysTagOptions(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/options`, {
        params: {
          ...data,
        },
      })
    },
  }
}

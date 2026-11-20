import { request as $alova } from '@/utils'

/**
 * Tag API requests
 */
export function useSysTagFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/tag'

  /*
     * Tag default data
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
    * Tag default data
    */
    sysTagDefaultData,

    /*
     * Tag pagination endpoint
     */
    sysTagPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Tag create endpoint
     */
    sysTagAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Tag update endpoint
     */
    sysTagEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Tag delete endpoint
     */
    sysTagDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Tag details endpoint
     */
    sysTagDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    /*
     * Tag options endpoint
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

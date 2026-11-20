import { request as $alova } from '@/utils'

/**
 * Notice API requests
 */
export function useSysNoticeFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/notice'

  /*
     * Notice default data
     */
  const sysNoticeDefaultData = {
    id: '',
    title: '',
    cover: '',
    url: '',
    sort: 0,
    content: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
    isVisible: false,
  }
  return {
    /*
    * Notice default data
    */
    sysNoticeDefaultData,

    /*
     * Notice pagination endpoint
     */
    sysNoticePage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Notice create endpoint
     */
    sysNoticeAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Notice update endpoint
     */
    sysNoticeEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Notice delete endpoint
     */
    sysNoticeDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Notice details endpoint
     */
    sysNoticeDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

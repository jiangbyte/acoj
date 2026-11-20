import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useSysNoticeFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/notice'

  /*
      * Implementation details.
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
     * Implementation details.
    */
    sysNoticeDefaultData,

    /*
      * Implementation details.
     */
    sysNoticePage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    sysNoticeAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    sysNoticeEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    sysNoticeDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    sysNoticeDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail/client`, {
        params: {
          ...data,
        },
      })
    },
    /*
      * Implementation details.
     */
    sysNoticeLatest() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/latest`)
    },
    sysNoticeLists() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/lists`)
    },
  }
}

import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useSysBannerFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/banner'

  /*
      * Implementation details.
     */
  const sysBannerDefaultData = {
    id: '',
    title: '',
    banner: '',
    buttonText: '',
    toUrl: '',
    sort: 0,
    subtitle: '',
    isVisible: false,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
     * Implementation details.
    */
    sysBannerDefaultData,

    /*
      * Implementation details.
     */
    sysBannerPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    sysBannerAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    sysBannerEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    sysBannerDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    sysBannerDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    sysBannerLatest() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/latest`)
    },
  }
}

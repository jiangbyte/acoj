import { request as $alova } from '@/utils'

/**
 * Banner API requests
 */
export function useSysBannerFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/banner'

  /*
     * Banner default data
     */
  const sysBannerDefaultData = {
    id: '',
    title: '',
    banner: '',
    buttonText: '',
    isVisibleButton: false,
    jumpModule: null,
    jumpType: null,
    jumpTarget: null,
    targetBlank: false,
    sort: 0,
    subtitle: '',
    isVisibleSubtitle: false,
    isVisible: false,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * Banner default data
    */
    sysBannerDefaultData,

    /*
     * Banner pagination endpoint
     */
    sysBannerPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Banner create endpoint
     */
    sysBannerAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Banner update endpoint
     */
    sysBannerEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Banner delete endpoint
     */
    sysBannerDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Banner details endpoint
     */
    sysBannerDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Banner latest endpoint
     */
    sysBannerLatest() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/latest`)
    },
    sysBannerJumpTargetList(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/jumpTargetList`, {
        params: {
          ...data,
        },
      })
    },
  }
}

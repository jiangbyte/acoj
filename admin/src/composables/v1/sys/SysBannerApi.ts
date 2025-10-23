import { request as $alova } from '@/utils'

/**
 * 横幅 API请求
 */
export function useSysBannerFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/banner'

  /*
     * 横幅 默认数据
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
    * 横幅 默认数据
    */
    sysBannerDefaultData,

    /*
     * 横幅 分页接口
     */
    sysBannerPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 横幅 新增接口
     */
    sysBannerAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 横幅 修改接口
     */
    sysBannerEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 横幅 删除接口
     */
    sysBannerDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 横幅 详情接口
     */
    sysBannerDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 横幅 最新接口
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

import { request as $alova } from '@/utils'

/**
 * 标签 API请求
 */
export function useSysTagFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'sys/tag'

  /*
     * 标签 默认数据
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
    * 标签 默认数据
    */
    sysTagDefaultData,

    /*
     * 标签 分页接口
     */
    sysTagPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 标签 新增接口
     */
    sysTagAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 标签 修改接口
     */
    sysTagEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 标签 删除接口
     */
    sysTagDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 标签 详情接口
     */
    sysTagDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 标签 选型接口
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

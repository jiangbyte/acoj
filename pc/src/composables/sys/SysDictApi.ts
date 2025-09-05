import { request as $alova } from '@/utils'

/**
 * 系统字典 API请求
 */
export function useSysDictFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'sys/dict'

  /*
     * 系统字典 默认数据
     */
  const sysDictDefaultData = {
    id: '',
    dictType: '',
    typeLabel: '',
    dictValue: '',
    dictLabel: '',
    sortOrder: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 系统字典 默认数据
    */
    sysDictDefaultData,

    /*
     * 系统字典 分页接口
     */
    sysDictPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 系统字典 新增接口
     */
    sysDictAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 系统字典 修改接口
     */
    sysDictEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 系统字典 删除接口
     */
    sysDictDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 系统字典 详情接口
     */
    sysDictDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    /*
     * 系统字典 详情接口
     */
    sysDictTree(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/tree`, {
        params: {
          ...data,
        },
      })
    },
    /*
     * 系统字典 详情接口
     */
    sysDictListOption(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/list/option`, {
        params: {
          ...data,
        },
      })
    },
    /*
     * 系统字典 详情接口
     */
    sysDictOptions(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/options`, {
        params: {
          ...data,
        },
      })
    },
    /*
     * 系统字典 详情接口
     */
    sysDictListGroup(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/list/group`, {
        params: {
          ...data,
        },
      })
    },
  }
}

import { request as $alova } from '@/utils'

/**
 * 分类 API请求
 */
export function useSysCategoryFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'sys/category'

  /*
     * 分类 默认数据
     */
  const sysCategoryDefaultData = {
    id: '',
    name: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 分类 默认数据
    */
    sysCategoryDefaultData,

    /*
     * 分类 分页接口
     */
    sysCategoryPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 分类 新增接口
     */
    sysCategoryAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 分类 修改接口
     */
    sysCategoryEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 分类 删除接口
     */
    sysCategoryDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 分类 详情接口
     */
    sysCategoryDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 分类 选型接口
     */
    sysCategoryOptions(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/options`, {
        params: {
          ...data,
        },
      })
    },
  }
}

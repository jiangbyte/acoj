import { request as $alova } from '@/utils'

/**
* 分类 API请求
*/
export function useProCategoryFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'pro/category'

    /*
     * 分类 默认数据
     */
    const proCategoryDefaultData = {
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
    proCategoryDefaultData,

    /*
     * 分类 分页接口
     */
    proCategoryPage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 分类 新增接口
     */
    proCategoryAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 分类 修改接口
     */
    proCategoryEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 分类 删除接口
     */
    proCategoryDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 分类 详情接口
     */
    proCategoryDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}
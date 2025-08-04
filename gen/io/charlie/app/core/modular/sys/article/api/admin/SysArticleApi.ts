import { request as $alova } from '@/utils'

/**
* 系统文章 API请求
*/
export function useSysArticleFetch() {
  const pathPrefix = '/core/api/v1/'
  const table = 'sys/article'

    /*
     * 系统文章 默认数据
     */
    const sysArticleDefaultData = {
    id: '',
        title: '',
        subtitle: '',
        cover: '',
        author: '',
        summary: '',
        sort: 0,
        toUrl: '',
        parentId: '',
        type: '',
        category: '',
        content: '',
        createTime: '',
        createUser: '',
        updateTime: '',
        updateUser: '',
    }
 return {
    /*
    * 系统文章 默认数据
    */
    sysArticleDefaultData,

    /*
     * 系统文章 分页接口
     */
    sysArticlePage(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/page', {
        params: {
        ...data,
        },
      })
    },

    /*
     * 系统文章 新增接口
     */
    sysArticleAdd(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/add', data)
    },

    /*
     * 系统文章 修改接口
     */
    sysArticleEdit(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/edit', data)
    },

    /*
     * 系统文章 删除接口
     */
    sysArticleDelete(data: any) {
      return $alova.Post<IResult<any>>(pathPrefix + table + '/delete', data)
    },

    /*
     * 系统文章 详情接口
     */
    sysArticleDetail(data: any) {
      return $alova.Get<IResult<any>>(pathPrefix + table + '/detail', {
        params: {
        ...data,
        },
      })
    },
  }
}
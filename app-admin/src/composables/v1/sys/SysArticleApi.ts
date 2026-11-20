import { request as $alova } from '@/utils'

/**
 * System Article API requests
 */
export function useSysArticleFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/article'

  /*
     * System Article default data
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
    * System Article default data
    */
    sysArticleDefaultData,

    /*
     * System Article pagination endpoint
     */
    sysArticlePage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * System Article create endpoint
     */
    sysArticleAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * System Article update endpoint
     */
    sysArticleEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * System Article delete endpoint
     */
    sysArticleDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * System Article details endpoint
     */
    sysArticleDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
  }
}

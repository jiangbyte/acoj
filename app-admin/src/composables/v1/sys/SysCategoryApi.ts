import { request as $alova } from '@/utils'

/**
 * Category API requests
 */
export function useSysCategoryFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/category'

  /*
     * Category default data
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
    * Category default data
    */
    sysCategoryDefaultData,

    /*
     * Category pagination endpoint
     */
    sysCategoryPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Category create endpoint
     */
    sysCategoryAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Category update endpoint
     */
    sysCategoryEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Category delete endpoint
     */
    sysCategoryDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Category details endpoint
     */
    sysCategoryDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Category options endpoint
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

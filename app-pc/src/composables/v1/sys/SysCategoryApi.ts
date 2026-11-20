import { request as $alova } from '@/utils'

/**
  * Implementation details.
 */
export function useSysCategoryFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/category'

  /*
      * Implementation details.
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
     * Implementation details.
    */
    sysCategoryDefaultData,

    /*
      * Implementation details.
     */
    sysCategoryPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
      * Implementation details.
     */
    sysCategoryAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
      * Implementation details.
     */
    sysCategoryEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
      * Implementation details.
     */
    sysCategoryDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
      * Implementation details.
     */
    sysCategoryDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    }, /*
      * Implementation details.
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

import { request as $alova } from '@/utils'

/**
 * System Dictionary API requests
 */
export function useSysDictFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/dict'

  /*
     * System Dictionary default data
     */
  const sysDictDefaultData = {
    id: '',
    dictType: null,
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
    * System Dictionary default data
    */
    sysDictDefaultData,

    /*
     * System Dictionary pagination endpoint
     */
    sysDictPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * System Dictionary create endpoint
     */
    sysDictAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * System Dictionary update endpoint
     */
    sysDictEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * System Dictionary delete endpoint
     */
    sysDictDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * System Dictionary details endpoint
     */
    sysDictDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    sysDictTree(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/tree`, {
        params: {
          ...data,
        },
      })
    },
    sysDictListOption(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/list/option`, {
        params: {
          ...data,
        },
      })
    },
    sysDictListGroup(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/list/group`, {
        params: {
          ...data,
        },
      })
    },
    /*
     * Dictionary options endpoint
     */
    sysDictOptions(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/options`, {
        params: {
          ...data,
        },
      })
    },
  }
}

import { request as $alova } from '@/utils'

/**
 * User Group API requests
 */
export function useSysGroupFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/group'

  /*
     * User Group default data
     */
  const sysGroupDefaultData = {
    id: '',
    parentId: '',
    name: '',
    code: '',
    description: '',
    sort: 0,
    adminId: null,
    groupType: false,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * User Group default data
    */
    sysGroupDefaultData,

    /*
     * User Group pagination endpoint
     */
    sysGroupPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * User Group create endpoint
     */
    sysGroupAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * User Group update endpoint
     */
    sysGroupEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * User Group delete endpoint
     */
    sysGroupDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * User Group details endpoint
     */
    sysGroupDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    sysGroupAuthTree(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/tree`, {
        params: {
          ...data,
        },
      })
    },
  }
}

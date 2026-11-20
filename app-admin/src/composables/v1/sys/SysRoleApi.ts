import { request as $alova } from '@/utils'

/**
 * Role API requests
 */
export function useSysRoleFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/role'

  /*
     * Role default data
     */
  const sysRoleDefaultData = {
    id: '',
    name: '',
    code: '',
    description: '',
    level: 0,
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
    assignGroupIds: [],
  }
  return {
    /*
    * Role default data
    */
    sysRoleDefaultData,

    /*
     * Role pagination endpoint
     */
    sysRolePage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Role create endpoint
     */
    sysRoleAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Role update endpoint
     */
    sysRoleEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Role delete endpoint
     */
    sysRoleDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Role details endpoint
     */
    sysRoleDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    sysRoleAuthRoles() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/auth/list`)
    },
    sysRoleAuthRoles1() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/auth/list1`)
    },
    sysRoleAssign(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/assign`, data)
    },
    sysMenuAssign(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/menu/assign`, data)
    },
  }
}

import { request as $alova } from '@/utils'

/**
 * Menu API requests
 */
export function useSysMenuFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/menu'

  /*
     * Menu default data
     */
  const sysMenuDefaultData = {
    id: '',
    pid: '',
    name: '',
    path: '',
    componentPath: '',
    title: '',
    icon: '',
    parameters: '',
    keepAlive: false,
    visible: false,
    sort: 0,
    pined: false,
    menuType: 0,
    exJson: [],
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * Menu default data
    */
    sysMenuDefaultData,

    /*
     * Menu pagination endpoint
     */
    sysMenuPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * Menu create endpoint
     */
    sysMenuAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * Menu update endpoint
     */
    sysMenuEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * Menu delete endpoint
     */
    sysMenuDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * Menu details endpoint
     */
    sysMenuDetail(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/detail`, {
        params: {
          ...data,
        },
      })
    },
    sysMenuTreeList() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/tree/list`)
    },
    sysMenuAuthList() {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/auth/list`)
    },
    sysMenuAssignMenuPermission(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/auth/assign/permission`, data)
    },
  }
}

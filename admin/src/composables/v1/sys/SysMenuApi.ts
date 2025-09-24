import { request as $alova } from '@/utils'

/**
 * 菜单 API请求
 */
export function useSysMenuFetch() {
  const context = import.meta.env.VITE_MAIN_SERVICE_CONTEXT
  const pathPrefix = `${context}/api/v1/`
  const table = 'sys/menu'

  /*
     * 菜单 默认数据
     */
  const sysMenuDefaultData = {
    id: '',
    pid: '',
    name: '',
    path: '',
    componentPath: '',
    title: '',
    icon: '',
    keepAlive: false,
    visible: false,
    sort: 0,
    pined: false,
    menuType: 0,
    exJson: '',
    createTime: '',
    createUser: '',
    updateTime: '',
    updateUser: '',
  }
  return {
    /*
    * 菜单 默认数据
    */
    sysMenuDefaultData,

    /*
     * 菜单 分页接口
     */
    sysMenuPage(data: any) {
      return $alova.Get<IResult<any>>(`${pathPrefix + table}/page`, {
        params: {
          ...data,
        },
      })
    },

    /*
     * 菜单 新增接口
     */
    sysMenuAdd(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/add`, data)
    },

    /*
     * 菜单 修改接口
     */
    sysMenuEdit(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/edit`, data)
    },

    /*
     * 菜单 删除接口
     */
    sysMenuDelete(data: any) {
      return $alova.Post<IResult<any>>(`${pathPrefix + table}/delete`, data)
    },

    /*
     * 菜单 详情接口
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
  }
}
